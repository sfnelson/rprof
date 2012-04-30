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
    size_t using;
    size_t size;
    size_t max;
    jboolean flush;
    EventRecord events[EVENT_BUFFER_SIZE];
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

static struct store *
create_store(CommEnv env)
{
    struct store *store;
    
    store = allocate(env->jvmti, sizeof(struct store));
    store->max = EVENT_BUFFER_SIZE;
    
    return store;
}

static void
destroy_store(CommEnv env, struct store *store)
{
    post(env->log, HEADER(env->dataset), store->events,
         store->size * sizeof(EventRecord),
         NULL, NULL, NULL, NULL);
    
    bzero(store, sizeof(struct store));
    deallocate(env->jvmti, store);
}

static jlong
request_store(CommEnv env, size_t toStore, struct store **store, EventRecord **record)
{
    struct store *current = NULL, *old = NULL;
    jlong id = 0;
    
    LOCK(env->jvmti, env->lock)
    
    current = env->store;
    
    if (current->size + toStore > current->max) {
        old = current;
        current = create_store(env);
        env->store = current;
    }
    
    if (toStore > 0) {
        (*record) = &(current->events[current->size]);
        current->size += toStore;
        
        id = env->prev_id + 1;
        env->prev_id += toStore;
    }
    
    (current->using)++;
    
    if (old != NULL && old->using > 0) {
        old->flush = JNI_TRUE;
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
    
    if (store->using == 1 && store->flush == JNI_TRUE) {
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
	bzero(env, sizeof(struct _CommEnv));
	
    env->jvmti = jvmti;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, "comm", &(env->lock));
    check_jvmti_error(jvmti, error, "Cannot create monitor");
    
    env->store = create_store(env);
    
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

static size_t
read_header(char *input, size_t size, size_t count, Response *response)
{
	jint len;
    jint id;
    
	if (strncasecmp(input, CONTENT_LENGTH, CONTENT_LENGTH_LEN) == 0) {
		len = (jint) strtol(&input[CONTENT_LENGTH_LEN], NULL, 0);
		*(response->length) = len;
		*(response->image) = allocate(response->jvmti, (size_t) len);
	}
    
	if (strncasecmp(input, CLASS_ID, CLASS_ID_LEN) == 0) {
		id = (jint) strtol(&input[CLASS_ID_LEN], NULL, 0);
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
read_dataset(char *input, size_t size, size_t count, CommEnv env)
{
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
    post(env->start, HEADER(env->benchmark), NULL, 0,
         (header_func) read_dataset, (void *) env,
         NULL, NULL);	
}

void
comm_stopped(CommEnv env)
{
    char lastId[255];
    struct curl_slist *headers;
    
    sprintf(lastId, "Last-Event: %ld", env->prev_id);
    headers = HEADER(env->dataset);
    ADD_HEADER(&headers, lastId);
    
	post(env->stop, headers, NULL, 0,
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
    struct store *store;
    
    LOCK(env->jvmti, env->lock);
    
    store = env->store;
    env->store = create_store(env);
    
    if (store->using > 0) {
        store->flush = JNI_TRUE;
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

