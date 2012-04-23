#ifndef _RPROF_CLASSES_H
#define _RPROF_CLASSES_H

#include <jvmti.h>

struct _ClassList;
typedef struct _ClassList *ClassList;

ClassList
classes_create(jvmtiEnv* jvmti, const char *name);

void
classes_add(ClassList classes, const char *cname);

typedef void (*ClassVisitor) (jvmtiEnv *jvmti, JNIEnv *jni, const char *cname);

void
classes_visit(ClassList classes, jvmtiEnv *jvmti, JNIEnv *jni,
              ClassVisitor callback);

void
classes_destroy(ClassList classes);

#endif /* _RPROF_CLASSES_H */
