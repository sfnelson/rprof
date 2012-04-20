#include "rprof_util.h"
#include "agent_util.h"

/* TODO this is not thread-safe :-( */

#define LOAD_FACTOR 0.7f

typedef struct _r_fieldTable {
    size_t size;
    size_t max;
    r_fieldRecord *entries;
} field_map;

typedef struct {
    jfieldID field;
    jint fid;
} FieldEntry;

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


/*
static ClassEntry *
_find_cls(ClassTable *table, jint cnum)
{
    size_t index = (size_t) cnum;

    if (index >= table->size) {
        fatal_error("request for cnum greater than table size!");
    }
    
    return &(table->classes[cnum]);
}

static FieldEntry *
_find_field(ClassEntry *cls, jfieldID field)
{
    size_t i;
    
    for (i = 0; i < cls->num_fields; i++) {
        if (cls->fields[i].field == field) {
            return &(cls->fields[i]);
        }
    }
    
    return NULL;
}*/

volatile size_t numProbes;
volatile size_t numSearches;

static r_fieldRecord *
find(r_fieldTable table, jlong class_tag, jfieldID field)
{
    r_fieldRecord *result;
    size_t key, max, index, i;
	size_t fieldAsInt = (size_t) field;
    size_t classAsInt = (size_t) class_tag;

    max = table->max;
    
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
		result = &(table->entries[i]);
		if (result->field == NULL) break; /* empty */
		if (result->class_tag == class_tag && result->field == field) break; /* found */
		index++;
        if (index >= max) {
            fatal_error("ERROR: probed all hash table entries without finding entry\n");            
            return NULL;
        }
	}

	numProbes += index;
    numSearches += 1;
    
    return result;
}

void find_field(r_fieldTable table, jlong class_tag, jfieldID field, r_fieldRecord* target)
{
    memcpy(target, find(table, class_tag, field), sizeof(r_fieldRecord));
}

void store_fields(r_fieldTable *table, r_fieldRecord *toStore, size_t len)
{
    size_t size, max, i, old;
    r_fieldTable old_table, new_table;
    r_fieldRecord *in, *out;

    old_table = *table;

    if (old_table != NULL) {
        size = old_table->size + len;
        old = max = old_table->max;
    }
    else {
        size = len;
        max = 1;
        old = 0;
    }

    while (size > LOAD_FACTOR * max) max *= 2;

    new_table = malloc(sizeof(field_map));
    if (new_table == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
        return;
    }

    new_table->size = size;
    new_table->max = max;
    new_table->entries = malloc(max * sizeof(r_fieldRecord));
    if (new_table->entries == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
    }

    bzero(new_table->entries, max * sizeof(r_fieldRecord));

    for (i = 0; i < old; i++) {
        in = &(old_table->entries[i]);
        if (in->field != NULL) {
            out = find(new_table, in->class_tag, in->field);
            memcpy(out, in, sizeof(r_fieldRecord));
        }
    }

    for (i = 0; i < len; i++) {
        in = &(toStore[i]);
        if (in->field != NULL) {
            out = find(new_table, in->class_tag, in->field);
            memcpy(out, in, sizeof(r_fieldRecord));
        }
    }

    (*table) = new_table;

    if (old_table != NULL) {
        free(old_table->entries);
        free(old_table);
    }
    
    numProbes = 0;
    numSearches = 0;
}

void visit_fields(r_fieldTable table, void (*callback) (r_fieldRecord*))
{
    size_t i, max;
    r_fieldRecord *tmp;

    max = table->max;
    for (i = 0; i < max; i++) {
        tmp = &(table->entries[i]);
        if (tmp->field != NULL) {
            (callback)(tmp);
        }
    }
}

/* should call visit_fields first and clean up the cls global refs */
void cleanup_fields(r_fieldTable *table)
{
    if (*table == NULL) return;
    free((*table)->entries);
    free((*table));
    (*table) = NULL;
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
