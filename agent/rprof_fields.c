#include "rprof_fields.h"
#include "agent_util.h"

/* TODO this is not thread-safe :-( */

#define LOAD_FACTOR 0.7

#define LOCK(J, L) \
check_jvmti_error(J, (*J)->RawMonitorEnter(J, L), "cannot get monitor");

#define RELEASE(J, L) \
check_jvmti_error(J, (*J)->RawMonitorExit(J, L), "cannot release monitor");

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

static struct f_map *
map_create(FieldTable table, size_t size)
{
    struct f_map * map;
    
    map = allocate(table->jvmti, sizeof(struct f_map));
    bzero(map, sizeof(struct f_map));
    if (map == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
        return NULL;
    }
    
    map->size = 0;
    map->max = size;
    map->entries = allocate(table->jvmti, size * sizeof(r_fieldRecord));
    bzero(map->entries, size * sizeof(r_fieldRecord));
    if (map->entries == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table entries\n");
    }
    
    return map;
}

static struct f_map *
map_request(FieldTable table)
{
    struct f_map *map;
    
    LOCK(table->jvmti, table->useLock)
    
    if (table->map == NULL) {
        map = NULL;
    }
    
    else {
        ++(table->map->using);
        map = table->map;
    }
    
    RELEASE(table->jvmti, table->useLock)
    
    return map;
}

static void
map_release(FieldTable table, struct f_map *map)
{
    LOCK(table->jvmti, table->useLock)
    
    if (map == NULL);
    else {
        if (map->free == JNI_TRUE && map->using == 1) {
            bzero(map->entries, map->max * sizeof(r_fieldRecord));
            deallocate(table->jvmti, map->entries);
            bzero(map, sizeof(struct f_map));
            deallocate(table->jvmti, map);
        }
        else {
            --(map->using);
        }
    }
    RELEASE(table->jvmti, table->useLock);
}

static r_fieldRecord *
find(struct f_map *map, jlong class_tag, jfieldID field)
{
    r_fieldRecord *result;
    size_t key, max, pos, i;
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
    
	pos = 0;
	while (JNI_TRUE) {
        /* quad probing, guaranteed not to repeat before $max hops */
		i = ((2*key + pos + pos*pos) / 2) % max;
		result = &(map->entries[i]);
		if (result->field == NULL) {
            break; /* empty */
        }
		if (result->class_tag == class_tag && result->field == field) {
            break; /* found */
        }
        
		pos++;
        
        if (pos >= max) {
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
    
    map = map_request(table);
    
    if (map != NULL) {
        memcpy(target, find(map, class_tag, field), sizeof(r_fieldRecord));
    }
    else {
        bzero(target, sizeof(r_fieldRecord));
    }
    
    map_release(table, map);
}

void
fields_visit(FieldTable table,  void (*callback) (r_fieldRecord*))
{
    size_t i, max;
    struct f_map *map;
    r_fieldRecord *tmp;
    
    map = map_request(table);
    
    if (map != NULL) {
        max = map->max;
        for (i = 0; i < max; i++) {
            tmp = &(map->entries[i]);
            if (tmp->field != NULL) {
                (callback)(tmp);
            }
        }
    }
    
    map_release(table, map);
}

void
fields_store(FieldTable table, r_fieldRecord *toStore, size_t len)
{
    size_t size, max, i, old;
    struct f_map *old_map = NULL, *new_map = NULL;
    r_fieldRecord *in, *out;
    
    LOCK(table->jvmti, table->changeLock);
    
    old_map = map_request(table);
    
    if (old_map != NULL) {
        size = old_map->size + len;
        old = max = old_map->max;
    }
    else {
        size = len;
        max = 1;
        old = 0;
    }
    
    if (size > LOAD_FACTOR * max) {
        while (size > LOAD_FACTOR * max) max *= 2;
        
        new_map = map_create(table, max);
        
        for (i = 0; i < old; i++) {
            in = &(old_map->entries[i]);
            if (in->field != NULL) {
                out = find(new_map, in->class_tag, in->field);
                memcpy(out, in, sizeof(r_fieldRecord));
            }
        }
        
        new_map->size = size - len;
    }
    else {
        new_map = old_map;
    }
    
    for (i = 0; i < len; i++) {
        in = &(toStore[i]);
        if (in->field != NULL) {
            out = find(new_map, in->class_tag, in->field);
            memcpy(out, in, sizeof(r_fieldRecord));
        }
    }
    
    if (new_map != NULL) {
        new_map->size = size;
    }
    
    LOCK(table->jvmti, table->useLock)
    table->map = new_map;
    if (old_map != NULL && old_map != new_map) {
        old_map->free = JNI_TRUE;
    }
    map_release(table, old_map);
    RELEASE(table->jvmti, table->useLock)
    
    RELEASE(table->jvmti, table->changeLock);
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
