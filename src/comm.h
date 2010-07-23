#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

JNIEXPORT void JNICALL init_comm();

JNIEXPORT void JNICALL weave_classfile(
		const char* classname, int systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint* new_class_data_len, unsigned char** new_class_data);

JNIEXPORT void JNICALL log_profiler_started();

JNIEXPORT void JNICALL log_profiler_stopped();

JNIEXPORT void JNICALL log_method_event(jlong thread, const char* message,
		jint cnum, jint mnum, jint len, jlong* params, int force_send);

JNIEXPORT void JNICALL flush_method_event_buffer();

#define MAX_PARAMETERS 16

struct EventRecord {
	int thread_upper;
	int thread_lower;
	char message[64];
	int cnum;
	int mnum;
	int len;
	int params[MAX_PARAMETERS * 2];
};

