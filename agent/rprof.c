#include <errno.h>
#include <stdint.h>

#include "rprof.h"

#include "agent_util.h"
#include "rprof_comm.h"
#include "rprof_classes.h"
#include "rprof_fields.h"
#include "rprof_events.h"

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

#define ACC_INTERFACE 512

#define _SAVE_OTHERS
#define _RESTORE_OTHERS

struct _m128 {
    uint64_t x;
    uint64_t y;
} __attribute__ ((aligned (16)));

#define _SAVE_XMM \
struct _m128 _xmm0, _xmm1, _xmm2, _xmm3, _xmm4, _xmm5, _xmm6, _xmm7; \
__asm__ volatile ( \
"           movups   %%xmm0,%[_xmm0]\n" \
"           movups   %%xmm1,%[_xmm1]\n" \
"           movups   %%xmm2,%[_xmm2]\n" \
"           movups   %%xmm3,%[_xmm3]\n" \
"           movups   %%xmm4,%[_xmm4]\n" \
"           movups   %%xmm5,%[_xmm5]\n" \
"           movups   %%xmm6,%[_xmm6]\n" \
"           movups   %%xmm7,%[_xmm7]\n" \
: \
: [_xmm0] "o" (_xmm0), [_xmm1] "o" (_xmm1), [_xmm2] "o" (_xmm2), [_xmm3] "o" (_xmm3), \
[_xmm4] "o" (_xmm4), [_xmm5] "o" (_xmm5), [_xmm6] "o" (_xmm6), [_xmm7] "o" (_xmm7) \
: "memory"); \
{
/* end of _SAVE_XMM */

#define _RESTORE_XMM \
} \
__asm__ volatile ( \
"           movups   %[_xmm0],%%xmm0\n" \
"           movups   %[_xmm1],%%xmm1\n" \
"           movups   %[_xmm2],%%xmm2\n" \
"           movups   %[_xmm3],%%xmm3\n" \
"           movups   %[_xmm4],%%xmm4\n" \
"           movups   %[_xmm5],%%xmm5\n" \
"           movups   %[_xmm6],%%xmm6\n" \
"           movups   %[_xmm7],%%xmm7\n" \
: \
: [_xmm0] "o" (_xmm0), [_xmm1] "o" (_xmm1), [_xmm2] "o" (_xmm2), [_xmm3] "o" (_xmm3), \
[_xmm4] "o" (_xmm4), [_xmm5] "o" (_xmm5), [_xmm6] "o" (_xmm6), [_xmm7] "o" (_xmm7) \
: "memory");
/* end of _RESTORE_XMM */

/* ------------------------------------------------------------------- */

/* Global agent data structure */

