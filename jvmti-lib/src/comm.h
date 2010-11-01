#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

JNIEXPORT void JNICALL init_comm(jvmtiEnv *jvmti);

JNIEXPORT void JNICALL weave_classfile(
		const char* classname, int systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint* new_class_data_len, unsigned char** new_class_data);

JNIEXPORT void JNICALL log_profiler_started();
JNIEXPORT void JNICALL log_profiler_stopped();

JNIEXPORT void JNICALL log_method_event(jlong thread,
		jint message, jint cnum, jint mnum, jint len, jlong* params);

JNIEXPORT void JNICALL flush_method_event_buffer();

#define MAX_PARAMETERS 16
