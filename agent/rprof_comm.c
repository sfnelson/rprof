#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <curl/curl.h>
#include <stdint.h>

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

#include "agent_util.h"
#include "rprof_events.h"
#include "rprof_comm.h"

#define EVENT_BUFFER_SIZE (20 * 1024 * 1024 * sizeof(unsigned char))

typedef struct {
    jvmtiEnv *jvmti;
	unsigned char **image;
	jint   *length;
	jint   *class_id;
    jint   *properties;
	size_t  offset;
} Response;

struct store {
    size_t using;       /* number of threads currently using store */
    size_t size;        /* consumed data */
    size_t max;         /* available data */
    size_t records;     /* number of records contained */
    union {
        jboolean flush;
        size_t _padding;
    } flush;       /* should flush, aligned */
    unsigned char events[EVENT_BUFFER_SIZE];
};

struct _CommEnv {
	jvmtiEnv      *jvmti;
	jrawMonitorID  lock;
    struct store  *store;
    volatile jlong prev_id;
    char *start;
    char *weave;
    char *log;
    char *stop;
    char *dataset;
    char *benchmark;
};

#define LOCK(J, L) \
check_jvmti_error(J, (*J)->RawMonitorEnter(J, L), "cannot get monitor");

#define RELEASE(J, L) \
check_jvmti_error(J, (*J)->RawMonitorExit(J, L), "cannot release monitor");

#define ADD_HEADERS(X) \
ADD_HEADER(X, "Content-Type: application/rprof") \
ADD_HEADER(X, "Connection: Keep-Alive") \
ADD_HEADER(X, "Keep-Alive: 600")

#define HEADER(X) \
curl_slist_append(NULL, X)

#define ADD_HEADER(X, Y) \
X = curl_slist_append(X, Y);

typedef curl_write_callback header_func;
typedef curl_write_callback data_func;

static void
post(const char* dest, struct curl_slist *headers, void *data, size_t len,
     header_func cbHeader, void * header_param,
     data_func cbData, void * data_param)
{
    CURLcode err;
    long int status;
    CURL* curl = curl_easy_init();
    
    ADD_HEADERS(headers);
    
    curl_easy_setopt(curl, CURLOPT_URL, dest);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    
    if (data != NULL) {
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, len);
    }
    if (cbHeader != NULL) {
        curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, cbHeader);
        curl_easy_setopt(curl, CURLOPT_HEADERDATA, header_param);
    }
    if (cbData != NULL) {
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, cbData);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, data_param);
    }
    
    err = curl_easy_perform(curl);
    
    if (err != CURLE_OK) {   
        fatal_error("error sending %s! %d: %s",
                    dest, err, curl_easy_strerror(err));
        return;
    }
    
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &status);
    if (status/100 != 2) {
        fatal_error("error sending %s! HTTP %ld",
                    dest, status);
        return;
    }
    
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
}

static struct store *
create_store(CommEnv env)
{
    struct store *s;
    
    s = allocate(env->jvmti, sizeof(struct store));
    s->using = 0;
    s->size = 0;
    s->max = EVENT_BUFFER_SIZE;
    s->records = 0;
    s->flush.flush = JNI_FALSE;
    
    return s;
}

static void
destroy_store(CommEnv env, struct store *s)
{
    struct curl_slist *headers = NULL;
    char records[255];
    
    if (s->size > 0) {
        sprintf(records, "Records: %lu", s->records);
        ADD_HEADER(headers, env->dataset)
        ADD_HEADER(headers, records)
        post(env->log, headers, s->events, s->size,
             NULL, NULL, NULL, NULL);
    }
    
    bzero(s, sizeof(struct store));
    deallocate(env->jvmti, s);
}

static jlong
request_store(CommEnv env, size_t size, struct store **store, EventRecord **record)
{
    struct store *current = NULL, *old = NULL;
    jlong id = 0;
    
    LOCK(env->jvmti, env->lock)
    
    current = env->store;
    
    if (current->size + size > current->max) {
        old = current;
        current = create_store(env);
        env->store = current;
    }
    
    if (size > 0) {
        (*record) = (EventRecord*) &(current->events[current->size]);
        current->size += size;
        current->records++;
        
        id = ++(env->prev_id);
    }
    
    (current->using)++;
    
    if (old != NULL && old->using > 0) {
        old->flush.flush = JNI_TRUE;
        RELEASE(env->jvmti, env->lock)
    }
    else if (old != NULL) {
        RELEASE(env->jvmti, env->lock)
        destroy_store(env, old);
    }
    else {
        RELEASE(env->jvmti, env->lock)
    }
    
    (*store) = current;
    return id;
}

