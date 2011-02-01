/*
 * @(#)heapTracker.c    1.21 06/02/16
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

#include "stdlib.h"
#include "string.h"
#include "errno.h"

#include "rprof.h"
#include "java_crw_demo.h"

#include "jni.h"
#include "jvmti.h"

#include "agent_util.h"
#include "comm.h"

/* -------------------------------------------------------------------
 * Some constant names that tie to Java class/method names.
 *    We assume the Java class whose static methods we will be calling
 *    looks like:
 *
 * public class HeapTracker {
 *     private static int engaged;
 *     private static native void _newobj(...);
 *     public static void newobj(Object o) {...}
 *     private static native void _newarr(...);
 *     public static void newarr(Object a) {...}
 * }
 *
 *    The engaged field allows us to inject all classes (even system classes)
 *    and delay the actual calls to the native code until the VM has reached
 *    a safe time to call native methods (Past the JVMTI VM_START event).
 *
 */

#define HEAP_TRACKER_package		nz/ac/vuw/ecs/rprof /* Package containing the class */
#define HEAP_TRACKER_class			HeapTracker /* Name of class we are using */
#define HEAP_TRACKER_newobj			newobj		/* Name of java init method */
#define HEAP_TRACKER_newarr			newarr		/* Name of java newarray method */
#define HEAP_TRACKER_native_newobj	_newobj		/* Name of java newobj native */
#define HEAP_TRACKER_native_newarr	_newarr		/* Name of java newarray native */
#define HEAP_TRACKER_newcls			newcls
#define HEAP_TRACKER_native_newcls	_newcls
#define HEAP_TRACKER_enter			enter		/* Name of java method execution method */
#define HEAP_TRACKER_exit			exit		/* Name of java method return method */
#define HEAP_TRACKER_except			except
#define HEAP_TRACKER_native_enter	_menter		/* Name of java method execution native */
#define HEAP_TRACKER_native_exit	_mexit		/* Name of java method return native */
#define HEAP_TRACKER_native_except  _mexcept
#define HEAP_TRACKER_engaged		engaged		/* Name of static field switch */
#define HEAP_TRACKER_main			main		/* Name of java main method tracker */
#define HEAP_TRACKER_native_main	_main		/* Name of java main method tracker native */

/* C macros to create strings from tokens */
#define _STRING(s) #s
#define STRING(s) _STRING(s)

/* ------------------------------------------------------------------- */

/* Global agent data structure */

typedef struct {
	jfieldID field;
	jclass cls;
	jint cnum;
	jint fnum;
} rField;

typedef struct {
	/* JVMTI Environment */
	jvmtiEnv      *jvmti;
	/* State of the VM flags */
	jboolean       vmStarted;
	jboolean       vmInitialized;
	jboolean       vmDead;
	/* Options */
	int            maxDump;
	/* Data access Lock */
	jrawMonitorID  lock;

	/* Counter for the number of untagged objects which have been tagged natively */
	jlong		   nullCounter;

	int			   mapSize;
	int			   mapLength;
	rField*		   fieldMap;
} GlobalAgentData;

static GlobalAgentData *gdata;

/* Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void
enterCriticalSection(jvmtiEnv *jvmti)
{
	jvmtiError error;

	error = (*jvmti)->RawMonitorEnter(jvmti, gdata->lock);
	check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/* Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void
exitCriticalSection(jvmtiEnv *jvmti)
{
	jvmtiError error;

	error = (*jvmti)->RawMonitorExit(jvmti, gdata->lock);
	check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

/* Method to get a new object id */
static jlong getNewObjectId() {
	// Because this is called from within cbObjectTagger we can't call JNI functions.

	jlong id = gdata->nullCounter;
	id++;
	gdata->nullCounter = id;

	return id;
}

#define RPROF_DEBUG 0

/* Method to log an event */
static void
logEvent(JNIEnv *env, jvmtiEnv *jvmti, jthread thread, jobject o, const char* format)
{
	jvmtiError error;
	jlong id;
	char* signature;
	jclass cls;

	error = (*jvmti)->GetTag(jvmti, o, &id);
	check_jvmti_error(jvmti, error, "Cannot read object tag");

	cls = (*env)->GetObjectClass(env, o);
	error = (*jvmti)->GetClassSignature(jvmti, cls, &signature, NULL);
	check_jvmti_error(jvmti, error, "Cannot get class signature");

	stdout_message(format, id, signature);
}

