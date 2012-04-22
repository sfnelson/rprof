#ifndef RPROF_H
#define RPROF_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <stdarg.h>
#include <stdint.h>

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>

/* heapTracker utility functions */
#include "agent_util.h"

/* Agent library externals to export. */

#define RPROF_CLASS_THREAD_ID (0x0000FFFF00000000ll)

#define TAG_TO_CNUM(X) ((jint) (X & 0xffffffffll))
#define CNUM_TO_TAG(X) ((jlong) (RPROF_CLASS_THREAD_ID | X))

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved);
JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm);

#endif

