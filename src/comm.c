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

JNIEXPORT void JNICALL init_comm()
{
	curl_global_init(CURL_GLOBAL_ALL);
}

struct response {
	jint* length;
	unsigned char** image;
	unsigned int offset;
};

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

JNIEXPORT void JNICALL log_profiler_started()
{
	CURL* handle = curl_easy_init();
	char* host = "http://localhost:8888/start";
	long status;

	curl_easy_setopt(handle, CURLOPT_URL, host);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	stdout_message("profiler started.\n");

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error sending message! %d: %s\n", err, curl_easy_strerror(err));
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending message! HTTP %ld\n", status);
	}

	curl_slist_free_all(headers); /* free the header list */
}

JNIEXPORT void JNICALL log_profiler_stopped()
{
	CURL* handle = curl_easy_init();
	char* host = "http://localhost:8888/stop";
	long status;

	curl_easy_setopt(handle, CURLOPT_URL, host);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	stdout_message("profiler started.\n");

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error sending message! %d: %s\n", err, curl_easy_strerror(err));
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending message! HTTP %ld\n", status);
	}

	curl_slist_free_all(headers); /* free the header list */
}

#define EVENT_BUFFER_SIZE 64

struct EventRecord record_buffer[EVENT_BUFFER_SIZE];
int event_index = 0;

JNIEXPORT void JNICALL log_method_event(jlong thread, const char* message,
		jint cnum, jint mnum, jint len, jlong* params, int force_send)
{
	int i;

	if (len > MAX_PARAMETERS) {
		fatal_error("max method parameters exceeded! %d.%d %d > %d\n", cnum, mnum, len, MAX_PARAMETERS);
	}

	struct EventRecord* record = &(record_buffer[event_index++]);

	memset(record, 0, sizeof(record));
	record->thread_upper = htonl((thread >> 32) & 0xffffffff);
	record->thread_lower = htonl(thread & 0xffffffff);
	strcpy(&record->message, message);
	record->cnum = htonl(cnum);
	record->mnum = htonl(mnum);
	record->len = htonl(len);
	for (i = 0; i < len; i++) {
		record->params[i * 2] = htonl((params[i] >> 32) & 0xffffffff);
		record->params[i * 2 + 1] = htonl(params[i] & 0xffffffff);
	}

	if (force_send || event_index >= EVENT_BUFFER_SIZE) {
		flush_method_event_buffer();
	}
}

JNIEXPORT void JNICALL flush_method_event_buffer()
{
	CURL* handle = curl_easy_init();
	char* host = "http://localhost:8888/logger";
	long status;

	curl_easy_setopt(handle, CURLOPT_URL, host);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	/* post binary data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDS, &record_buffer);

	/* set the size of the postfields data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDSIZE, sizeof(record_buffer[0]) * event_index);

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error sending log! %d: %s\n", err, curl_easy_strerror(err));
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error sending log! HTTP %ld\n", status);
	}

	curl_slist_free_all(headers); /* free the header list */

	event_index = 0;
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
	char* host = "http://localhost:8888/weaver?";
	long status;

	char name[strlen(classname) + strlen(host) + 1];
	sprintf(name, "%s%s", host, classname);

	curl_easy_setopt(handle, CURLOPT_URL, name);
	curl_easy_setopt(handle, CURLOPT_HEADERFUNCTION, read_header);
	curl_easy_setopt(handle, CURLOPT_WRITEHEADER, &r);
	curl_easy_setopt(handle, CURLOPT_WRITEFUNCTION, read_response);
	curl_easy_setopt(handle, CURLOPT_WRITEDATA, &r);

	struct curl_slist *headers=NULL;
	headers = curl_slist_append(headers, "Content-Type: application/rprof");

	/* post binary data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDS, class_data);

	/* set the size of the postfields data */
	curl_easy_setopt(handle, CURLOPT_POSTFIELDSIZE, class_data_len);

	curl_easy_setopt(handle, CURLOPT_HTTPHEADER, headers);

	//stdout_message("sending %d bytes to weaver\n", class_data_len);

	CURLcode err = curl_easy_perform(handle); /* post away! */

	if (err != 0) {
		fatal_error("error weaving file! %d: %s\n", err, curl_easy_strerror(err));
	}

	curl_easy_getinfo(handle, CURLINFO_RESPONSE_CODE, &status);
	if (status/100 != 2) {
		fatal_error("error weaving file! HTTP %ld\n", status);
	}

	curl_slist_free_all(headers); /* free the header list */
}