static int hashField(rField *map, int len, jfieldID field) {
	int index = 0;
	int key = ((long unsigned int) field) % len;
	int i = 0;
	while (i < len) {
		index = ((int)(key + i/2.0f + i*i/2.0f)) % len;
		if (map[index].field == NULL) return index;
		if (map[index].field == field) return index;
		i++;
	}
	fatal_error("ERROR: probed all hash table entries without finding entry\n");
	return -1;
}

static rField
getField(rField* map, int len, jfieldID field) {
	return map[hashField(map, len, field)];
}

static void
storeFields(rField *entries, int num) {
	jvmtiEnv *jvmti;
	jvmti = gdata->jvmti;

	enterCriticalSection(jvmti); {
		rField *oldMap;
		int oldLen, size, len, i, index;
		rField *map;
		rField tmp;
		jvmtiError error;

		oldMap = gdata->fieldMap;
		oldLen = gdata->mapLength;
		if (oldMap != NULL) {
			size = gdata->mapSize + num;
			len = gdata->mapLength;
		}
		else {
			size = num;
			len = 1;
			oldLen = 0;
		}

		while (size > 0.8f * len) {
			len = len * 2;
		}

		error = (*jvmti)->Allocate(jvmti, sizeof(rField) * len, (unsigned char**)&map);
		check_jvmti_error(jvmti, error, "Cannot allocate memory for field table");

		(void)memset((void*)map, 0, sizeof(rField) * len);

		for (i = 0; i < oldLen; i++) {
			tmp = oldMap[i];
			if (tmp.field != NULL) {
				index = hashField(map, len, tmp.field);
				map[index] = tmp;
			}
		}

		for (i = 0; i < num; i++) {
			tmp = entries[i];
			index = hashField(map, len, tmp.field);
			map[index] = tmp;
		}

		gdata->fieldMap = map;
		gdata->mapSize = size;
		gdata->mapLength = len;

		if (oldMap != NULL) {
			(*jvmti)->Deallocate(jvmti, (unsigned char*) oldMap);
		}

	} exitCriticalSection(jvmti);
}

static void
watch_fields(jvmtiEnv *jvmti, rField* fields, int len)
{
	int i;
	rField field;
	jvmtiError error;

	for (i = 0; i < len; i++) {
		field = fields[i];
		if (field.field == NULL) continue;
		error = (*jvmti)->SetFieldAccessWatch(jvmti, field.cls, field.field);
		check_jvmti_error(jvmti, error, "Cannot add access watch to field");
		error = (*jvmti)->SetFieldModificationWatch(jvmti, field.cls, field.field);
		check_jvmti_error(jvmti, error, "Cannot add modify watch to field");
	}
}

/* Java Native Method for <clinit> */
static void
HEAP_TRACKER_native_newcls(JNIEnv *env, jclass klass, jobject cls, jint cnum, jintArray fieldsToWatch)
{
	jvmtiError error;
	jvmtiEnv *jvmti;
	jlong tag;
	jint numFields;
	jint* targetFields;
	jfieldID* fields;
	int i;
	rField* fieldsToStore;
	rField field;

	if ( gdata->vmDead ) {
		return;
	}

	if (RPROF_DEBUG) stdout_message("----> newcls (%d)\n", cnum);

	jvmti = gdata->jvmti;

	tag = RPROF_MAGIC | cnum;

	error = (*jvmti)->SetTag(jvmti, cls, tag);
	check_jvmti_error(jvmti, error, "Cannot tag class with id");

	log_method_event(0, RPROF_CLASS_INITIALIZED, cnum, 0, 1, &tag);

	if (fieldsToWatch != NULL) {
		jsize len = (*env)->GetArrayLength(env, fieldsToWatch);
		fieldsToStore = malloc(sizeof(rField) * len);
		targetFields = (*env)->GetIntArrayElements(env, fieldsToWatch, NULL);
		(*jvmti)->GetClassFields(jvmti, cls, &numFields, &fields);
		for (i = 0; i < len; i++) {
			field.cnum = cnum;
			field.fnum = targetFields[i];
			field.cls = (*env)->NewGlobalRef(env, cls);
			field.field = fields[targetFields[i] - 1]; // Fields begin at 1 in our profiler and 0 in jvmti
			fieldsToStore[i] = field;
		}
		storeFields(fieldsToStore, len); // always store fields, they're looked up later
		if (gdata->vmInitialized == JNI_TRUE) {
			watch_fields(jvmti, fieldsToStore, len);
		}
		(*jvmti)->Deallocate(jvmti, (unsigned char*) fields);
		(*env)->ReleaseIntArrayElements(env, fieldsToWatch, targetFields, JNI_ABORT);
	}

	if (RPROF_DEBUG) stdout_message("<---- newcls (%d)\n", cnum);
}

