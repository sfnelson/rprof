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

#include "rprof.h"
#include "java_crw_demo.h"

#include "jni.h"
#include "jvmti.h"

#include "agent_util.h"

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
#define HEAP_TRACKER_enter			enter		/* Name of java method execution method */
#define HEAP_TRACKER_exit			exit		/* Name of java method return method */
#define HEAP_TRACKER_native_enter	_menter		/* Name of java method execution native */
#define HEAP_TRACKER_native_exit	_mexit		/* Name of java method return native */
#define HEAP_TRACKER_engaged		engaged		/* Name of static field switch */

/* C macros to create strings from tokens */
#define _STRING(s) #s
#define STRING(s) _STRING(s)

/* ------------------------------------------------------------------- */

/* Global agent data structure */

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
	/* Counter on classes where BCI has been applied */
	jint           ccount;
	/* Counter for object ids */
	jlong		   oid;
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

	jlong id;

	enterCriticalSection(gdata->jvmti); {
		id = gdata->oid++;
	} exitCriticalSection(gdata->jvmti);

	return id;
}

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

	//stdout_message(format, id, signature);
}

/* Java Native Method for Object.<init> */
static void
HEAP_TRACKER_native_newobj(JNIEnv *env, jclass klass, jthread thread, jobject o, jlong id)
{
	jvmtiError error;
	jvmtiEnv *jvmti;
	char* signature;
	jclass cls;

	if ( gdata->vmDead ) {
		return;
	}

	jvmti = gdata->jvmti;

	error = (*jvmti)->SetTag(jvmti, o, id);
	check_jvmti_error(jvmti, error, "Cannot tag object with id");

	logEvent(env, jvmti, thread, o, "object allocated: %x (%s)\n");
}

/* Java Native Method for newarray */
static void
HEAP_TRACKER_native_newarr(JNIEnv *env, jclass klass, jthread thread, jobject a, jlong id)
{
	jvmtiError error;
	jvmtiEnv *jvmti;

	if ( gdata->vmDead ) {
		return;
	}

	jvmti = gdata->jvmti;

	error = (*jvmti)->SetTag(jvmti, a, id);
	check_jvmti_error(jvmti, error, "Cannot tag array with id");

	logEvent(env, jvmti, thread, a, "array allocated:  %x (%s)\n");
}

/* Java Native Method for method execution */
static void
HEAP_TRACKER_native_enter(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum)
{
	if (gdata->vmInitialized) {
		jvmtiError error;
		jvmtiEnv *jvmti;
		jclass cls;

		if ( gdata->vmDead ) {
			return;
		}

		jvmti = gdata->jvmti;

		//stdout_message("method execution began: %d %d\n", cnum, mnum);

		jvmtiFrameInfo frames[5];
		jint count;
		jmethodID method;
		jint size;
		char *methodName;
		char *signature;
		jobject obj;
		int i;

		error = (*jvmti)->GetStackTrace(jvmti, thread, 0, 5, &frames, &count);
		check_jvmti_error(jvmti, error, "Cannot access stack trace");

		method = frames[2].method;

		if (count < 1) return;

		error = (*jvmti)->GetMethodName(jvmti, method, &methodName, &signature, NULL);
		check_jvmti_error(jvmti, error, "Cannot access method name");

		error = (*jvmti)->GetArgumentsSize(jvmti, method, &size);
		check_jvmti_error(jvmti, error, "Cannot access arguments size");

		//stdout_message("Executing method: %s %d %s\n", methodName, size, signature);

		char argTypes[size];
		int arg = 0;
		int len = strlen(signature);
		for (i = 1; i < len; i++) {
			switch(signature[i]) {
			case ')':
				i = strlen(signature);
				break;
			case 'J': // long
			case 'D': // double
				argTypes[arg++] = signature[i]; // ignore primitive parameter
			case 'B': // byte
			case 'C': // character
			case 'S': // short
			case 'Z': // boolean
			case 'F': // float
			case 'I': // int
				argTypes[arg++] = signature[i]; // ignore primitive parameter
				break;
			case 'L': // object, followed by FQ name, terminated by ';'
			case '[': // array, followed by a second type descriptor
				argTypes[arg++] = signature[i];
				while (signature[i] == '[') i++;
				if (signature[i] == 'L') {
					while (signature[++i] != ';');
				}
				break;
			}
		}
		if (arg < size) {
			argTypes[arg] = '.';
		}

		jlong args[size];
		int offset = 0;
		if (arg < size) {
			offset = 1;
		}
		for (i = 0; i < size; i++) {
			if (i - offset < 0 || argTypes[i - offset] == 'L' || argTypes[i - offset] == '[') {
				error = (*jvmti)->GetLocalObject(jvmti, thread, 2, i, &obj);
				check_jvmti_error(jvmti, error, "Cannot access local variable");

				if (obj != NULL) {
					error = (*jvmti)->GetTag(jvmti, obj, &args[i]);
					check_jvmti_error(jvmti, error, "Cannot read tag");
				} else {
					args[i] = 0xDEADBEEF;
				}
			} else {
				args[i] = 0;
			}
		}

		/*stdout_message("method arguments: %s ( ", methodName);
		for (i = 0; i < size; i++) {
			stdout_message("%x %c ", args[i], argTypes[i]);
		}
		stdout_message(") - %s\n", signature);*/

		(*jvmti)->Deallocate(jvmti, methodName);
		(*jvmti)->Deallocate(jvmti, signature);
	}
}