typedef struct {
	/* JVMTI Environment */
	jvmtiEnv      *jvmti;
	/* State of the VM flags */
	jboolean       vmStarted;
	jboolean       vmInitialized;
	jboolean       vmDead;
    
	/* Data access Lock */
	jrawMonitorID  lock;
    
	/* Counter for the number of untagged objects which have been tagged natively */
	jlong		   nullCounter;
    
    CommEnv        comm;
    ClassList      classes;
	FieldTable     fields;
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
static jlong
generate_object_tag()
{
	/* Because this is called from within cbObjectTagger we can't call JNI functions. */
	return ++(gdata->nullCounter);
}

static jlong
tag_class(jvmtiEnv *jvmti, jclass cls, jint cnum)
{
    jvmtiError error;
    jlong tag = RPROF_CLASS_THREAD_ID | cnum;
    
    error = (*jvmti)->SetTag(jvmti, cls, tag);
    check_jvmti_error(jvmti, error, "Cannot tag class with id");
    
    return tag;
}

static jlong
tag_object(jvmtiEnv *jvmti, jobject obj, jlong tag)
{
    jvmtiError error;
    
    error = (*jvmti)->SetTag(jvmti, obj, tag);
    check_jvmti_error(jvmti, error, "Cannot tag object with id");
    
    return tag;
}

static jlong
get_tag(jvmtiEnv *jvmti, jobject obj)
{
    jvmtiError error;
    jlong tag;
    
    if (obj == NULL) return 0;
    
    error = (*jvmti)->GetTag(jvmti, obj, &tag);
    check_jvmti_error(jvmti, error, "Cannot read tag");
    
    return tag;
}

static void
watch_field(r_fieldRecord *record)
{
    jvmtiEnv *jvmti = gdata->jvmti;
	jvmtiError error;
    
    /*
     jlong ctag;
     jint cnum;
     char *cname, *fname;
     
     error = (*jvmti)->GetClassSignature(jvmti, record->cls, &cname, NULL);
     check_jvmti_error(jvmti, error, "could not get class name\n");
     
     error = (*jvmti)->GetFieldName(jvmti, record->cls, record->field, &fname, NULL, NULL);
     check_jvmti_error(jvmti, error, "could not get field name\n");
     
     error = (*jvmti)->GetTag(jvmti, record->cls, &ctag);
     check_jvmti_error(jvmti, error, "could not get class tag\n");
     
     cnum = TAG_TO_CNUM(ctag);
     
     stdout_message("--watching %x,%x (%s.%s) \n", cnum, record->field, cname, fname);
     deallocate(jvmti, cname);
     deallocate(jvmti, fname);
     */
    
	error = (*jvmti)->SetFieldAccessWatch(jvmti, record->cls, record->field);
	check_jvmti_error(jvmti, error, "Cannot add access watch to field");
	error = (*jvmti)->SetFieldModificationWatch(jvmti, record->cls, record->field);
	check_jvmti_error(jvmti, error, "Cannot add modify watch to field");
}

/* Java Native Method for <clinit> */
static void
HEAP_TRACKER_native_newcls(JNIEnv *env, jclass tracker, jobject cls, jint cnum, jintArray toWatch)
{
	jvmtiError error;
	jvmtiEnv *jvmti;
	jlong tag;
	jint numFields;
	jint* targetFields;
	jfieldID* fields;
	r_fieldRecord* toStore;
	r_fieldRecord* record;
	size_t i;
	jboolean vmInitialized;
	r_event event;
    
    jvmti = gdata->jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	tag = get_tag(jvmti, cls);
    if ((tag & RPROF_CLASS_THREAD_ID) == RPROF_CLASS_THREAD_ID) {
        return;
    }
    
	bzero(&event, sizeof(event));
    
	tag = tag_class(jvmti, cls, cnum);
    
    event.type = RPROF_CLASS_INITIALIZED;
    event.cid = cnum;
    event.args_len = 1;
    event.args[0] = tag;
    
	comm_log(gdata->comm, &event);
    
	if (toWatch == NULL) return;
    
    size_t len = (size_t) (*env)->GetArrayLength(env, toWatch);
    targetFields = (*env)->GetIntArrayElements(env, toWatch, NULL);     /* (1) */
    vmInitialized = gdata->vmInitialized;
    
    error = (*jvmti)->GetClassFields(jvmti, cls, &numFields, &fields);  /* (2) */
    check_jvmti_error(jvmti, error, "cannot get fields for new class");
    
    toStore = allocate(jvmti, sizeof(r_fieldRecord) * len);             /* (3) */
    
    cls = (*env)->NewGlobalRef(env, cls);
    
    
    for (i = 0; i < len; i++) {
        record = &(toStore[i]);
        record->id.id.cls = (jint) cnum;
        record->id.id.field = (jshort) targetFields[i];
        record->cls = cls;
        record->class_tag = tag;
        /* Fields begin at 1 in our profiler and 0 in jvmti */
        record->field = fields[targetFields[i] - 1];
    }
    
    /* always store fields, watches are added later if we're not ready yet */
    fields_store(gdata->fields, toStore, len);
    
    /* don't watch fields until after they've been inserted into the map */
    if (vmInitialized == JNI_TRUE) {
        for (i = 0; i < len; i++) {
            watch_field(&(toStore[i]));
        }
    }
    
    /* cleanup */
    (*env)->ReleaseIntArrayElements(env, toWatch, targetFields,         /* (1) */
                                    JNI_ABORT);
    deallocate(jvmti, fields);                                          /* (2) */
    deallocate(jvmti, toStore);                                         /* (3) */
}

/* Java Native Method for Object.<init> */
static void
HEAP_TRACKER_native_newobj(JNIEnv *env, jclass site, jthread thread, jclass klass, jobject o, jlong id)
{
    r_event event;
    jlong type;
    jvmtiEnv *jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
    jvmti = gdata->jvmti;
    
	bzero(&event, sizeof(event));
    
	/*if (id == 0) { todo this is a test */
    enterCriticalSection(jvmti); {
	    id = generate_object_tag();
    }; exitCriticalSection(jvmti);
	/*}*/
    
	tag_object(jvmti, o, id);
	type = get_tag(jvmti, klass);
    
    /*
     if (type == 0) {
     print_class(gdata->jvmti, klass);
     }
     */
    
	event.type = RPROF_OBJECT_ALLOCATED;
	event.thread = get_tag(jvmti, thread);
	event.cid = TAG_TO_CNUM(type);
	event.args_len = 1;
	event.args[0] = id;
    
	comm_log(gdata->comm, &event);
}

/* Java Native Method for newarray */
static void
HEAP_TRACKER_native_newarr(JNIEnv *env, jclass klass, jthread thread, jobject a, jlong id)
{
    r_event event;
    jvmtiEnv *jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	jvmti = gdata->jvmti;
    
	bzero(&event, sizeof(event));
    
	if (id == 0) {
        id = generate_object_tag();
    }
    
	tag_object(jvmti, a, id);
    
    event.type = RPROF_ARRAY_ALLOCATED;
    event.thread = get_tag(jvmti, thread);
    event.args_len = 1;
    event.args[0] = id;
    
    comm_log(gdata->comm, &event);
}

/* Java Native Method for method execution */
static void
HEAP_TRACKER_native_enter(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jarray args)
{
	jvmtiEnv *jvmti;
    r_event event;
	int i;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	bzero(&event, sizeof(event));
    
	jvmti = gdata->jvmti;
    
    event.type = RPROF_METHOD_ENTER;
    event.thread = get_tag(jvmti, thread);
    event.cid = cnum;
    event.attr.mid = mnum;
    
	if (args != NULL) {
		event.args_len = (*env)->GetArrayLength(env, args);
        if (event.args_len > RPROF_MAX_PARAMETERS) {
            fatal_error("max method parameters exceeded! %d.%d %d > %d\n",
                        cnum, mnum, event.args_len, RPROF_MAX_PARAMETERS);
        }
		for (i = 0; i < event.args_len; i++) {
			jobject o = (*env)->GetObjectArrayElement(env, args, i);
			event.args[i] = get_tag(jvmti, o);
		}
	}
    
	comm_log(gdata->comm, &event);
}

/* Java Native Method for method return */
static void
HEAP_TRACKER_native_exit(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jobject arg)
{
    r_event event;
    jvmtiEnv *jvmti;
    
    if ( gdata->vmDead ) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    bzero(&event, sizeof(event));
    
    event.type = RPROF_METHOD_RETURN;
    event.thread = get_tag(jvmti, thread);
    event.cid = cnum;
    event.attr.mid = mnum;
    
    if (arg != NULL) {
        event.args_len = 1;
        event.args[0] = get_tag(jvmti, arg);
    }
    
    comm_log(gdata->comm, &event);
}

/* Java Native Method for method exceptional return */
static void
HEAP_TRACKER_native_except(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum, jobject thrown)
{
    r_event event;
    jvmtiEnv *jvmti;
    
    if ( gdata->vmDead ) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    bzero(&event, sizeof(event));
    
    event.type = RPROF_METHOD_EXCEPTION;
    event.thread = get_tag(jvmti, thread);
    event.cid = cnum;
    event.attr.mid = mnum;
    
    if (thrown != NULL) {
        event.args_len = 1;
        event.args[0] = get_tag(jvmti, thrown);
    }
    
    comm_log(gdata->comm, &event);
}

static void
HEAP_TRACKER_native_main(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum)
{
    r_event event;
    
    if ( gdata->vmDead ) {
        return;
    }
    
    bzero(&event, sizeof(event));
    
    event.type = RPROF_METHOD_ENTER;
    event.thread = get_tag(gdata->jvmti, thread);
    event.cid = cnum;
    event.attr.mid = mnum;
    
    comm_log(gdata->comm, &event);
}

/* Callback for JVMTI_EVENT_VM_START */
static void
cbVMStart(jvmtiEnv *jvmti, JNIEnv *env)
{
	enterCriticalSection(jvmti); {
		jclass klass;
		jfieldID field;
		jint rc;
        
		/* Java Native Methods for class */
		static JNINativeMethod registry[7] = {
            {STRING(HEAP_TRACKER_native_newobj), "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Object;J)V",
                (void*)&HEAP_TRACKER_native_newobj},
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


static void
cbClassTagger(jvmtiEnv *jvmti, JNIEnv *env, const char* cname)
{
    jclass klass;
    jmethodID mid;
    jint status;
    jvmtiError error;
    jlong tag;
    
    klass = (*env)->FindClass(env, cname);
    if (klass == NULL) {
        stdout_message("====could not find class %s\n", cname);
        return;
    }
    
    tag = get_tag(jvmti, klass);
    if ((tag & RPROF_CLASS_THREAD_ID) == RPROF_CLASS_THREAD_ID) {
        return;
    }
    
    error = (*jvmti)->GetClassStatus(jvmti, klass, &status);
    check_jvmti_error(jvmti, error, "Unable to get class status!");
    
    if ((status & JVMTI_CLASS_STATUS_INITIALIZED) == 0) {
        stdout_message("====class %s not ready (%x)\n", cname, status);
        return;
    }
    
    mid = (*env)->GetStaticMethodID(env, klass, "_rprof_agent_init", "()V");
    if (mid == NULL) {
        (*env)->ExceptionClear(env);
        stdout_message("====class %s does not have an agent init method\n", cname);
        return;
    }
    
    (*env)->CallStaticVoidMethod(env, klass, mid);
}

/* Iterate Through Heap callback */
static jint
cbObjectTagger(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* userData)
{
	r_event event;
    
	if (length != -1) {
		/* TODO: stop ignoring arrays! */
		return JVMTI_VISIT_OBJECTS;
	}
    
	if (*tag_ptr == 0) {
	    /* this is equivalent to calling tag_object() */
		*tag_ptr = generate_object_tag();
	}
    
    bzero(&event, sizeof(event));
    
    event.type = RPROF_OBJECT_TAGGED;
    event.cid = (jint)(class_tag & 0xFFFFFFFFll);
	event.args_len = 1;
	event.args[0] = *tag_ptr;
    comm_log(gdata->comm, &event);
    
	return JVMTI_VISIT_OBJECTS;
}

/* Callback for JVMTI_EVENT_VM_INIT */
static void
cbVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;
    jclass             object_class;
    
#ifdef DEBUG
	stdout_message("----- trying gc at init\n");
#endif
    
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");
    
#ifdef DEBUG
	stdout_message("----- gc ok on init\n");
#endif
    
    enterCriticalSection(jvmti); {
    
        object_class = (*env)->FindClass(env, "java/lang/Object");
        HEAP_TRACKER_native_newcls(env, object_class, object_class, 1, NULL);
        
        classes_visit(gdata->classes, jvmti, env, &cbClassTagger);
        classes_destroy(gdata->classes);
        gdata->classes = NULL;
        
#ifdef DEBUG
        stdout_message("----- loaded classes for init\n");
#endif
        
        /* Iterate through heap, find all untagged objects allocated before this */
        bzero(&heapCallbacks, sizeof(heapCallbacks));
        heapCallbacks.heap_iteration_callback = &cbObjectTagger;
        error = (*jvmti)->IterateThroughHeap(jvmti, 0, NULL, &heapCallbacks, NULL);
        check_jvmti_error(jvmti, error, "Cannot iterate through heap");
        
#ifdef DEBUG
        stdout_message("----- tagged existing objects\n");
#endif
        
        error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
                                                   JVMTI_EVENT_FIELD_MODIFICATION, (jthread)NULL);
        check_jvmti_error(jvmti, error, "Cannot set event notification");
        error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
                                                   JVMTI_EVENT_FIELD_ACCESS, (jthread)NULL);
        check_jvmti_error(jvmti, error, "Cannot set event notification");
        
        fields_visit(gdata->fields, &watch_field);
        
		/* Indicate VM is initialized */
		gdata->vmInitialized = JNI_TRUE;
        
	} exitCriticalSection(jvmti);
}

/* Callback for JVMTI_EVENT_VM_DEATH */
static void
cbVMDeath(jvmtiEnv *jvmti, JNIEnv *env)
{
	jvmtiError         error;
    
	comm_flush(gdata->comm);
    
#ifdef DEBUG
	stdout_message("----- vm death\n");
#endif
    
	/* These are purposely done outside the critical section */
    
	/* Force garbage collection now so we get our ObjectFree calls */
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");
    
#ifdef DEBUG
	stdout_message("----- gc finished\n");
#endif
    
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
		bzero(&callbacks, sizeof(callbacks));
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
    
	comm_flush(gdata->comm);
}

/* Callback for JVMTI_EVENT_VM_OBJECT_ALLOC */
static void
cbVMObjectAlloc(jvmtiEnv *jvmti, JNIEnv *env, jthread thread,
                jobject o, jclass klass, jlong size)
{
	HEAP_TRACKER_native_newobj(env, klass, thread, klass, o, 0);
}

/* Callback for JVMTI_EVENT_OBJECT_FREE */
static void
cbObjectFree(jvmtiEnv *jvmti, jlong id)
{
    r_event event;
    
    bzero(&event, sizeof(event));
    
    event.type = RPROF_OBJECT_FREED;
    event.args_len = 1;
    event.args[0] = id;
    comm_log(gdata->comm, &event);
}

static void
cbClassPrepareHook(jvmtiEnv *jvmti, JNIEnv* env, jthread thread, jclass klass) {
    jvmtiError error;
    jlong tag;
    jint access;
    char* cname;
    jmethodID mid;
    
    error = (*jvmti)->GetClassModifiers(jvmti, klass, &access);
    check_jvmti_error(jvmti, error, "could not get class access flags");
    
    if ((access & ACC_INTERFACE) != 0) {
        return; /* interface, don't tag */
    }
    
    tag = get_tag(jvmti, klass);
    if ((tag & RPROF_CLASS_THREAD_ID) == RPROF_CLASS_THREAD_ID) {
        return; /* already tagged */
    }
    
    mid = (*env)->GetStaticMethodID(env, klass, "_rprof_agent_init", "()V");
    if (mid == NULL) {
        (*env)->ExceptionClear(env);
        (*jvmti)->GetClassSignature(jvmti, klass, &cname, NULL);
        stdout_message("====class %s does not have an agent init method\n", cname);
        deallocate(jvmti, cname);
        return;
    }
    
    (*env)->CallStaticVoidMethod(env, klass, mid);
}

#ifdef DEBUG
static void
storeClass(const char* classname,
           const unsigned char* pre_data, const size_t pre_len,
           const unsigned char* post_data, const size_t post_len)
{
    char buffer[255], cname[255];
    FILE *pre, *post;
    size_t written;
    int err;
    
    strcpy(cname, classname);

    size_t i;
    for (i = 0; i < strlen(cname); i++) {
        if (cname[i] == '/') {
            cname[i] = '.';
        }
    }
    
    sprintf(buffer, "tmp/pre/%s.class", cname);
    pre = fopen(buffer, "w");
    if (pre == NULL) {
        fatal_error("could not open file %s (%s)\n", buffer, strerror(errno));
    }
    written = fwrite(pre_data, 1, pre_len, pre);
    if (written != pre_len) {
        err = ferror(pre);
        fatal_error("error writing file: %s (%s)\n", buffer, strerror(err));
    }
    fclose(pre);
    
    sprintf(buffer, "tmp/post/%s.class", cname);
    post = fopen(buffer, "w");
    if (post == NULL) {
        fatal_error("could not open file: %s (%s)\n", buffer, strerror(errno));
    }
    written = fwrite(post_data, 1, post_len, post);
    if (written != post_len) {
        err = ferror(post);
        fatal_error("error writing file: %s (%s)\n", buffer, strerror(err));
    }
    fclose(post);
}
#endif

/* Callback for JVMTI_EVENT_CLASS_FILE_LOAD_HOOK */
static void
cbClassFileLoadHook(jvmtiEnv *jvmti, JNIEnv* env,
                    jclass class_being_redefined, jobject loader,
                    const char* classname, jobject protection_domain,
                    jint class_len, const unsigned char* class_data,
                    jint* new_class_len, unsigned char** new_class_data)
{
    jboolean       systemClass;
    jint           class_id;
    
    /* It's possible we get here right after VmDeath event, be careful */
    if ( !gdata->vmDead ) {
        
        enterCriticalSection(jvmti); {
            
            /* Name can be NULL, make sure we avoid SEGV's */
            if ( classname == NULL ) {
                fatal_error("ERROR: No classname in classfile\n");
                return;
            }
            
            systemClass = JNI_FALSE;
            if ( !gdata->vmStarted ) {
                systemClass = JNI_TRUE;
            }
            
            class_id = 0;
            
            comm_weave(gdata->comm, classname, systemClass,
                       class_len, class_data,
                       new_class_len, new_class_data,
                       &class_id);
            
#ifdef DEBUG
            storeClass(classname, class_data, (size_t) class_len, *new_class_data, (size_t) *new_class_len);
#endif
            
            if (!gdata->vmInitialized == JNI_TRUE && class_id > 0) {
                classes_add(gdata->classes, classname);
            }
            
        }; exitCriticalSection(jvmti);
    }
}

static void JNICALL
findFieldRecord(jvmtiEnv *jvmti, JNIEnv *env, jclass field_klass, jfieldID field, r_fieldRecord *record)
{
    jclass cls;
    jlong class_tag = 0;
    char *cname, *fname;
    
    bzero(record, sizeof(r_fieldRecord));
    
    cls = field_klass;
    class_tag = get_tag(jvmti, cls);
    fields_find(gdata->fields, class_tag, field, record);
    
    while (cls != NULL && TAG_TO_CNUM(class_tag) != 1 && record->cls == NULL) {
        class_tag = get_tag(jvmti, cls);
        fields_find(gdata->fields, class_tag, field, record);
        if (record->cls == NULL) {
            cls = (*env)->GetSuperclass(env, cls);
        }
        else {
            /* found a field on a superclass - register for next time */
            class_tag = get_tag(jvmti, field_klass);
            record->class_tag = class_tag;
            record->cls = field_klass;
            fields_store(gdata->fields, record, 1);
            break;
        }
    }
    
    if (record->class_tag != class_tag || record->field != field) {
        (*jvmti)->GetClassSignature(jvmti, field_klass, &cname, NULL);
        (*jvmti)->GetFieldName(jvmti, field_klass, field, &fname, NULL, NULL);
        fatal_error("could not find field %s.%s (%llx.%llx), found (%llx.%llx)\n",
                    cname, fname,
                    class_tag, field,
                    record->class_tag, record->field);
        deallocate(jvmti, cname);
        deallocate(jvmti, fname);
    }
}

static void _cbFieldAccess(jvmtiEnv* jvmti,
                           JNIEnv* env,
                           jthread thread,
                           jmethodID method,
                           jlocation location,
                           jclass field_klass,
                           jobject object,
                           jfieldID field) __attribute__ ((__always_inline__));

static void JNICALL
_cbFieldAccess(jvmtiEnv *jvmti,
               JNIEnv* env,
               jthread thread,
               jmethodID method,
               jlocation location,
               jclass field_klass,
               jobject object,
               jfieldID field)
{
    r_event event;
    r_fieldRecord record;
    
    bzero(&event, sizeof(r_event));
    
    findFieldRecord(jvmti, env, field_klass, field, &record);
    
    event.thread = get_tag(jvmti, thread);
    event.type = RPROF_FIELD_READ;
    event.cid = record.id.id.cls;
    event.attr.fid = record.id.id.field;
    event.args_len = 1;
    event.args[0] = get_tag(jvmti, object);
    
    comm_log(gdata->comm, &event);
}

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
    _SAVE_OTHERS
    _SAVE_XMM
    
    _cbFieldAccess(jvmti, env, thread, method, location, field_klass, object, field);
    
    _RESTORE_XMM
    _RESTORE_OTHERS
}

