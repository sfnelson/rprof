#ifndef RPROF_COMM_H
#define RPROF_COMM_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

#include "rprof_events.h"

void init_comm(jvmtiEnv *jvmti, char *options);

void weave_classfile(
		const char* classname, int systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint* new_class_data_len, unsigned char** new_class_data,
		jint* classId);

void log_profiler_started();
void log_profiler_stopped();

jlong log_event(jvmtiEnv *jvmti, r_event *record);
void flush_events(jvmtiEnv *jvmti);

#endif