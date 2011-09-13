#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <curl/curl.h>

#include <sys/types.h>
#include "jni.h"
#include "jvmti.h"

#include "comm.h"
#include "agent_util.h"
#include "rprof.h"

#define EVENT_BUFFER_SIZE 65536
#define HOST_MAX_LENGTH 256
#define DATASET_MAX_LENGTH 256

struct response {
	jint* length;
	unsigned char** image;
	unsigned int offset;
};

typedef struct {
    int id_upper;
    int id_lower;
	int thread_upper;
	int thread_lower;
	int message;
	int cnum;
	int mnum;
	int len;
	int params[MAX_PARAMETERS * 2];
} EventRecord;

typedef struct {
	jvmtiEnv      *jvmti;
	jrawMonitorID  lock;
	EventRecord    records[EVENT_BUFFER_SIZE];
	unsigned int   event_index;
	char		   host[HOST_MAX_LENGTH];
	char           dataset[DATASET_MAX_LENGTH];
	long int       lastId;
} GlobalCommData;

static GlobalCommData *cdata;
static char *host;

/* Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void
enterCriticalSection()
{
	jvmtiEnv *jvmti = cdata->jvmti;
	jvmtiError error;

	error = (*jvmti)->RawMonitorEnter(jvmti, cdata->lock);
	check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/* Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void
exitCriticalSection()
{
	jvmtiEnv *jvmti = cdata->jvmti;
	jvmtiError error;

	error = (*jvmti)->RawMonitorExit(jvmti, cdata->lock);
	check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

JNIEXPORT void JNICALL init_comm(jvmtiEnv *jvmti, char *options)
{
	static GlobalCommData data;
	jvmtiError error;

	(void)memset((void*)&data, 0, sizeof(data));
	cdata = &data;
	
	if (0 == options || 0 == strlen(options))
	{
		strcpy(cdata->host, "localhost:8888");
	}
	else {
		strcpy(cdata->host, options);
	}

	cdata->jvmti = jvmti;

	error = (*jvmti)->CreateRawMonitor(jvmti, "comm data", &(cdata->lock));
	check_jvmti_error(jvmti, error, "Cannot create raw monitor");

	curl_global_init(CURL_GLOBAL_ALL);
}

size_t read_header(void *ptr, size_t size, size_t nmemb, struct response* response) {
	unsigned int len, i;

	char header[size * nmemb + 1];
	for (i = 0; i < size * nmemb; i++) {
		header[i] = tolower(((char*)ptr)[i]);
	}
	header[size * nmemb] = 0;

	if (strstr(header, "content-length:") == header) {
		int len = atoi(&header[16]);
		*response->length = len;
		*response->image = malloc(len * sizeof(unsigned char));
	}

	return size * nmemb;
}

size_t read_response(void *buffer, size_t size, size_t nmemb, struct response* response) {
	unsigned char* image = *response->image;
	unsigned int offset = response->offset;
	memcpy(&image[offset], buffer, size * nmemb);
	offset += size * nmemb;
	response->offset = offset;

	//stdout_message("received %d bytes from weaver (offset %d)\n", size * nmemb, offset);

	return size * nmemb;
}

size_t read_dataset(void *ptr, size_t size, size_t nmemb, void* args) {
	unsigned int len, i;
	
	char header[size * nmemb + 1];
	for (i = 0; i < size * nmemb; i++) {
		header[i] = tolower(((char*)ptr)[i]);
	}
	header[size * nmemb] = 0;

	if (strstr(header, "dataset:") == header) {
		sprintf(cdata->dataset, "Dataset: %s", &header[9]);
		cdata->dataset[9 + 14] = 0;
	}

	return size * nmemb;
}

JNIEXPORT void JNICALL log_profiler_started()
{
	CURL* handle = curl_easy_init();
	char host [HOST_MAX_LENGTH];
	long status;
	
	sprintf(host, "http://%s/start", cdata->host);

	curl_easy_setopt(handle, CURLOPT_URL, host);
	curl_easy_setopt(handle, CURLOPT_HEADERFUNCTION, read_dataset);
	curl_easy_setopt(handle, CURLOPT_WRITEHEADER, NULL);
	
	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	// stdout_message("profiler started.\n");

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error sending message! %d: %s (%s)\n", err, curl_easy_strerror(err), host);
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending message! HTTP %ld (%s)\n", status, host);
	}

	curl_slist_free_all(headers); /* free the header list */
	curl_easy_cleanup(handle);
}

