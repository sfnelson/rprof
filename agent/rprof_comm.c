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

#define EVENT_BUFFER_SIZE 121072

typedef struct {
    jvmtiEnv *jvmti;
	unsigned char **image;
	jint   *length;
	jint   *class_id;
	size_t  offset;
} Response;

typedef struct {
    int id_upper;
    int id_lower;
	int thread_upper;
	int thread_lower;
	int message;
	int cnum;
	int mnum;
	int len;
	int params[RPROF_MAX_PARAMETERS * 2];
} EventRecord;

struct store {
    volatile size_t using;
    size_t size;
    size_t max;
    jboolean flush;
    EventRecord events[EVENT_BUFFER_SIZE];
};

struct _CommEnv {
	jvmtiEnv      *jvmti;
	jrawMonitorID  lock;
    struct store *store;
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
(*X) = curl_slist_append(*X, Y);

typedef size_t (*header_func) (unsigned char*, size_t, size_t, void *);
typedef size_t (*data_func) (unsigned char*, size_t, size_t, void *);

static void
post(const char* dest, struct curl_slist *headers, void *data, size_t len,
     header_func cbHeader, void * header_param,
     data_func cbData, void * data_param)
{
    CURLcode err;
    long int status;
    CURL* curl = curl_easy_init();
    
    ADD_HEADERS(&headers);
    
    curl_easy_setopt(curl, CURLOPT_URL, dest);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    
    if (data != NULL) {
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, data);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, len);
    }
    if (cbHeader != NULL) {
        curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, cbHeader);
        curl_easy_setopt(curl, CURLOPT_WRITEHEADER, header_param);
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

static void
flush(CommEnv env, struct store *to_flush)
{
    if (to_flush == NULL) return;
    
    post(env->log, HEADER(env->dataset), &(to_flush->events),
         to_flush->size * sizeof(EventRecord),
         NULL, NULL, NULL, NULL);
    
    deallocate(env->jvmti, to_flush);
}

static jlong
request_store(CommEnv env, size_t toStore, struct store **store, EventRecord **record)
{
    struct store *to_flush = NULL;
    jlong id = 0;
    
    if (env == NULL) {
        fatal_error("NULL comm env!");
        return 0;
    }
    
    LOCK(env->jvmti, env->lock)
    
    (*store) = env->store;
    
    if ((*store)->size + toStore > (*store)->max) {
        to_flush = (*store);
        
        env->store = allocate(env->jvmti, sizeof(struct store));
        bzero(env->store, sizeof(struct store));
        (*store) = env->store;
        (*store)->max = EVENT_BUFFER_SIZE;
        
        if (to_flush->using != 0) {
            /* still in use, set flush signal and proceed */
            (*store)->flush = JNI_TRUE;
            to_flush = NULL;
        }
    }
    
    if (toStore > 0) {
        (*record) = &((*store)->events[(*store)->size]);
        (*store)->size += toStore;
        
        id = env->prev_id + 1;
        env->prev_id += toStore;
    }
    
    ((*store)->using)++;
    
    RELEASE(env->jvmti, env->lock)
    
    if (to_flush != NULL) {
        flush(env, to_flush);
    }
    
    return id;
}

