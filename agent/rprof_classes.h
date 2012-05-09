#ifndef _RPROF_CLASSES_H
#define _RPROF_CLASSES_H

#include <jvmti.h>

/* sync with nz.ac.vuw.ecs.rprof.server.domain.Clazz */
#define CLASS_VERSION_UPDATED   0x1
#define SPECIAL_CLASS_WEAVER    0x2
#define CLASS_INCLUDE_MATCHED   0x4
#define CLASS_EXCLUDE_MATCHED   0x8
#define COLLECTION_MATCHED      0x10
#define GENERATED_MATCHED       0x20
#define HAS_CLINIT              0x40
#define HAS_RINIT               0x80

struct _ClassList;
typedef struct _ClassList *ClassList;

ClassList
classes_create(jvmtiEnv* jvmti, const char *name);

void
classes_add(ClassList classes, const char *cname, jint cid, jint properties);

typedef void (*ClassVisitor) (jvmtiEnv *jvmti, JNIEnv *jni,
                              const char *cname, jint cid, jint properties);
void
classes_visit(ClassList classes, jvmtiEnv *jvmti, JNIEnv *jni,
              ClassVisitor callback);

jboolean
classes_find(ClassList classes, const char *cname, jint *cid, jint *properties);

void
classes_destroy(ClassList classes);

#endif /* _RPROF_CLASSES_H */