/* Java Native Method for method return */
static void
HEAP_TRACKER_native_exit(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum)
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

		//stdout_message("method execution finished: %d %d\n", cnum, mnum);
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
		static JNINativeMethod registry[4] = {
				{STRING(HEAP_TRACKER_native_newobj), "(Ljava/lang/Object;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newobj},
				{STRING(HEAP_TRACKER_native_newarr), "(Ljava/lang/Object;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newarr},
				{STRING(HEAP_TRACKER_native_enter), "(Ljava/lang/Object;II)V", (void*)&HEAP_TRACKER_native_enter},
				{STRING(HEAP_TRACKER_native_exit), "(Ljava/lang/Object;II)V", (void*)&HEAP_TRACKER_native_exit}
		};

		/* Register Natives for class whose methods we use */
		klass = (*env)->FindClass(env, STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		if ( klass == NULL ) {
			fatal_error("ERROR: JNI: Cannot find %s with FindClass\n",
					STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
		rc = (*env)->RegisterNatives(env, klass, registry, 4);
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
cbObjectTagger(jlong class_tag, jlong size, jlong* tag_ptr, jint length,
		void *user_data)
{
	*tag_ptr = getNewObjectId();

	return JVMTI_VISIT_OBJECTS;
}

/* Callback for JVMTI_EVENT_VM_INIT */
static void JNICALL
cbVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;

	stdout_message("-------- tagging exiting objects\n");

	/* Iterate through heap, find all untagged objects allocated before this */
	(void)memset(&heapCallbacks, 0, sizeof(heapCallbacks));
	heapCallbacks.heap_iteration_callback = &cbObjectTagger;
	error = (*jvmti)->IterateThroughHeap(jvmti, JVMTI_HEAP_FILTER_TAGGED,
			NULL, &heapCallbacks, NULL);
	check_jvmti_error(jvmti, error, "Cannot iterate through heap");

	stdout_message("-------- done tagging exiting objects\n");

	enterCriticalSection(jvmti); {

		/* Indicate VM is initialized */
		gdata->vmInitialized = JNI_TRUE;

	} exitCriticalSection(jvmti);
}

/* Callback for JVMTI_EVENT_VM_DEATH */
static void JNICALL
cbVMDeath(jvmtiEnv *jvmti, JNIEnv *env)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;

	/* These are purposely done outside the critical section */

	/* Force garbage collection now so we get our ObjectFree calls */
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");

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

}

/* Callback for JVMTI_EVENT_VM_OBJECT_ALLOC */
static void JNICALL
cbVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread,
		jobject o, jclass object_klass, jlong size)
{
	jlong id;
	jvmtiError error;

	if ( gdata->vmDead ) {
		return;
	}

	id = getNewObjectId(env);

	error = (*jvmti)->SetTag(jvmti, o, id);
	check_jvmti_error(jvmti, error, "Cannot tag object with id");

	logEvent(env, jvmti, thread, o, "object allocated: %x (%s)\n");
}

/* Callback for JVMTI_EVENT_OBJECT_FREE */
static void JNICALL
cbObjectFree(jvmtiEnv *jvmti, jlong id)
{

	if ( gdata->vmDead ) {
		return;
	}

	//stdout_message("object freed: %x\n", id);
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
				classname = java_crw_demo_classname(class_data, class_data_len,
						NULL);
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

			//stdout_message("weaving %s\n", classname);

			/* The tracker class itself? */
			if ( strstr(classname, STRING(HEAP_TRACKER_package)) != classname ) {
				jint           cnum;
				int            systemClass;
				unsigned char *newImage;
				long           newLength;

				/* Get number for every class file image loaded */
				cnum = gdata->ccount++;

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

				/* Call the class file reader/write demo code */
				java_crw_demo(cnum,
						classname,
						class_data,
						class_data_len,
						systemClass,
						STRING(HEAP_TRACKER_package/HEAP_TRACKER_class),
						"L" STRING(HEAP_TRACKER_package/HEAP_TRACKER_class) ";",
						STRING(HEAP_TRACKER_enter), "(II)V", // Method offset 0
						STRING(HEAP_TRACKER_exit), "(II)V", // Method return
						STRING(HEAP_TRACKER_newobj), "(Ljava/lang/Object;)V", // Object <init>
						STRING(HEAP_TRACKER_newarr), "(Ljava/lang/Object;)V", // new array opcode
						&newImage,
						&newLength,
						NULL,
						NULL);

				/* If we got back a new class image, return it back as "the"
				 *   new class image. This must be JVMTI Allocate space.
				 */
				if ( newLength > 0 ) {
					unsigned char *jvmti_space;

					jvmti_space = (unsigned char *)allocate(jvmti, (jint)newLength);
					(void)memcpy((void*)jvmti_space, (void*)newImage, (int)newLength);
					*new_class_data_len = (jint)newLength;
					*new_class_data     = jvmti_space; /* VM will deallocate */
				}

				/* Always free up the space we get from java_crw_demo() */
				if ( newImage != NULL ) {
					(void)free((void*)newImage); /* Free malloc() space with free() */
				}
			}

			(void)free((void*)classname);
		}
	} exitCriticalSection(jvmti);
}