static void _cbFieldModification(jvmtiEnv* jvmti,
                                 JNIEnv* env,
                                 jthread thread,
                                 jmethodID method,
                                 jlocation location,
                                 jclass field_klass,
                                 jobject object,
                                 jfieldID field,
                                 char signature_type,
                                 jvalue new_value) __attribute__ ((__always_inline__));

static void
_cbFieldModification(jvmtiEnv *jvmti,
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
    r_event event;
    r_fieldRecord record;
    
    bzero(&event, sizeof(event));
    
    findFieldRecord(jvmti, env, field_klass, field, &record);
    
    event.type = RPROF_FIELD_WRITE;
    event.thread = get_tag(jvmti, thread);
    event.cid = record.id.id.cls;
    event.attr.fid = record.id.id.field;
    
    event.args_len = 2;
    event.args[0] = get_tag(jvmti, object);
    switch (signature_type) {
        case 'L':
        case '[':
            if (new_value.l != NULL) {
                event.args[1] = get_tag(jvmti, new_value.l);
            }
            break;
        default:
            event.args_len = 1;
            break;
    }
    
    comm_log(gdata->comm, &event);
}

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
    _SAVE_XMM
    
    _cbFieldModification(jvmti, env, thread, method, location, field_klass, object, field, signature_type, new_value);
    
    _RESTORE_XMM
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
	bzero(&data, sizeof(GlobalAgentData));
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
    
	/* Immediately after getting the jvmtiEnv* we need to ask for the
	 *   capabilities this agent will need.
	 */
	bzero(&capabilities, sizeof(capabilities));

	capabilities.can_generate_all_class_hook_events = 1;
	capabilities.can_tag_objects  = 1;
	capabilities.can_generate_object_free_events  = 1;
	/* capabilities.can_get_source_file_name  = 1; */
	/* capabilities.can_get_line_numbers  = 1; */
	capabilities.can_generate_vm_object_alloc_events  = 1;
	/* capabilities.can_access_local_variables = 1; */
	capabilities.can_generate_field_access_events = 1;
	capabilities.can_generate_field_modification_events = 1;

	error = (*jvmti)->AddCapabilities(jvmti, &capabilities);
	check_jvmti_error(jvmti, error, "Unable to get necessary JVMTI capabilities.");
    
	/* Next we need to provide the pointers to the callback functions to
	 *   to this jvmtiEnv*
	 */
	bzero(&callbacks, sizeof(callbacks));

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
	/* JVMTI_EVENT_CLASS_PREPARE_HOOK */
	callbacks.ClassPrepare = &cbClassPrepareHook;
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
	error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
                                               JVMTI_EVENT_CLASS_PREPARE, (jthread)NULL);
    check_jvmti_error(jvmti, error, "Cannot set event notification");
    
    /* perform our init */
    
	error = (*jvmti)->CreateRawMonitor(jvmti, "agent data", &(gdata->lock));
	check_jvmti_error(jvmti, error, "Cannot create agent data monitor");
    
    gdata->fields = fields_create(jvmti, "field table");
    gdata->classes = classes_create(jvmti, "classes list");
    gdata->comm = comm_create(jvmti, options);
    
	comm_started(gdata->comm);
    
	/* We return JNI_OK to signify success */
	return JNI_OK;
}

/* Agent_OnUnload: This is called immediately before the shared library is
 *   unloaded. This is the last code executed.
 */
JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm)
{
	comm_stopped(gdata->comm);
}
