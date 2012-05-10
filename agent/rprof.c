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
#define HEAP_TRACKER_clinit			clinit
#define HEAP_TRACKER_native_clinit	_clinit
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

#define _GetClassName(J, C, N) \
char *cname_orig; \
check_jvmti_error(J, (*J)->GetClassSignature(J, C, &(cname_orig), NULL), \
"error getting class name");\
if (cname_orig[0] == 'L') { N = &cname_orig[1]; } else { N = cname_orig; }\
if (cname_orig[strlen(cname_orig)-1] == ';') { cname_orig[strlen(cname_orig)-1] = 0; } \
{

#define _FreeClassName(J, N) \
} \
check_jvmti_error(J, (*J)->Deallocate(J, (unsigned char *)cname_orig), "error deallocating class name");


#define _GetFieldName(J, C, F, N) \
char *fname_orig; \
check_jvmti_error(J, (*J)->GetFieldName(J, C, F, &(fname_orig), NULL, NULL), \
"error getting field name");\
N = fname_orig;\
{

#define _FreeFieldName(J, N) \
} \
check_jvmti_error(J, (*J)->Deallocate(J, (unsigned char *)fname_orig), "error deallocating field name");



#define CreateEvent(J, L) \
allocate(J, sizeof(EventRecord) + L * sizeof(jlong))

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
    
	error = (*jvmti)->SetFieldAccessWatch(jvmti, record->cls, record->field);
	check_jvmti_error(jvmti, error, "Cannot add access watch to field");
	error = (*jvmti)->SetFieldModificationWatch(jvmti, record->cls, record->field);
	check_jvmti_error(jvmti, error, "Cannot add modify watch to field");
}

/* Java Native Mathoc for _rprof_class_init */
static void
HEAP_TRACKER_native_newcls(JNIEnv *env, jclass tracker,
                           jobject klass, jint cid, jintArray toWatch)
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
	EventRecord *event;
    
    jvmti = gdata->jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	tag = get_tag(jvmti, klass);
    if ((tag & RPROF_CLASS_THREAD_ID) == RPROF_CLASS_THREAD_ID) {
        char *cname;
        _GetClassName(jvmti, klass, cname)
        fatal_error("trying to tag class %s more than once! (%d, %d)\n",
                    cname, TAG_TO_CNUM(tag), cid);
        _FreeClassName(jvmti, cname)
    }
    
	tag = tag_class(jvmti, klass, cid);
    
    event = CreateEvent(jvmti, 1);
    
    event->type = RPROF_CLASS_INITIALIZED;
    event->thread = 0;
    event->cid = cid;
    event->attr.mid = 0;
    event->args_len = 1;
    event->args[0] = tag;
    
	comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
    
    /* if there are no fields to watch we're done now */
	if (toWatch == NULL) return;
    
    /* ---------------
     * Field Tracking
     * --------------- */

    size_t len = (size_t) (*env)->GetArrayLength(env, toWatch);
    targetFields = (*env)->GetIntArrayElements(env, toWatch, NULL);     /* (1) */
    
    error = (*jvmti)->GetClassFields(jvmti, klass, &numFields, &fields);/* (2) */
    check_jvmti_error(jvmti, error, "cannot get fields for new class");
    
    toStore = allocate(jvmti, sizeof(r_fieldRecord) * len);             /* (3) */
    
    klass = (*env)->NewGlobalRef(env, klass); /* saving for ever in field table */
    
    for (i = 0; i < len; i++) {
        record = &(toStore[i]);
        record->id.id.cls = (jint) cid;
        record->id.id.field = (jshort) targetFields[i];
        record->cls = klass;
        record->class_tag = tag;
        /* Fields begin at 1 in our profiler and 0 in jvmti */
        record->field = fields[targetFields[i] - 1];
    }
    
    /* always store fields, watches are added later if we're not ready yet */
    fields_store(gdata->fields, toStore, len);
    
    /* don't watch fields until after they've been inserted into the map */
    if (gdata->vmInitialized) {
        for (i = 0; i < len; i++) {
            watch_field(&(toStore[i]));
        }
    }
    
    /* cleanup */
    (*env)->ReleaseIntArrayElements(env, toWatch, targetFields,         /* (1) */
                                    JNI_ABORT);
    error = (*jvmti)->Deallocate(jvmti, (unsigned char*) fields);       /* (2) */
    check_jvmti_error(jvmti, error, "could not deallocate fields\n");
    deallocate(jvmti, toStore);                                         /* (3) */
}

