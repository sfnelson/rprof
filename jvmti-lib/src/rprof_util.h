#ifndef RPROF_UTIL_H
#define RPROF_UTIL_H

#include <sys/types.h>
#include <jni.h>
#include <jvmti.h>
#include <stdint.h>
#include <stringlist.h>

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

struct _r_fieldTable;
typedef struct _r_fieldTable *r_fieldTable;

typedef StringList *r_classList;

void store_fields(r_fieldTable* table, r_fieldRecord* toStore, uint32_t len);
void visit_fields(r_fieldTable table, void (*callback) (r_fieldRecord*));
void find_field(r_fieldTable table, jlong class_tag, jfieldID fieldId, r_fieldRecord* target);
void cleanup_fields(r_fieldTable* table);

void store_class(r_classList* classes, const char *cname);
void visit_classes(r_classList classes, jvmtiEnv* jvmti, JNIEnv *env,
    void (*callback) (jvmtiEnv* jvmti, JNIEnv *env, const char *cname));
void cleanup_classes(r_classList* table);

#endif