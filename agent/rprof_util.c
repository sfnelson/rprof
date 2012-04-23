#include "rprof_util.h"
#include "agent_util.h"

/* TODO this is not thread-safe :-( */

#define LOAD_FACTOR 0.7f

#define LOCK(X,Y) \
	jvmtiError error; \
	error = (*X)->RawMonitorEnter(X, Y); \
	check_jvmti_error(X, error, "Cannot enter with raw monitor"); {

#define RELEASE(X,Y) \
    }; error = (*X)->RawMonitorExit(X, Y); \
	check_jvmti_error(X, error, "Cannot exit with raw monitor");

#define USING(X, Y, Z) \
    ++(Y->map->using); (*Z) = Y->map; {

#define DONE(X, Y, Z) \
    }; if (map->free == JNI_TRUE && map->using == 1) { LOCK(X, Y->lock) \
        free(map->entries); \
        free(map); \
    RELEASE(X, Y->lock); } else { --(map->using); };

typedef struct {
    jfieldID field;
    jint fid;
} FieldEntry;

struct f_map {
    volatile size_t using;
    volatile jboolean free;
    size_t size;
    size_t max;
    r_fieldRecord *entries;
};

struct _FieldTable {
    struct f_map    *map;
    jrawMonitorID   lock;
};

typedef struct {
    jlong cid;
    jclass cls;
    size_t num_fields;
    FieldEntry *fields;
} ClassEntry;

typedef struct _r_class_table {
    size_t size;
    size_t max;
    ClassEntry *classes;
} ClassTable;

static r_fieldRecord *
find(struct f_map *map, jlong class_tag, jfieldID field)
{
    r_fieldRecord *result;
    size_t key, max, index, i;
	size_t fieldAsInt = (size_t) field;
    size_t classAsInt = (size_t) class_tag;

    max = map->max;
    
    key = fieldAsInt;
    key ^= fieldAsInt >> 20;
    key ^= fieldAsInt >> 12;
    key ^= fieldAsInt >> 7;
    key ^= fieldAsInt >> 4;
    key ^= classAsInt;
    key ^= classAsInt >> 20;
    key ^= classAsInt >> 12;
    key ^= classAsInt >> 7;
    key ^= classAsInt >> 4;
	key &= (max - 1);

	index = 0;
	while (JNI_TRUE) {
         /* quad probing, guaranteed not to repeat before $max hops */
		i = ((size_t)(key + index/2.0f + index*index/2.0f)) % max;
		result = &(map->entries[i]);
		if (result->field == NULL) {
            break; /* empty */
        }
		if (result->class_tag == class_tag && result->field == field) {
            break; /* found */
        }

		index++;

        if (index >= max) {
            fatal_error("ERROR: probed all entries without finding field\n");            
            return NULL;
        }
	}
    
    return result;
}

void
fields_find(FieldTable table, jvmtiEnv *jvmti,
            jlong class_tag, jfieldID field, r_fieldRecord* target)
{
    struct f_map *map;
    
    USING(jvmti, table, &map);
    
    memcpy(target, find(table->map, class_tag, field), sizeof(r_fieldRecord));
    
    DONE(jvmti, table, map);
}

void
fields_visit(FieldTable table, jvmtiEnv *jvmti,
             void (*callback) (r_fieldRecord*))
{
    size_t i, max;
    r_fieldRecord *tmp;
    struct f_map *map;
    
    USING(jvmti, table, &map);
    
    max = map->max;
    for (i = 0; i < max; i++) {
        tmp = &(map->entries[i]);
        if (tmp->field != NULL) {
            (callback)(tmp);
        }
    }
    
    DONE(jvmti, table, map);
}

void
fields_store(FieldTable table, jvmtiEnv *jvmti, r_fieldRecord *toStore, size_t len)
{
    size_t size, max, i, old;
    struct f_map *old_map, *new_map;
    r_fieldRecord *in, *out;

    LOCK(jvmti, table->lock);

    old_map = table->map;

    if (old_map != NULL) {
        size = old_map->size + len;
        old = max = old_map->max;
    }
    else {
        size = len;
        max = 1;
        old = 0;
    }

    while (size > LOAD_FACTOR * max) max *= 2;

    new_map = malloc(sizeof(struct f_map));
    if (new_map == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
        return;
    }

    new_map->size = size;
    new_map->max = max;
    new_map->entries = malloc(max * sizeof(r_fieldRecord));
    if (new_map->entries == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
    }

    bzero(new_map->entries, max * sizeof(r_fieldRecord));

    for (i = 0; i < old; i++) {
        in = &(old_map->entries[i]);
        if (in->field != NULL) {
            out = find(new_map, in->class_tag, in->field);
            memcpy(out, in, sizeof(r_fieldRecord));
        }
    }

    for (i = 0; i < len; i++) {
        in = &(toStore[i]);
        if (in->field != NULL) {
            out = find(new_map, in->class_tag, in->field);
            memcpy(out, in, sizeof(r_fieldRecord));
        }
    }

    table->map = new_map;

    if (old_map != NULL) {
        if (old_map->using == 0) {
            free(old_map->entries);
            free(old_map);
        }
        else {
            old_map->free = JNI_TRUE;
        }
    }
    
    RELEASE(jvmti, table->lock);
}

FieldTable
fields_create(jvmtiEnv *jvmti, const char *name)
{
    FieldTable table;
    jvmtiError error;

    table = malloc(sizeof(struct _FieldTable));
    if (table == NULL) {
        fatal_error("unable to allocate field table");
        return NULL;
    }
    table->map = NULL;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, name, &(table->lock));
    if (error != JVMTI_ERROR_NONE) {
        fatal_error("unable to create monitor for field table");
        return NULL;
    }
    
    return table;
}

/* ==== Class List Functions */

void store_class(r_classList* classes, const char *cname)
{
    if ((*classes) == NULL) {
        (*classes) = sl_init();
    }

    sl_add(*classes, cname);
}

void visit_classes(r_classList classes, jvmtiEnv* jvmti, JNIEnv *env,
    void (*callback) (jvmtiEnv* jvmti, JNIEnv *env, const char *cname))
{
    size_t i;
    if (classes == NULL) return;
    for (i = 0; i < classes->sl_cur; i++) {
        (callback) (jvmti, env, classes->sl_str[i]);
    }
}

void cleanup_classes(r_classList* classes)
{
    if ((*classes) == NULL) return;
    sl_free(*classes, 1);
    *classes = NULL;
}
