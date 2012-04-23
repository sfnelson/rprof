#ifndef RPROF_UTIL_H
#define RPROF_UTIL_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>
#include <stdint.h>

#include "stringlist.h"

typedef union {
    struct {
        jshort  ds;
        jint    cls;
        jshort  field;
    } id;
    jlong raw;
} r_fieldID;

typedef union {
    struct {
        jshort  ds;
        jint    cls;
        jshort  field;
    } id;
    jlong raw;
} r_classID;

typedef struct {
    jlong class_tag;
	jfieldID field;
	jclass cls;
	r_fieldID id;
} r_fieldRecord;

struct _FieldTable;
typedef struct _FieldTable *FieldTable;

typedef StringList *r_classList;

/* Field Table */

FieldTable
fields_create(jvmtiEnv *jvmti, const char *name);

void
fields_store(FieldTable table, jvmtiEnv *jvmti,
             r_fieldRecord* toStore, size_t len);

void
fields_visit(FieldTable table, jvmtiEnv *jvmti,
             void (*callback) (r_fieldRecord*));

void
fields_find(FieldTable table, jvmtiEnv *jvmti,
            jlong class_tag, jfieldID fieldId, r_fieldRecord* target);

/* Class List */

void store_class(r_classList* classes, const char *cname);
void visit_classes(r_classList classes, jvmtiEnv* jvmti, JNIEnv *env,
    void (*callback) (jvmtiEnv* jvmti, JNIEnv *env, const char *cname));
void cleanup_classes(r_classList* table);

#endif