static void
release_store(CommEnv env, struct store *store)
{
    LOCK(env->jvmti, env->lock)
    
    if (store->using == 1 && store->flush.flush == JNI_TRUE) {
        RELEASE(env->jvmti, env->lock)
        destroy_store(env, store);
    }
    else {
        (store->using)--;
        RELEASE(env->jvmti, env->lock)
    }
}

#define URL_START   "http://%s/start"
#define URL_STOP    "http://%s/stop"
#define URL_LOG     "http://%s/logger"
#define URL_WEAVE   "http://%s/weaver?cls=%%s"
#define HEADER_BENCHMARK "Benchmark: %s"

#define OPT_INIT(E, A, F, P) \
A = allocate(E->jvmti, strlen(F) + strlen(P) + 1); \
sprintf(A, F, P);

static void
comm_init(CommEnv env, const char *host, const char *benchmark)
{
    OPT_INIT(env, env->start, URL_START, host)
    OPT_INIT(env, env->log,   URL_LOG,   host)
    OPT_INIT(env, env->weave, URL_WEAVE, host)
    OPT_INIT(env, env->stop,  URL_STOP,  host)
    OPT_INIT(env, env->benchmark, HEADER_BENCHMARK, benchmark);
}

CommEnv
comm_create(jvmtiEnv *jvmti, char *options)
{
    CommEnv env;
	jvmtiError error;
	char *benchmark;
    
    env = allocate(jvmti, sizeof(struct _CommEnv));
	
    env->jvmti = jvmti;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, "comm", &(env->lock));
    check_jvmti_error(jvmti, error, "Cannot create monitor");
    
    env->store = create_store(env);
    env->prev_id = 0;
    env->start = NULL;
    env->weave = NULL;
    env->log = NULL;
    env->stop = NULL;
    env->dataset = NULL;
    env->benchmark = NULL;
    
	if (0 == options || 0 == strlen(options)) {
		comm_init(env, "localhost:8888", "unknown");
	}
	else if (0 == strchr(options, ',')) {
        comm_init(env, options, "unknown");
	}
	else {
	    benchmark = strchr(options, ',');
	    benchmark[0] = 0;
	    benchmark++;
	    comm_init(env, options, benchmark);
	}
    
	curl_global_init(CURL_GLOBAL_ALL);
    
    return env;
}

#define CONTENT_LENGTH "Content-Length: "
#define CONTENT_LENGTH_LEN 16
#define CLASS_ID "Class-id: "
#define CLASS_ID_LEN 10
#define CLASS_PROPERTIES "Properties: "
#define CLASS_PROPERTIES_LEN 12

static size_t
read_header(char *input, size_t size, size_t count, void * arg)
{
    jvmtiEnv *jvmti;
    jvmtiError error;
    Response *response = arg;
	jint len;
    jint id;
    
	if (strncasecmp(input, CONTENT_LENGTH, CONTENT_LENGTH_LEN) == 0) {
		len = (jint) (strtoull(&input[CONTENT_LENGTH_LEN], NULL, 0) & 0xFFFFFFFF);
        if (len > 0) {
            jvmti = response->jvmti;

            *(response->length) = len;
            
            /* deallocated by jvm after load */
            /* don't use allocate because this MUST be JVM allocated */
            error = (*jvmti)->Allocate(jvmti, len, response->image);
            check_jvmti_error(jvmti, error, "Error allocating space for class file");
        }
        else {
            *(response->length) = 0;
            *(response->image) = NULL;
        }
	}
    
    if (strncasecmp(input, CLASS_ID, CLASS_ID_LEN) == 0) {
		id = (jint) (strtoull(&input[CLASS_ID_LEN], NULL, 0) & 0xFFFFFFFF);
	    *(response->class_id) = id;
	}
    
    if (strncasecmp(input, CLASS_PROPERTIES, CLASS_PROPERTIES_LEN) == 0) {
		id = (jint) (strtoull(&input[CLASS_PROPERTIES_LEN], NULL, 0) & 0xFFFFFFFF);
	    *(response->properties) = id;
	}
    
	return size * count;
}