/* Java Native Method for Object.<init> */
static void
HEAP_TRACKER_native_newobj(JNIEnv *env, jclass tracker, jthread thread,
                           jclass klass, jobject o, jlong id)
{
    EventRecord *event;
    jlong type;
    jvmtiEnv *jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
    jvmti = gdata->jvmti;
    
	/*if (id == 0) { todo not using java id generation because it's broken */
    enterCriticalSection(jvmti); {
	    id = generate_object_tag();
    }; exitCriticalSection(jvmti);
	/*}*/
    
	tag_object(jvmti, o, id);
	type = get_tag(jvmti, klass);
    
    if (type == 0) {
        char *cname;
        jboolean untagged = JNI_FALSE;
        jclass array;
        _GetClassName(jvmti, klass, cname)
        if (cname[0] == '[') {
            /* tag array class and continue */
            array = (*env)->FindClass(env, "java/lang/reflect/Array");
            type = get_tag(jvmti, array);
        }
        else {
#ifdef DEBUG
            stdout_message("---- untagged class %s for object %llx\n", cname, id);
#endif
            untagged = JNI_TRUE;
        }
        _FreeClassName(jvmti, cname);
        if (untagged) {
            return;
        }
    }
    
    event = CreateEvent(jvmti, 1);

	event->type = RPROF_OBJECT_ALLOCATED;
	event->thread = get_tag(jvmti, thread);
	event->cid = TAG_TO_CNUM(type);
    event->attr.mid = 0;
	event->args_len = 1;
	event->args[0] = id;
    
	comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

/* Java Native Method for newarray */
static void
HEAP_TRACKER_native_newarr(JNIEnv *env, jclass tracker, jthread thread,
                           jobject a, jlong id)
{
    EventRecord *event;
    jvmtiEnv *jvmti;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	jvmti = gdata->jvmti;
    
	if (id == 0) {
        id = generate_object_tag();
    }
    
	tag_object(jvmti, a, id);
    
    event = CreateEvent(jvmti, 1);

    event->type = RPROF_ARRAY_ALLOCATED;
    event->thread = get_tag(jvmti, thread);
    event->cid = 0;
    event->attr.mid = 0;
    event->args_len = 1;
    event->args[0] = id;
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

/* Java Native Method for method execution */
static void
HEAP_TRACKER_native_enter(JNIEnv *env, jclass tracker, jthread thread,
                          jint cnum, jint mnum, jarray args)
{
	jvmtiEnv *jvmti;
    EventRecord *event;
	size_t i, len = 0;
    
	if ( gdata->vmDead ) {
		return;
	}
    
	jvmti = gdata->jvmti;
    
    if (args != NULL) {
        len = (size_t) ((*env)->GetArrayLength(env, args));
    }
    
    event = CreateEvent(jvmti, len);
    
    event->type = RPROF_METHOD_ENTER;
    event->thread = get_tag(jvmti, thread);
    event->cid = cnum;
    event->attr.mid = mnum;
    event->args_len = (jint) len;
    
    for (i = 0; i < len; i++) {
        jobject o = (*env)->GetObjectArrayElement(env, args, (jint) i);
        event->args[i] = get_tag(jvmti, o);
	}
    
	comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

/* Java Native Method for method return */
static void
HEAP_TRACKER_native_exit(JNIEnv *env, jclass tracker, jthread thread,
                         jint cnum, jint mnum, jobject arg)
{
    EventRecord *event;
    jvmtiEnv *jvmti;
    
    if ( gdata->vmDead ) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    event = CreateEvent(jvmti, 1);
    
    event->type = RPROF_METHOD_RETURN;
    event->thread = get_tag(jvmti, thread);
    event->cid = cnum;
    event->attr.mid = mnum;
    event->args_len = 0;
    
    if (arg != NULL) {
        event->args_len = 1;
        event->args[0] = get_tag(jvmti, arg);
    }
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

/* Java Native Method for method exceptional return */
static void
HEAP_TRACKER_native_except(JNIEnv *env, jclass tracker, jthread thread,
                           jint cnum, jint mnum, jobject arg, jobject thrown)
{
    EventRecord *event;
    jvmtiEnv *jvmti;

    if ( gdata->vmDead ) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    event = CreateEvent(jvmti, 2);
    
    event->type = RPROF_METHOD_EXCEPTION;
    event->thread = get_tag(jvmti, thread);
    event->cid = cnum;
    event->attr.mid = mnum;
    event->args_len = 0;

    if (arg != NULL) {
        event->args_len = 1;
        event->args[0] = get_tag(jvmti, arg);
    }
    
    if (thrown != NULL) {
        event->args_len = 2;
        event->args[1] = get_tag(jvmti, thrown);
        if (arg == NULL) {
            event->args[0] = 0;
        }
    }
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

static void
HEAP_TRACKER_native_main(JNIEnv *env, jclass klass, jthread thread, jint cnum, jint mnum)
{
    EventRecord *event;
    jvmtiEnv *jvmti;
    
    if ( gdata->vmDead ) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    event = CreateEvent(jvmti, 0);
    
    event->type = RPROF_METHOD_ENTER;
    event->thread = get_tag(gdata->jvmti, thread);
    event->cid = cnum;
    event->attr.mid = mnum;
    event->args_len = 0;
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
}

/* Called by cbClassTagger, cbClassPrepareHook, and _clinit to tag and initialize class */
static void
handle_class(jvmtiEnv *jvmti, JNIEnv *env, jclass klass, const char *cname,
          jint cid, jint props)
{
    jmethodID mid;
    
    jlong tag = get_tag(gdata->jvmti, klass);
    
    if ((tag & RPROF_CLASS_THREAD_ID) == RPROF_CLASS_THREAD_ID) {
        stdout_message("---- ignoring %s\n", cname);
        return; /* already tagged */
    }
    
    /* If the class doesn't have an agent init method we still need to ID it.
     * Fake a call to _newcls without watches*/
    if ((props & HAS_RINIT) == 0) {
        /* stdout_message("---- faking agent init for %s\n", cname); */
        HEAP_TRACKER_native_newcls(env, NULL, klass, cid, NULL);
        return;
    }
    
    /* If we've got this far it's safe to call the tracker method to get field
     * watches. */
    mid = (*env)->GetStaticMethodID(env, klass, "_rprof_agent_init", "()V");
    if (mid == NULL) {
        (*env)->ExceptionClear(env);
        fatal_error("class %s does not have an agent init method!\n", cname);
        return;
    }
    
    (*env)->CallStaticVoidMethod(env, klass, mid);
}

/* Called from cbVMInit when ready to start tracking */
static void
cbClassTagger(jvmtiEnv *jvmti, JNIEnv *env,
              const char* cname, jint cid, jint props)
{
    jclass klass;
    jint status;
    jvmtiError error;
    
    klass = (*env)->FindClass(env, cname);
    if (klass == NULL) {
        fatal_error("---- could not locate class %s\n", cname);
        return;
    }
    
    error = (*jvmti)->GetClassStatus(jvmti, klass, &status);
    check_jvmti_error(jvmti, error, "Unable to get class status!");
    
    if ((status & JVMTI_CLASS_STATUS_INITIALIZED) == 0) {
        fatal_error("---- class %s not ready (%x)\n", cname, status);
        return;
    }
    
    handle_class(jvmti, env, klass, cname, cid, props);
}

/* Called by JVM when class is ready to use (before <clinit>) */
static void
cbClassPrepareHook(jvmtiEnv *jvmti, JNIEnv* env, jthread thread, jclass klass) {
    jvmtiError error;
    jint access, cid, props;
    char *cname;
    
    if (gdata->vmDead || !gdata->vmInitialized) return;
    
    error = (*jvmti)->GetClassModifiers(jvmti, klass, &access);
    check_jvmti_error(jvmti, error, "could not get class access flags");
    
    if ((access & ACC_INTERFACE) != 0) {
        return; /* interface, ignore */
    }
    
    _GetClassName(jvmti, klass, cname);

    if (!classes_find(gdata->classes, cname, &cid, &props)) {
        fatal_error("could not find class %s in class table!", cname);
    }

    /* Searching for rinit method will cause <clinit> to trigger if it is
     * present. <clinit> will call rinit itself so avoid calling twice */

    if ((props & HAS_CLINIT) == 0) {
        handle_class(jvmti, env, klass, cname, cid, props);
    }

    _FreeClassName(jvmti, cname);
}

/* Java Native Method for <clinit> */
static void
HEAP_TRACKER_native_clinit(JNIEnv *env, jclass tracker, jobject klass)
{
    char *cname;
    jint cid, props;
    jvmtiEnv *jvmti;

    if (gdata->vmDead || !gdata->vmInitialized) {
        return;
    }
    
    jvmti = gdata->jvmti;
    
    _GetClassName(jvmti, klass, cname);
    
    if (!classes_find(gdata->classes, cname, &cid, &props)) {
        fatal_error("could not find class %s in class table!\n", cname);
    }
    
    handle_class(jvmti, env, klass, cname, cid, props);
    
    _FreeClassName(jvmti, cname);
}

/* call to engage should match phase set in cbVMObjectAlloc */
static void engage(jvmtiEnv *jvmti, JNIEnv *env)
{
    jclass klass;
    jfieldID field;
    
    klass = (*env)->FindClass(env, STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
    if ( klass == NULL ) {
        fatal_error("ERROR: JNI: Cannot find %s with FindClass\n",
                    STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
    }
    
    /* Engage calls. */    
    field = (*env)->GetStaticFieldID(env, klass, STRING(HEAP_TRACKER_engaged), "I");
    if ( field == NULL ) {
        fatal_error("ERROR: JNI: Cannot get field from %s\n",
                    STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
    }
    (*env)->SetStaticIntField(env, klass, field, 1);
}

/* Callback for JVMTI_EVENT_VM_START */
static void
cbVMStart(jvmtiEnv *jvmti, JNIEnv *env)
{
	enterCriticalSection(jvmti); {
		jclass klass;
		jint rc;
        
		/* Java Native Methods for class */
		static JNINativeMethod registry[8] = {
            {STRING(HEAP_TRACKER_native_newobj), "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newobj},
            {STRING(HEAP_TRACKER_native_newarr), "(Ljava/lang/Object;Ljava/lang/Object;J)V", (void*)&HEAP_TRACKER_native_newarr},
            {STRING(HEAP_TRACKER_native_enter), "(Ljava/lang/Object;II[Ljava/lang/Object;)V", (void*)&HEAP_TRACKER_native_enter},
            {STRING(HEAP_TRACKER_native_exit), "(Ljava/lang/Object;IILjava/lang/Object;)V", (void*)&HEAP_TRACKER_native_exit},
            {STRING(HEAP_TRACKER_native_except), "(Ljava/lang/Object;IILjava/lang/Object;Ljava/lang/Object;)V", (void*)&HEAP_TRACKER_native_except},
            {STRING(HEAP_TRACKER_native_main), "(Ljava/lang/Object;II)V", (void*)&HEAP_TRACKER_native_main},
            {STRING(HEAP_TRACKER_native_newcls), "(Ljava/lang/Object;I[I)V", (void*)&HEAP_TRACKER_native_newcls},
            {STRING(HEAP_TRACKER_native_clinit), "(Ljava/lang/Object;)V", (void*)&HEAP_TRACKER_native_clinit}
		};
        
		/* Register Natives for class whose methods we use */
		klass = (*env)->FindClass(env, STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		if ( klass == NULL ) {
			fatal_error("ERROR: JNI: Cannot find %s with FindClass\n",
                        STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
        
		rc = (*env)->RegisterNatives(env, klass, registry, 8);
		if ( rc != 0 ) {
			fatal_error("ERROR: JNI: Cannot register natives for class %s\n",
                        STRING(HEAP_TRACKER_package/HEAP_TRACKER_class));
		}
        
        /*we can start receiving callbacks now, but we don't need to */
        /*engage(jvmti, env);*/
        
		/* Indicate VM has started */
		gdata->vmStarted = JNI_TRUE;
        
	} exitCriticalSection(jvmti);
}

/* Iterate Through Heap callback */
static jint
cbObjectTagger(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* userData)
{
	EventRecord *event;
    jvmtiEnv *jvmti;
    
	if (length != -1) {
		/* TODO: stop ignoring arrays! */
		return JVMTI_VISIT_OBJECTS;
	}
    
	if (*tag_ptr == 0) {
	    /* this is equivalent to calling tag_object() */
		*tag_ptr = generate_object_tag();
	}
    
    if (class_tag == 0 || TAG_TO_CNUM(class_tag) == 0) {
        fatal_error("untagged class %llx! (%llx)\n", class_tag, *tag_ptr);
    }
    
    jvmti = gdata->jvmti;
    
    event = CreateEvent(jvmti, 1);
    
    event->type = RPROF_OBJECT_TAGGED;
    event->thread = 0;
    event->cid = TAG_TO_CNUM(class_tag);
    event->attr.mid = 0;
	event->args_len = 1;
	event->args[0] = *tag_ptr;
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
    
	return JVMTI_VISIT_OBJECTS;
}

/* Callback for JVMTI_EVENT_VM_INIT */
static void
cbVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread)
{
	jvmtiHeapCallbacks heapCallbacks;
	jvmtiError         error;
    
#ifdef DEBUG
	stdout_message("----- trying gc at init\n");
#endif
    
	error = (*jvmti)->ForceGarbageCollection(jvmti);
	check_jvmti_error(jvmti, error, "Cannot force garbage collection");
    
#ifdef DEBUG
	stdout_message("----- gc ok on init\n");
#endif
    
    enterCriticalSection(jvmti); {
        
        classes_visit(gdata->classes, jvmti, env, &cbClassTagger);
        
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
        
        engage(jvmti, env);
        
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
    /* don't start too early - this should match engage call */
    if (gdata->vmInitialized) {
        HEAP_TRACKER_native_newobj(env, klass, thread, klass, o, 0);
    }
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
    jint           properties;
    
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
            properties = 0;
            
            comm_weave(gdata->comm, classname, systemClass,
                       class_len, class_data,
                       new_class_len, new_class_data,
                       &class_id, &properties);
            
#ifdef DEBUG
            storeClass(classname, class_data, (size_t) class_len, *new_class_data, (size_t) *new_class_len);
#endif
            
            if (class_id > 0) {
                classes_add(gdata->classes, classname, class_id, properties);
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
        _GetClassName(jvmti, field_klass, cname)
        _GetFieldName(jvmti, field_klass, field, fname)
        class_tag = get_tag(jvmti, field_klass);
        fatal_error("could not find field %s.%s (%llx.%llx), found (%llx.%llx)\n",
                    cname, fname,
                    class_tag, field,
                    record->class_tag, record->field);
        _FreeFieldName(jvmti, fname)
        _FreeClassName(jvmti, cname)
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
    EventRecord *event;
    r_fieldRecord record;
    
    findFieldRecord(jvmti, env, field_klass, field, &record);
    
    event = CreateEvent(jvmti, 1);
    
    event->thread = get_tag(jvmti, thread);
    event->type = RPROF_FIELD_READ;
    event->cid = record.id.id.cls;
    event->attr.fid = record.id.id.field;
    event->args_len = 1;
    event->args[0] = get_tag(jvmti, object);
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
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
    EventRecord *event;
    r_fieldRecord record;
    
    findFieldRecord(jvmti, env, field_klass, field, &record);
    
    event = CreateEvent(jvmti, 2);
    
    event->type = RPROF_FIELD_WRITE;
    event->thread = get_tag(jvmti, thread);
    event->cid = record.id.id.cls;
    event->attr.fid = record.id.id.field;
    event->args_len = 2;
    event->args[0] = get_tag(jvmti, object);
    switch (signature_type) {
        case 'L':
        case '[':
            if (new_value.l != NULL) {
                event->args[1] = get_tag(jvmti, new_value.l);
            }
            break;
        default:
            event->args_len = 1;
            break;
    }
    
    comm_log(gdata->comm, event);
    
    deallocate(jvmti, event);
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
	/* capabilities.can_generate_object_free_events  = 1; */
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
	/* callbacks.ObjectFree        = &cbObjectFree; */
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
	/* error = (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
     JVMTI_EVENT_OBJECT_FREE, (jthread)NULL);
     check_jvmti_error(jvmti, error, "Cannot set event notification"); */
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