/* Parse the options for this heapTracker agent */
static void
parse_agent_options(char *options)
{
#define MAX_TOKEN_LENGTH        16
	char  token[MAX_TOKEN_LENGTH];
	char *next;

	/* Defaults */
	gdata->maxDump = 20;

	/* Parse options and set flags in gdata */
	if ( options==NULL ) {
		return;
	}

	/* Get the first token from the options string. */
	next = get_token(options, ",=", token, (int)sizeof(token));

	/* While not at the end of the options string, process this option. */
	while ( next != NULL ) {
		if ( strcmp(token,"help")==0 ) {
			stdout_message("The heapTracker JVMTI demo agent\n");
			stdout_message("\n");
			stdout_message(" java -agent:heapTracker[=options] ...\n");
			stdout_message("\n");
			stdout_message("The options are comma separated:\n");
			stdout_message("\t help\t\t\t Print help information\n");
			stdout_message("\t maxDump=n\t\t\t How many TraceInfo's to dump\n");
			stdout_message("\n");
			exit(0);
		} else if ( strcmp(token,"maxDump")==0 ) {
			char  number[MAX_TOKEN_LENGTH];

			next = get_token(next, ",=", number, (int)sizeof(number));
			if ( next == NULL ) {
				fatal_error("ERROR: Cannot parse maxDump=number: %s\n", options);
			}
			gdata->maxDump = atoi(number);
		} else if ( token[0]!=0 ) {
			/* We got a non-empty token and we don't know what it is. */
			fatal_error("ERROR: Unknown option: %s\n", token);
		}
		/* Get the next token (returns NULL if there are no more) */
		next = get_token(next, ",=", token, (int)sizeof(token));
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

	/* Parse any options supplied on java command line */
	parse_agent_options(options);

	/* Immediately after getting the jvmtiEnv* we need to ask for the
	 *   capabilities this agent will need.
	 */
	(void)memset(&capabilities,0, sizeof(capabilities));
	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_tag_objects  = 1;
	capabilities.can_generate_object_free_events  = 1;
	capabilities.can_get_source_file_name  = 1;
	capabilities.can_get_line_numbers  = 1;
	capabilities.can_generate_vm_object_alloc_events  = 1;
	capabilities.can_access_local_variables = 1;
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
	/* Skip any cleanup, VM is about to die anyway */
}

