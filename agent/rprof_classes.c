#include <stdio.h>
#include <string.h>
#include <err.h>
#include <stdlib.h>

#include "agent_util.h"
#include "rprof_classes.h"

#ifndef _RPROF_TEST

#define INITIAL_CLASS_TABLE_SIZE 64

#define LOCK(X) \
if (X == NULL) fatal_error("Class List is NULL"); \
else { _LOCK(X->jvmti, X->lock)

#define _LOCK(X,Y) \
check_jvmti_error(X, (*X)->RawMonitorEnter(X, Y), "Cannot enter monitor");

#define RELEASE(X) \
_RELEASE(X->jvmti, X->lock) }

#define _RELEASE(X, Y) \
check_jvmti_error(X, (*X)->RawMonitorExit(X, Y), "Cannot exit monitor");

#define CREATE_MONITOR(X, Y, Z) \
jvmtiError error; \
error = (*X)->CreateRawMonitor(X, Y, Z); \
check_jvmti_error(X, error, "Cannot create lock");

#define DESTORY_MONITOR(X, Y) \
(*X)->DestroyRawMonitor(X, Y);

#else

#define INITIAL_CLASS_TABLE_SIZE 4
#define LOCK(X) {
#define _LOCK(X,Y) {
#define RELEASE(X) }
#define _RELEASE(X, Y) }
#define CREATE_MONITOR(X, Y, Z)
#define DESTORY_MONITOR(X, Y)

#endif

struct _ClassListEntry {
    char *cname;
    jint cid;
    jint properties;
};

struct _ClassList {
    jvmtiEnv *jvmti;
    jrawMonitorID lock;
    size_t max;
    size_t size;
    struct _ClassListEntry *entries;
};

ClassList
classes_create(jvmtiEnv *jvmti, const char *name)
{
    ClassList list = NULL;
    
    list = allocate(jvmti, sizeof(struct _ClassList));
    
    list->jvmti = jvmti;
    
    CREATE_MONITOR(jvmti, name, &(list->lock))
    
    list->max = 0;
    list->size = 0;
    list->entries = NULL;
    
    return list;
}

static void
init(ClassList list, size_t new_size)
{
    size_t size = new_size * sizeof(struct _ClassListEntry);

    list->max = new_size;
    list->entries = allocate(list->jvmti, size);
}

void
classes_add(ClassList list, const char *cname, jint cid, jint properties)
{
    LOCK(list);
    
    if (list->entries == NULL) {
        init(list, INITIAL_CLASS_TABLE_SIZE);
        list->size = 0;
    }
    
    const size_t size = list->size;
    const size_t max = list->max;
    struct _ClassListEntry *entries = list->entries;
    struct _ClassListEntry *entry = NULL;
    
    /* copy class name into our memory */
    const size_t len = (strlen(cname) + 1) * sizeof(char);
    char *toStore = allocate(list->jvmti, len);
    memcpy(toStore, cname, len);

    /* increase available size of necessary */
    if (size >= max) {
        init(list, 2 * max);
        
        memcpy(list->entries, entries, max * sizeof(struct _ClassListEntry));
        bzero(entries, max * sizeof(struct _ClassListEntry));
        deallocate(list->jvmti, entries);
    }
    
    list->size++;
    
    entry = &(list->entries[size]);
    entry->cname = toStore;
    entry->cid = cid;
    entry->properties = properties;
    
    RELEASE(list);
}

void
classes_visit(ClassList list, jvmtiEnv *jvmti, JNIEnv *jni, ClassVisitor visit)
{
    LOCK(list);
    
    size_t i;
    struct _ClassListEntry *entry;
    
    for (i = 0; i < list->size; i++) {
        entry = &(list->entries[i]);
        (visit)(jvmti, jni, entry->cname, entry->cid, entry->properties);
    }
    
    RELEASE(list);
}

jboolean
classes_find(ClassList list, const char *name, jint *cid, jint *properties)
{
    struct _ClassListEntry *entry;
    size_t i;
    jboolean found = JNI_FALSE;
    const size_t size = list->size;
    
    LOCK(list);
    
    (*cid) = 0;
    (*properties) = 0;
    
    for (i = 0; i < size; i++) {
        entry = &(list->entries[i]);
        if (strcmp(name, entry->cname) == 0) {
            (*cid) = entry->cid;
            (*properties) = entry->properties;
            found = JNI_TRUE;
        }
    }
    
    RELEASE(list);
    
    return found;
}

void
classes_destroy(ClassList list)
{
    if (list == NULL) return;

    _LOCK(list->jvmti, list->lock);
    
    jrawMonitorID lock = list->lock;
    jvmtiEnv *jvmti = list->jvmti;
    size_t i;
    
    for (i = 0; i < list->size; i++) {
        deallocate(jvmti, list->entries[i].cname);
    }
    deallocate(jvmti, list->entries);
    
    bzero(list, sizeof(struct _ClassList));
    deallocate(jvmti, list);
    
    _RELEASE(jvmti, lock);
    
    DESTORY_MONITOR(jvmti, lock);
}
