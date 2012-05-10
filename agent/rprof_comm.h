#ifndef RPROF_COMM_H
#define RPROF_COMM_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

#include "rprof_events.h"

struct _CommEnv;
typedef struct _CommEnv *CommEnv;

CommEnv
comm_create(jvmtiEnv *jvmti, char *options);

void
comm_weave(CommEnv env,
		const char* classname, jboolean systemClass,
		jint class_data_len, const unsigned char* class_data,
		jint *new_class_len, unsigned char** new_class_data,
		jint *class_id, jint *class_properties);

void
comm_started(CommEnv env);

void
comm_stopped(CommEnv env);

jlong
comm_log(CommEnv env, EventRecord *event);

void
comm_flush(CommEnv env);

#endif
