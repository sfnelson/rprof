#include "rprof_fields.h"
#include "agent_util.h"

/* TODO this is not thread-safe :-( */

#define LOAD_FACTOR 0.7f

#define LOCK(X,Y) \
check_jvmti_error(X->jvmti, \
(*(X->jvmti))->RawMonitorEnter(X->jvmti, Y), \
"error requesting locking");

#define RELEASE(X,Y) \
check_jvmti_error(X->jvmti, \
(*(X->jvmti))->RawMonitorExit(X->jvmti, Y), \
"error releasing lock");

#define REQUEST_MAP(X, Y) \
LOCK(X, X->useLock) \
if (X->map == NULL) (*Y) = NULL; \
else { \
++(X->map->using); \
(*Y) = X->map; \
} \
RELEASE(X, X->useLock)

#define RELEASE_MAP(X, Y) \
LOCK(X, X->useLock) \
if (Y == NULL); \
else { \
--(Y->using); \
if (Y->free == JNI_TRUE && Y->using == 0) { \
bzero(Y->entries, Y->max * sizeof(r_fieldRecord)); \
deallocate(X->jvmti, Y->entries); \
bzero(Y, sizeof(struct f_map)); \
deallocate(X->jvmti, Y); } } \
RELEASE(X, X->useLock);

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
    jvmtiEnv       *jvmti;
    struct f_map   *map;
    jrawMonitorID   useLock;
    jrawMonitorID   changeLock;
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
fields_find(FieldTable table, jlong class_tag, jfieldID field,
            r_fieldRecord* target)
{
    struct f_map *map;
    
    REQUEST_MAP(table, &map);
    
    if (map != NULL) {
        memcpy(target, find(map, class_tag, field), sizeof(r_fieldRecord));
    }
    else {
        bzero(target, sizeof(r_fieldRecord));
    }
    
    RELEASE_MAP(table, map);
}

void
fields_visit(FieldTable table,  void (*callback) (r_fieldRecord*))
{
    size_t i, max;
    struct f_map *map;
    r_fieldRecord *tmp;
    
    REQUEST_MAP(table, &map);
    
    if (map != NULL) {
        max = map->max;
        for (i = 0; i < max; i++) {
            tmp = &(map->entries[i]);
            if (tmp->field != NULL) {
                (callback)(tmp);
            }
        }
    }
    
    RELEASE_MAP(table, map);
}

void
fields_store(FieldTable table, r_fieldRecord *toStore, size_t len)
{
    size_t size, max, i, old;
    struct f_map *old_map, *new_map;
    r_fieldRecord *in, *out;
    
    LOCK(table, table->changeLock);
    
    REQUEST_MAP(table, &old_map);
    
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
    
    new_map = allocate(table->jvmti, sizeof(struct f_map));
    if (new_map == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
        return;
    }
    
    new_map->size = size;
    new_map->max = max;
    new_map->entries = allocate(table->jvmti, max * sizeof(r_fieldRecord));
    if (new_map->entries == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
    }
    
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
    if (old_map != NULL) old_map->free = JNI_TRUE;
    RELEASE_MAP(table, old_map);
    
    RELEASE(table, table->changeLock);
}

FieldTable
fields_create(jvmtiEnv *jvmti, const char *name)
{
    FieldTable table;
    jvmtiError error;
    size_t len;
    
    len = strlen(name);

    char use[len + 6 + 1];
    char change[len + 9 + 1];
    
    sprintf(use, "%s - use", name);
    sprintf(change, "%s - change", name);
    
    table = allocate(jvmti, sizeof(struct _FieldTable));
    if (table == NULL) {
        fatal_error("unable to allocate field table");
        return NULL;
    }
    
    table->jvmti = jvmti;
    
    error = (*jvmti)->CreateRawMonitor(jvmti, use, &(table->useLock));
    if (error != JVMTI_ERROR_NONE) {
        fatal_error("unable to create use monitor for field table");
        return NULL;
    }
    
    error = (*jvmti)->CreateRawMonitor(jvmti, change, &(table->changeLock));
    if (error != JVMTI_ERROR_NONE) {
        fatal_error("unable to create change monitor for field table");
        return NULL;
    }
    
    return table;
}
