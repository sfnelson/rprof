#include <stdio.h>
#include <string.h>
#include <err.h>
#include <stdlib.h>

#include "agent_util.h"
#include "rprof_classes.h"

#define LOCK(X) \
if (X == NULL) fatal_error("Class List is NULL"); \
else { _LOCK(X->jvmti, X->lock)

#define _LOCK(X,Y) \
check_jvmti_error(X, (*X)->RawMonitorEnter(X, Y), "Cannot enter monitor");

#define RELEASE(X) \
_RELEASE(X->jvmti, X->lock) }

#define _RELEASE(X, Y) \
check_jvmti_error(X, (*X)->RawMonitorExit(X, Y), "Cannot exit monitor");

struct _ClassList {
    jvmtiEnv *jvmti;
    jrawMonitorID lock;
    size_t max;
    size_t size;
    char **entries;
};

ClassList
classes_create(jvmtiEnv *jvmti, const char *name)
{
    ClassList list = NULL;
    jvmtiError error;
    
    list = allocate(jvmti, sizeof(struct _ClassList));
    
    list->jvmti = jvmti;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, name, &(list->lock));
    check_jvmti_error(jvmti, error, "Cannot create lock");
    
    return list;
}

static void
init(ClassList list, size_t new_size)
{
    size_t size = new_size * sizeof(char*);

    list->max = new_size;
    list->size = 0;
    list->entries = allocate(list->jvmti, size);
}

void
classes_add(ClassList list, const char *cname)
{
    LOCK(list);
    
    const size_t size = list->size;
    const size_t max = list->max;
    char **current = list->entries;
    size_t i;
    
    const size_t len = (strlen(cname) + 1) * sizeof(char);
    char *toStore = allocate(list->jvmti, len);
    memcpy(toStore, cname, len);

    if (current == NULL) {
        init(list, 64);
    }
    else if (size < max) {
        list->entries[(list->size)++] = toStore;
    }
    else {
        init(list, 2 * max);
        
        for (i = 0; i < size; i++) {
            list->entries[(list->size)++] = current[i];
        }
        
        deallocate(list->jvmti, current);
        
        list->entries[(list->size)++] = toStore;
    }
    
    RELEASE(list);
}

void
classes_visit(ClassList list, jvmtiEnv *jvmti, JNIEnv *jni, ClassVisitor visit)
{
    LOCK(list);
    
    size_t i;
    
    for (i = 0; i < list->size; i++) {
        (visit)(jvmti, jni, list->entries[i]);
    }
    
    RELEASE(list);
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
        deallocate(jvmti, list->entries[i]);
    }
    deallocate(jvmti, list->entries);
    
    bzero(list, sizeof(struct _ClassList));
    deallocate(jvmti, list);
    
    _RELEASE(jvmti, lock);
    
    (*jvmti)->DestroyRawMonitor(jvmti, lock);
}