/* Java Native Method for Object.<init> */
static void
HEAP_TRACKER_native_newobj(JNIEnv *env, jclass klass, jthread thread, jobject o, jlong id)
{
	jvmtiError error;
	jvmtiEnv *jvmti;
	jclass cls;
	jlong threadId = 0;

	if ( gdata->vmDead ) {
		return;
	}

	if (id == 0) {
		id = getNewObjectId();
	}

	jvmti = gdata->jvmti;

	error = (*jvmti)->SetTag(jvmti, o, id);
	check_jvmti_error(jvmti, error, "Cannot tag object with id");

	if (thread != NULL) {
		error = (*jvmti)->GetTag(jvmti, thread, &threadId);
		check_jvmti_error(jvmti, error, "Cannot read tag");
	}

	log_method_event(threadId, RPROF_OBJECT_ALLOCATED, 0, 0, 1, &id);
}

/* Java Native Method for newarray */
static void
HEAP_TRACKER_native_newarr(JNIEnv *env, jclass klass, jthread thread, jobject a, jlong id)
{
	jvmtiError error;
	jvmtiEnv *jvmti;
	jlong threadId;

	if ( gdata->vmDead ) {
		return;
	}

	jvmti = gdata->jvmti;

	error = (*jvmti)->SetTag(jvmti, a, id);
	check_jvmti_error(jvmti, error, "Cannot tag array with id");

	error = (*jvmti)->GetTag(jvmti, thread, &threadId);
	check_jvmti_error(jvmti, error, "Cannot read tag");

	log_method_event(threadId, RPROF_ARRAY_ALLOCATED, 0, 0, 1, &id);
}

/* Java Native Method for method execution */
static void
HEAP_TRACKER_native_enter(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jarray args)
{
	if (gdata->vmInitialized) {
		jvmtiError error;
		jvmtiEnv *jvmti;
		jclass cls;
		jlong threadId;
		int i;

		if ( gdata->vmDead ) {
			return;
		}

		jvmti = gdata->jvmti;

		if (thread != NULL) {
			error = (*jvmti)->GetTag(jvmti, thread, &threadId);
			check_jvmti_error(jvmti, error, "Cannot read thread tag");
		} else {
			threadId = 0;
		}

		if (args != NULL) {
			jsize len = (*env)->GetArrayLength(env, args);
			jlong tags[len];
			for (i = 0; i < len; i++) {
				jobject o = (*env)->GetObjectArrayElement(env, args, i);
				if (o != NULL) {
					error = (*jvmti)->GetTag(jvmti, o, &tags[i]);
					check_jvmti_error(jvmti, error, "Cannot read object tag");
				} else {
					tags[i] = 0;
				}
			}

			log_method_event(threadId, RPROF_METHOD_ENTER, cnum, mnum, len, tags);
		}
		else {
			log_method_event(threadId, RPROF_METHOD_ENTER, cnum, mnum, 0, NULL);
		}

	}
}