/** This call is reentrant. Offset keeps track of how many bytes we've read. */
static size_t
read_response(char *input, size_t size, size_t count, void * arg)
{
    Response* response = arg;
    size_t offset = response->offset;
	unsigned char *image = *(response->image);
    size_t read = size * count;
    
    if (image == NULL) fatal_error("trying to read data into an unallocated class file\n");
    
    if (offset + read > (size_t) *(response->length)) {
        fatal_error("reading too much");
    }
    
	memcpy(&image[offset], input, read);
	response->offset += read;
    
	return read;
}

#define DATASET "Dataset: "
#define DATASET_LEN 9
#define HEADER_DATASET "Dataset: %s"

static size_t
read_dataset(char *input, size_t size, size_t count, void * arg)
{
    CommEnv env = (CommEnv) arg;
	if (strncasecmp(input, DATASET, DATASET_LEN) == 0) {
        OPT_INIT(env, env->dataset, HEADER_DATASET, &(input[DATASET_LEN]))
		char* end = strchr(env->dataset, '\r');
		if (end == 0) end = strchr(env->dataset, '\n');
		end[0] = 0;
	}
    
	return size * count;
}

void
comm_started(CommEnv env)
{
    post(env->start, HEADER(env->benchmark), NULL, 0, read_dataset, env, NULL, NULL);	
}

void
comm_stopped(CommEnv env)
{
    char lastId[255];
    struct curl_slist *headers = NULL;
    
    sprintf(lastId, "Last-Event: %ld", env->prev_id);
    ADD_HEADER(headers, env->dataset)
    ADD_HEADER(headers, lastId)
    
	post(env->stop, headers, NULL, 0, NULL, NULL, NULL, NULL);
}

#define mask32 0xFFFFFFFF
#define PUT_NETWORK_LONG(X, V) \
((uint32_t *)&(X))[0] = htonl(mask32 & (uint32_t)(V>>32));\
((uint32_t *)&(X))[1] = htonl(mask32 & (uint32_t)(V));

#define PUT_NETWORK_INT(X, V) \
X = htonl(V);

jlong
comm_log(CommEnv env, EventRecord *event)
{
    struct store *store;
    EventRecord *record;
    jlong id;
    size_t size, i, len;
    
    len = (size_t) event->args_len;
    size = sizeof(EventRecord) + len * sizeof(jlong);

    id = request_store(env, size, &store, &record);
    
    PUT_NETWORK_LONG(record->id, id);
    PUT_NETWORK_LONG(record->thread, event->thread);
    PUT_NETWORK_INT(record->type, event->type);
    PUT_NETWORK_INT(record->cid, event->cid);
    PUT_NETWORK_INT(record->attr.mid, event->attr.mid);
    PUT_NETWORK_INT(record->args_len, event->args_len);

    for (i = 0; i < len; i++) {
        PUT_NETWORK_LONG(record->args[i], event->args[i]);
    }
    
    release_store(env, store);
    
	return id;
}

void
comm_flush(CommEnv env)
{
    struct store *store;
    
    LOCK(env->jvmti, env->lock);
    
    store = env->store;
    env->store = create_store(env);
    
    if (store->using > 0) {
        store->flush.flush = JNI_TRUE;
        store = NULL;
        RELEASE(env->jvmti, env->lock);
    }
    else {
        RELEASE(env->jvmti, env->lock);
        destroy_store(env, store);
    }
}

void
comm_weave(CommEnv env,
           const char* classname, jboolean systemClass,
           jint class_len, const unsigned char* class_data,
           jint *new_class_len, unsigned char** new_class_data,
           jint* class_id, jint* class_properties)
{
	Response r;
    char *url;
    
	*new_class_len = 0;
	*new_class_data = NULL;
    
    r.jvmti = env->jvmti;
	r.length = new_class_len;
	r.image = new_class_data;
	r.class_id = class_id;
    r.properties = class_properties;
	r.offset = 0;
    
    OPT_INIT(env, url, env->weave, classname)
    
    post(url, HEADER(env->dataset), (void *) class_data, (size_t) class_len,
         read_header, &r,
         read_response, &r);
    
    deallocate(env->jvmti, url);
}