JNIEXPORT void JNICALL log_profiler_stopped()
{
	CURL* handle = curl_easy_init();
	char host [HOST_MAX_LENGTH];
	long status;
	
	sprintf(host, "http://%s/stop", cdata->host);

	curl_easy_setopt(handle, CURLOPT_URL, host);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, cdata->dataset);
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	// stdout_message("profiler stopped.\n");

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error sending message! %d: %s (%s)\n", err, curl_easy_strerror(err), host);
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending message! HTTP %ld (%s)\n", status, host);
	}

	curl_slist_free_all(headers); /* free the header list */
	curl_easy_cleanup(handle);
}

JNIEXPORT void JNICALL log_method_event(jlong thread, jint message,
		jint cnum, jint mnum, jint len, jlong* params)
{
	int i;
	long int id;
	EventRecord* record;

	if (len > MAX_PARAMETERS) {
		fatal_error("max method parameters exceeded! %d.%d %d > %d\n", cnum, mnum, len, MAX_PARAMETERS);
	}

	enterCriticalSection(); {
		record = &(cdata->records[cdata->event_index++]);
		memset(record, 0, sizeof(record));

        id = ++(cdata->lastId);
        record->id_upper = htonl((id >> 32) & 0xffffffff);
		record->id_lower = htonl(id & 0xffffffff);
		record->thread_upper = htonl((thread >> 32) & 0xffffffff);
		record->thread_lower = htonl(thread & 0xffffffff);
		record->message = htonl(message);
		record->cnum = htonl(cnum);
		record->mnum = htonl(mnum);
		record->len = htonl(len);
		for (i = 0; i < len; i++) {
			record->params[i * 2] = htonl((params[i] >> 32) & 0xffffffff);
			record->params[i * 2 + 1] = htonl(params[i] & 0xffffffff);
		}

		if (cdata->event_index >= EVENT_BUFFER_SIZE) {
			flush_method_event_buffer();
		}
	}; exitCriticalSection();
}

JNIEXPORT void JNICALL flush_method_event_buffer()
{
	CURL* handle = curl_easy_init();
	char host [HOST_MAX_LENGTH];
	long status;
	CURLcode err;
	
	sprintf(host, "http://%s/logger", cdata->host);

	curl_easy_setopt(handle, CURLOPT_URL, host);
	curl_easy_setopt(handle, CURLOPT_TIMEOUT, 600);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, cdata->dataset);
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	enterCriticalSection(); {
		/* post binary data */
		curl_easy_setopt(handle, CURLOPT_POSTFIELDS, &(cdata->records));

		/* set the size of the postfields data */
		curl_easy_setopt(handle, CURLOPT_POSTFIELDSIZE, sizeof(EventRecord) * cdata->event_index);

		curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

		err = curl_easy_perform(handle); /* post away! */

		cdata->event_index = 0;
	}; exitCriticalSection();

	if (err != 0) {
		fatal_error("error sending log! %d: %s (%s)\n", err, curl_easy_strerror(err), host);
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending log! HTTP %ld (%s)\n", status, host);
	}

	curl_slist_free_all(headers); /* free the header list */
	curl_easy_cleanup(handle);
}

JNIEXPORT void JNICALL weave_classfile(
		const char* classname, int systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint* newLength, unsigned char** newImage)
{
	*newLength = 0;
	*newImage = NULL;

	struct response r;
	r.length = newLength;
	r.image = newImage;
	r.offset = 0;
	
	CURL* handle = curl_easy_init();
	char host [HOST_MAX_LENGTH];
	long status;
	
	sprintf(host, "http://%s/weaver?", cdata->host);

	char name[strlen(classname) + strlen(host) + 1];
	sprintf(name, "%s%s", host, classname);

	curl_easy_setopt(handle, CURLOPT_URL, name);
	curl_easy_setopt(handle, CURLOPT_HEADERFUNCTION, read_header);
	curl_easy_setopt(handle, CURLOPT_WRITEHEADER, &r);
	curl_easy_setopt(handle, CURLOPT_WRITEFUNCTION, read_response);
	curl_easy_setopt(handle, CURLOPT_WRITEDATA, &r);
	curl_easy_setopt(handle, CURLOPT_TIMEOUT, 300);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, cdata->dataset);
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	/* post binary data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDS, class_data);

	/* set the size of the postfields data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDSIZE, class_data_len);

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	// stdout_message("sending %d bytes to weaver\n", class_data_len);

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error weaving file! %d: %s (%s)\n", err, curl_easy_strerror(err), host);
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error weaving file! HTTP %ld (%s)\n", status, name);
	}

	curl_slist_free_all(headers); /* free the header list */
	curl_easy_cleanup(handle);
}

