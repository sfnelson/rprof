#ifndef RPROF_EVENTS_H
#define RPROF_EVENTS_H

#define RPROF_OBJECT_ALLOCATED 0x1
#define RPROF_ARRAY_ALLOCATED 0x2
#define RPROF_METHOD_ENTER 0x4
#define RPROF_METHOD_RETURN 0x8
#define RPROF_FIELD_READ 0x10
#define RPROF_FIELD_WRITE 0x20
#define RPROF_CLASS_WEAVE 0x40
#define RPROF_CLASS_INITIALIZED 0x80
#define RPROF_OBJECT_TAGGED 0x100
#define RPROF_OBJECT_FREED 0x200
#define RPROF_METHOD_EXCEPTION 0x400

typedef struct {
    jlong id;
    jlong thread;
    jint type;
    jint cid;
    union {
        jint mid;
        jint fid;
    } attr;
    jint args_len;
    jlong args[];
} EventRecord;

#endif
