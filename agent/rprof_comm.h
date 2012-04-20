#ifndef RPROF_COMM_H
#define RPROF_COMM_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

#include "rprof_events.h"

void comm_init(jvmtiEnv *jvmti, char *options);

void comm_weave(
		const char* classname, jboolean systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint* new_class_data_len, unsigned char** new_class_data,
		jint* classId);

void comm_started(jvmtiEnv *jvmti);
void comm_stopped(jvmtiEnv *jvmti);

jlong comm_log(jvmtiEnv *jvmti, r_event *event);
void comm_flush(jvmtiEnv *jvmti);

#endif