/* Java Native Method for method return */
static void
HEAP_TRACKER_native_exit(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jobject arg)
{
	if (gdata->vmInitialized) {
		jvmtiError error;
		jvmtiEnv *jvmti;
		char* signature;
		jclass cls;

		if ( gdata->vmDead ) {
			return;
		}

		jvmti = gdata->jvmti;

		jlong threadId;
		jlong argId;

		jint numArgs = 0;
		jlong* args = NULL;

		error = (*jvmti)->GetTag(jvmti, thread, &threadId);
		check_jvmti_error(jvmti, error, "Cannot read thread tag");

		if (arg != NULL) {
			error = (*jvmti)->GetTag(jvmti, arg, &argId);
			check_jvmti_error(jvmti, error, "Cannot read object tag");

			numArgs = 1;
			args = &argId;
		}

		log_method_event(threadId, RPROF_METHOD_RETURN, cnum, mnum, numArgs, args);
	}
}

/* Java Native Method for method exceptional return */
static void
HEAP_TRACKER_native_except(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jobject thrown)
{
	if (gdata->vmInitialized) {
		jvmtiError error;
		jvmtiEnv *jvmti;
		char* signature;
		jclass cls;

		if ( gdata->vmDead ) {
			return;
		}

		jvmti = gdata->jvmti;

		jlong threadId;
		jlong argId;

		jint numArgs = 0;
		jlong* args = NULL;

		error = (*jvmti)->GetTag(jvmti, thread, &threadId);
		check_jvmti_error(jvmti, error, "Cannot read thread tag");

		if (thrown != NULL) {
			error = (*jvmti)->GetTag(jvmti, thrown, &argId);
			check_jvmti_error(jvmti, error, "Cannot read object tag");

			numArgs = 1;
			args = &argId;
		}

		log_method_event(threadId, RPROF_METHOD_EXCEPTION, cnum, mnum, numArgs, args);
	}
}

static void
HEAP_TRACKER_native_main(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum)
{
	if (gdata->vmInitialized) {
		jvmtiError error;
		jvmtiEnv *jvmti;
		jclass cls;

		if ( gdata->vmDead ) {
			return;
		}

		jvmti = gdata->jvmti;

		jlong threadId;

		error = (*jvmti)->GetTag(jvmti, thread, &threadId);
		check_jvmti_error(jvmti, error, "Cannot read tag");

		log_method_event(threadId, RPROF_METHOD_ENTER, cnum, mnum, 0, NULL);
	}
}