static void
release_store(CommEnv env, struct store *store)
{
    LOCK(env->jvmti, env->lock)
    
    (store->using)--;
    
    if (store->using == 0 && store->flush == JNI_TRUE) {
        flush(env, store);
    }
    
    RELEASE(env->jvmti, env->lock)
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

CommEnv comm_create(jvmtiEnv *jvmti, char *options)
{
    CommEnv env;
	jvmtiError error;
	char *benchmark;
    
    env = allocate(jvmti, sizeof(struct _CommEnv));
	bzero(env, sizeof(struct _CommEnv));
	
    env->jvmti = jvmti;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, "comm", &(env->lock));
    check_jvmti_error(jvmti, error, "Cannot create monitor");

    env->store = allocate(jvmti, sizeof(struct store));
    bzero(env->store, sizeof(struct store));
    env->store->max = EVENT_BUFFER_SIZE;

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
#define CLASS_ID "Class-id: "

static size_t
read_header(char *input, size_t size, size_t count, Response *response)
{
	jint len;
    jint id;
    
	if (strcasestr(input, CONTENT_LENGTH) == input) {
		len = (jint) strtol(&input[strlen(CONTENT_LENGTH)], NULL, 0);
		*(response->length) = len;
		*(response->image) = allocate(response->jvmti, (size_t) len);
	}
    
	if (strcasestr(input, CLASS_ID) == input) {
		id = (jint) strtol(&input[strlen(CLASS_ID)], NULL, 0);
	    *(response->class_id) = id;
	}
    
	return size * count;
}

/** This call is reentrant. Offset keeps track of how many bytes we've read. */
static size_t
read_response(void *input, size_t size, size_t count, Response* response)
{
    size_t offset = response->offset;
	unsigned char *image = *(response->image);
    size_t read = size * count;
    
	memcpy(&image[offset], input, read);
	response->offset += read;
    
	return read;
}

#define DATASET "Dataset: "
#define HEADER_DATASET "Dataset: %s"

static size_t
read_dataset(char *input, size_t size, size_t count, CommEnv env)
{
	if (strcasestr(input, DATASET) == input) {
        OPT_INIT(env, env->dataset, HEADER_DATASET, &(input[strlen(DATASET)]))
		char* end = strchr(env->dataset, '\r');
		if (end == 0) end = strchr(env->dataset, '\n');
		end[0] = 0;
	}
    
	return size * count;
}

void
comm_started(CommEnv env)
{
    post(env->start, HEADER(env->benchmark), NULL, 0,
         (header_func) read_dataset, (void *) env,
         NULL, NULL);	
}

void
comm_stopped(CommEnv env)
{
	post(env->stop, HEADER(env->dataset), NULL, 0,
         NULL, NULL, NULL, NULL);
}

#define untohl(X) (htonl((uint32_t)((X >> 32) & 0xFFFFFFFF)))
#define lntohl(X) (htonl((uint32_t)(X & 0xFFFFFFFF)))

jlong
comm_log(CommEnv env, r_event *event)
{
    struct store *store;
    EventRecord *record;
    jlong id;
    jint i;
    
    id = request_store(env, 1, &store, &record);
    
    record->id_upper =  untohl(id);
    record->id_lower = lntohl(id);
    record->thread_upper = untohl(event->thread);
    record->thread_lower = lntohl(event->thread);
    record->message = htonl(event->type);
    record->cnum = htonl(event->cid);
    record->mnum = htonl(event->attr.mid);
    record->len = htonl(event->args_len);
    for (i = 0; i < event->args_len; i++) {
        record->params[i * 2] = untohl(event->args[i]);
        record->params[i * 2 + 1] = lntohl(event->args[i]);
    }
    
    release_store(env, store);
    
	return id;
}

void
comm_flush(CommEnv env)
{
    struct store *store, *new_store;
    
    request_store(env, 0, &store, NULL);
    
    new_store = allocate(env->jvmti, sizeof(struct store));
    bzero(new_store, sizeof(struct store));
    new_store->max = EVENT_BUFFER_SIZE;
    
    env->store = new_store;
    store->flush = JNI_TRUE;
    
    release_store(env, store);
}

void
comm_weave(CommEnv env,
           const char* classname, jboolean systemClass,
           jint class_len, const unsigned char* class_data,
           jint *new_class_len, unsigned char** new_class_data,
           jint* class_id)
{
	Response r;
    char *url;

	*new_class_len = 0;
	*new_class_data = NULL;
    
    r.jvmti = env->jvmti;
	r.length = new_class_len;
	r.image = new_class_data;
	r.class_id = class_id;
	r.offset = 0;
    
    OPT_INIT(env, url, env->weave, classname)
    
    post(url, HEADER(env->dataset), (void *) class_data, (size_t) class_len,
         (header_func) &read_header, &r,
         (data_func) &read_response, &r);
}

