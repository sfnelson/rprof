#ifndef RPROF_FIELDS_H
#define RPROF_FIELDS_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>
#include <stdint.h>

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

FieldTable
fields_create(jvmtiEnv *jvmti, const char *name);

void
fields_store(FieldTable table, r_fieldRecord* toStore, size_t len);

void
fields_visit(FieldTable table, void (*callback) (r_fieldRecord*));

void
fields_find(FieldTable table,  jlong class_tag, jfieldID fieldId,
            r_fieldRecord* target);

#endif