/* Callback for JVMTI_EVENT_VM_START */
static void JNICALL
cbVMStart(jvmtiEnv *jvmti, JNIEnv *env)
{
	enterCriticalSection(jvmti); {
		jclass klass;
		jfieldID field;
		jint rc;

		/* Java Native Methods for class */
		static JNINativeMethod registry[7] = {
				{STRING(HEAP_TRACKER_native_newobj), "(Ljava/lang/Object;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newobj},
				{STRING(HEAP_TRACKER_native_newarr), "(Ljava/lang/Object;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newarr},
				{STRING(HEAP_TRACKER_native_enter), "(Ljava/lang/Object;II[Ljava/lang/Object;)V", (void*)&HEAP_TRACKER_native_enter},
				{STRING(HEAP_TRACKER_native_exit), "(Ljava/lang/Object;IILjava/lang/Object;)V", (void*)&HEAP_TRACKER_native_exit},
				{STRING(HEAP_TRACKER_native_except), "(Ljava/lang/Object;IILjava/lang/Object;)V", (void*)&HEAP_TRACKER_native_except},
				{STRING(HEAP_TRACKER_native_main), "(Ljava/lang/Object;II)V", (void*)&HEAP_TRACKER_native_main},
				{STRING(HEAP_TRACKER_native_newcls), "(Ljava/lang/Object;I[I)V", (void*)&HEAP_TRACKER_native_newcls}
		};

		/* Register Natives for class whose methods we use */
		klass = (*env)->FindClass(env, STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		if ( klass == NULL ) {
			fatal_error("ERROR: JNI: Cannot find %s with FindClass\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}

		rc = (*env)->RegisterNatives(env, klass, registry, 7);
		if ( rc != 0 ) {
			fatal_error("ERROR: JNI: Cannot register natives for class %s\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}

		/* Engage calls. */
		field = (*env)->GetStaticFieldID(env, klass, STRING(HEAP_TRACKER_engaged), "I");
		if ( field == NULL ) {
			fatal_error("ERROR: JNI: Cannot get field from %s\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
		(*env)->SetStaticIntField(env, klass, field, 1);

		/* Indicate VM has started */
		gdata->vmStarted = JNI_TRUE;

	} exitCriticalSection(jvmti);
}

/* Iterate Through Heap callback */
static jint JNICALL
cbObjectTagger(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* userData)
{
	jint cnum;

	if (length != -1) {
		// TODO: stop ignoring arrays!
		return JVMTI_VISIT_OBJECTS;
	}

	if (*tag_ptr == 0) {
		*tag_ptr = getNewObjectId();
	}

	cnum = (jint)(class_tag & 0xFFFFFFFFll);
	log_method_event(0, RPROF_OBJECT_TAGGED, cnum, 0, 1, tag_ptr);

	return JVMTI_VISIT_OBJECTS;
}

/* Callback for JVMTI_EVENT_VM_INIT */
static void JNICALL
cbVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;

	if (RPROF_DEBUG) stdout_message("----- trying gc at init\n");
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");
	if (RPROF_DEBUG) stdout_message("----- gc ok on init\n");

	// stdout_message("-------- tagging existing objects\n");

	jclass object_class = (*env)->FindClass(env, "java/lang/Object");
	error = (*jvmti)->SetTag(jvmti, object_class, RPROF_MAGIC | 1ll);
	check_jvmti_error(jvmti, error, "Cannot set tag on Object.class");

	/* Iterate through heap, find all untagged objects allocated before this */
	(void)memset(&heapCallbacks, 0, sizeof(heapCallbacks));
	heapCallbacks.heap_iteration_callback = &cbObjectTagger;
	error = (*jvmti)->IterateThroughHeap(jvmti, 0, //JVMTI_HEAP_FILTER_TAGGED,
			NULL, &heapCallbacks, NULL);
	check_jvmti_error(jvmti, error, "Cannot iterate through heap");

	// stdout_message("-------- done tagging existing objects\n");

	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_FIELD_MODIFICATION, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_FIELD_ACCESS, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");

	enterCriticalSection(jvmti); {

		/* Indicate VM is initialized */
		gdata->vmInitialized = JNI_TRUE;

		watch_fields(jvmti, gdata->fieldMap, gdata->mapLength);

	} exitCriticalSection(jvmti);
}

/* Callback for JVMTI_EVENT_VM_DEATH */
static void JNICALL
cbVMDeath(jvmtiEnv *jvmti, JNIEnv *env)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;

	flush_method_event_buffer(jvmti);

	if (RPROF_DEBUG) stdout_message("----- vm death\n");

	/* These are purposely done outside the critical section */

	/* Force garbage collection now so we get our ObjectFree calls */
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");

	if (RPROF_DEBUG) stdout_message("----- gc finished\n");

	/* Iterate through heap and find all objects */
	//(void)memset(&heapCallbacks, 0, sizeof(heapCallbacks));
	//heapCallbacks.heap_iteration_callback = &cbObjectSpaceCounter;
	//error = (*jvmti)->IterateThroughHeap(jvmti, 0, NULL, &heapCallbacks, NULL);
	//check_jvmti_error(jvmti, error, "Cannot iterate through heap");

	/* Process VM Death */
	enterCriticalSection(jvmti); {
		jclass              klass;
		jfieldID            field;
		jvmtiEventCallbacks callbacks;

		/* Disengage calls in HEAP_TRACKER_class. */
		klass = (*env)->FindClass(env, STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		if ( klass == NULL ) {
			fatal_error("ERROR: JNI: Cannot find %s with FindClass\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
		field = (*env)->GetStaticFieldID(env, klass, STRING(HEAP_TRACKER_engaged), "I");
		if ( field == NULL ) {
			fatal_error("ERROR: JNI: Cannot get field from %s\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
		(*env)->SetStaticIntField(env, klass, field, 0);

		/* The critical section here is important to hold back the VM death
		 *    until all other callbacks have completed.
		 */

		/* Clear out all callbacks. */
		(void)memset(&callbacks,0, sizeof(callbacks));
		error = (*jvmti)->SetEventCallbacks(jvmti, &callbacks,
				(jint)sizeof(callbacks));
		check_jvmti_error(jvmti, error, "Cannot set jvmti callbacks");

		/* Since this critical section could be holding up other threads
		 *   in other event callbacks, we need to indicate that the VM is
		 *   dead so that the other callbacks can short circuit their work.
		 *   We don't expect an further events after VmDeath but we do need
		 *   to be careful that existing threads might be in our own agent
		 *   callback code.
		 */
		gdata->vmDead = JNI_TRUE;

	} exitCriticalSection(jvmti);

	flush_method_event_buffer(jvmti);
}

/* Callback for JVMTI_EVENT_VM_OBJECT_ALLOC */
static void JNICALL
cbVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread,
		jobject o, jclass object_klass, jlong size)
{
	jlong id;
	jvmtiError error;

	HEAP_TRACKER_native_newobj(env, object_klass, thread, o, 0);
}

/* Callback for JVMTI_EVENT_OBJECT_FREE */
static void JNICALL
cbObjectFree(jvmtiEnv *jvmti, jlong id)
{
	log_method_event(0, RPROF_OBJECT_FREED, 0, 0, 1, &id);
}

/* Callback for JVMTI_EVENT_CLASS_FILE_LOAD_HOOK */
static void JNICALL
cbClassFileLoadHook(jvmtiEnv *jvmti, JNIEnv* env,
		jclass class_being_redefined, jobject loader,
		const char* name, jobject protection_domain,
		jint class_data_len, const unsigned char* class_data,
		jint* new_class_data_len, unsigned char** new_class_data)
{
	enterCriticalSection(jvmti); {
		/* It's possible we get here right after VmDeath event, be careful */
		if ( !gdata->vmDead ) {

			const char * classname;

			/* Name can be NULL, make sure we avoid SEGV's */
			if ( name == NULL ) {
				classname = java_crw_demo_classname(class_data, class_data_len, NULL);
				if ( classname == NULL ) {
					fatal_error("ERROR: No classname in classfile\n");
				}
			} else {
				classname = strdup(name);
				if ( classname == NULL ) {
					fatal_error("ERROR: Ran out of malloc() space\n");
				}
			}

			*new_class_data_len = 0;
			*new_class_data     = NULL;

			jint           cnum;
			int            systemClass;
			unsigned char* newImage;
			jint           newLength;

			//stdout_message("weaving %s\n", classname);

			/* Is it a system class? If the class load is before VmStart
			 *   then we will consider it a system class that should
			 *   be treated carefully. (See java_crw_demo)
			 */
			systemClass = 0;
			if ( !gdata->vmStarted ) {
				systemClass = 1;
			}

			newImage = NULL;
			newLength = 0;

			weave_classfile(classname, systemClass,
					class_data_len, class_data,
					&newLength, &newImage);

			/* If we got back a new class image, return it back as "the"
			 *   new class image. This must be JVMTI Allocate space.
			 */
			if ( newLength > 0 && newImage != NULL) {
				unsigned char *jvmti_space;

				char cfname[strlen(classname)+1];
				strcpy(cfname, classname);

				unsigned int i;
				for (i = 0; i < strlen(cfname); i++) {
					if (cfname[i] == '/') {
						cfname[i] = '.';
					}
				}

				char buffer[255];
				FILE *pre, *post;
				int written, err;

				sprintf(buffer, "../tmp/pre/%s.class", cfname);
				pre = fopen(buffer, "w");
				if (pre == NULL) {
					fatal_error("could not open file %s (%s)\n", buffer, strerror(errno));
				}
				written = fwrite ( class_data, sizeof(unsigned char), class_data_len, pre );
				if (written != class_data_len) {
					err = ferror(pre);
					fatal_error("error %d writing file: %s (%s)\n", err, strerror(err), buffer);
				}
				fclose(pre);

				sprintf(buffer, "../tmp/post/%s.class", cfname);
				post = fopen(buffer, "w");
				if (post == NULL) {
					fatal_error("could not open file %d: %s (%s)\n", buffer, strerror(errno));
				}
				written = fwrite ( newImage, sizeof(unsigned char), newLength, post );
				if (written != newLength) {
					err = ferror(post);
					fatal_error("error %d writing file: %s (%s)\n", err, strerror(err), buffer);
				}
				fclose(post);

				jvmti_space = (unsigned char *)allocate(jvmti, (jint)newLength);
				(void)memcpy((void*)jvmti_space, (void*)newImage, (int)newLength);

				*new_class_data_len = (jint)newLength;
				*new_class_data     = jvmti_space; /* VM will deallocate */
			}

			/* Always free up the space we get from java_crw_demo() */
			if ( newImage != NULL ) {
				(void)free((void*)newImage); /* Free malloc() space with free() */
			}

			(void)free((void*)classname);
		}
	} exitCriticalSection(jvmti);
}

/* Callback for JVMTI_EVENT_FIELD_ACCESS */
static void JNICALL
cbFieldAccess(jvmtiEnv *jvmti,
		JNIEnv* env,
		jthread thread,
		jmethodID method,
		jlocation location,
		jclass field_klass,
		jobject object,
		jfieldID field)
{
	jlong threadID = 0;
	jlong objectID = 0;
	jlong classID = 0;
	jint cnum = 0;
	jint mnum = 0;
	jvmtiError error;

	rField fieldRecord = getField(gdata->fieldMap, gdata->mapLength, field);
	if (fieldRecord.field == field) {
		mnum = fieldRecord.fnum;
	}

	if (thread != NULL) {
		error = (*jvmti)->GetTag(jvmti, thread, &threadID);
		check_jvmti_error(jvmti, error, "Cannot read thread tag");
	}

	if (object != NULL) {
		error = (*jvmti)->GetTag(jvmti, object, &objectID);
		check_jvmti_error(jvmti, error, "Cannot read object tag");
	}

	if (field_klass != NULL) {
		error = (*jvmti)->GetTag(jvmti, field_klass, &classID);
		check_jvmti_error(jvmti, error, "Cannot read class tag");
		cnum = (jint)(classID & 0xFFFFFFFFll);
	}

	log_method_event(threadID, RPROF_FIELD_READ, cnum, mnum, 1, &objectID);
}

/* Callback for JVMTI_EVENT_FIELD_MODIFICATION */
static void JNICALL
cbFieldModification(jvmtiEnv *jvmti,
		JNIEnv* env,
		jthread thread,
		jmethodID method,
		jlocation location,
		jclass field_klass,
		jobject object,
		jfieldID field,
		char signature_type,
		jvalue new_value)
{
	jlong threadID = 0;
	jlong classID = 0;
	jint cnum = 0;
	jint mnum = 0;
	jvmtiError error;
	jlong params[2];

	rField fieldRecord = getField(gdata->fieldMap, gdata->mapLength, field);
	if (fieldRecord.field == field) {
		mnum = fieldRecord.fnum;
	}

	if (thread != NULL) {
		error = (*jvmti)->GetTag(jvmti, thread, &threadID);
		check_jvmti_error(jvmti, error, "Cannot read thread tag");
	}

	if (object != NULL) {
		error = (*jvmti)->GetTag(jvmti, object, &params[0]);
		check_jvmti_error(jvmti, error, "Cannot read object tag");
	}

	if (field_klass != NULL) {
		error = (*jvmti)->GetTag(jvmti, field_klass, &classID);
		check_jvmti_error(jvmti, error, "Cannot read class tag");
		cnum = (jint)(classID & 0xFFFFFFFFll);
	}

	switch (signature_type) {
	case 'L':
	case '[':
		if (new_value.l != NULL) {
			error = (*jvmti)->GetTag(jvmti, new_value.l, &params[1]);
			check_jvmti_error(jvmti, error, "Cannot read param tag");
		}
		else {
			params[1] = 0;
		}
		log_method_event(threadID, RPROF_FIELD_WRITE, cnum, mnum, 2, params);
		break;
	default:
		log_method_event(threadID, RPROF_FIELD_WRITE, cnum, mnum, 1, params);
		break;
	}
}

/* Agent_OnLoad: This is called immediately after the shared library is
 *   loaded. This is the first code executed.
 */
JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
	static GlobalAgentData data;
	jvmtiEnv              *jvmti;
	jvmtiError             error;
	jint                   res;
	jvmtiCapabilities      capabilities;
	jvmtiEventCallbacks    callbacks;

	/* Setup initial global agent data area
	 *   Use of static/extern data should be handled carefully here.
	 *   We need to make sure that we are able to cleanup after ourselves
	 *     so anything allocated in this library needs to be freed in
	 *     the Agent_OnUnload() function.
	 */
	(void)memset((void*)&data, 0, sizeof(data));
	gdata = &data;

	data.fieldMap = NULL;

	/* First thing we need to do is get the jvmtiEnv* or JVMTI environment */
	res = (*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION_1);
	if (res != JNI_OK) {
		/* This means that the VM was unable to obtain this version of the
		 *   JVMTI interface, this is a fatal error.
		 */
		fatal_error("ERROR: Unable to access JVMTI Version 1 (0x%x),"
				" is your JDK a 5.0 or newer version?"
				" JNIEnv's GetEnv() returned %d\n",
				JVMTI_VERSION_1, res);
	}

	/* Here we save the jvmtiEnv* for Agent_OnUnload(). */
	gdata->jvmti = jvmti;

	/* initialize comm utilities */
	init_comm(jvmti, options);

	log_profiler_started();

	/* Immediately after getting the jvmtiEnv* we need to ask for the
	 *   capabilities this agent will need.
	 */
	(void)memset(&capabilities,0, sizeof(capabilities));
	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_tag_objects  = 1;
	capabilities.can_generate_object_free_events  = 1;
	// capabilities.can_get_source_file_name  = 1;
	// capabilities.can_get_line_numbers  = 1;
	capabilities.can_generate_vm_object_alloc_events  = 1;
	// capabilities.can_access_local_variables = 1;
	capabilities.can_generate_field_access_events = 1;
	capabilities.can_generate_field_modification_events = 1;
	error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
	check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities.");

	/* Next we need to provide the pointers to the callback functions to
	 *   to this jvmtiEnv*
	 */
	(void)memset(&callbacks,0, sizeof(callbacks));
	/* JVMTI_EVENT_VM_START */
	callbacks.VMStart           = &cbVMStart;
	/* JVMTI_EVENT_VM_INIT */
	callbacks.VMInit            = &cbVMInit;
	/* JVMTI_EVENT_VM_DEATH */
	callbacks.VMDeath           = &cbVMDeath;
	/* JVMTI_EVENT_OBJECT_FREE */
	callbacks.ObjectFree        = &cbObjectFree;
	/* JVMTI_EVENT_VM_OBJECT_ALLOC */
	callbacks.VMObjectAlloc     = &cbVMObjectAlloc;
	/* JVMTI_EVENT_CLASS_FILE_LOAD_HOOK */
	callbacks.ClassFileLoadHook = &cbClassFileLoadHook;
	/* JVMTI_EVENT_FIELD_MODIFICATION */
	callbacks.FieldModification = &cbFieldModification;
	/* JVMTI_EVENT_FIELD_ACCESS */
	callbacks.FieldAccess = &cbFieldAccess;
	error = (*jvmti)->SetEventCallbacks(jvmti, &callbacks, (jint)sizeof(callbacks));
	check_jvmti_error(jvmti, error, "Cannot set jvmti callbacks");

	/* At first the only initial events we are interested in are VM
	 *   initialization, VM death, and Class File Loads.
	 *   Once the VM is initialized we will request more events.
	 */
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_VM_START, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_VM_INIT, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_VM_DEATH, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_OBJECT_FREE, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_VM_OBJECT_ALLOC, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
			JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, (jthread)NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");

	/* Here we create a raw monitor for our use in this agent to
	 *   protect critical sections of code.
	 */
	error = (*jvmti)->CreateRawMonitor(jvmti, "agent data", &(gdata->lock));
	check_jvmti_error(jvmti, error, "Cannot create raw monitor");

	/* Add jar file to boot classpath */
	add_demo_jar_to_bootclasspath(jvmti, "rprof");

	/* We return JNI_OK to signify success */
	return JNI_OK;
}

/* Agent_OnUnload: This is called immediately before the shared library is
 *   unloaded. This is the last code executed.
 */
JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm)
{

	log_profiler_stopped();

	/* Skip any cleanup, VM is about to die anyway */
}

