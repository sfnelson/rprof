#include "rprof_util.h"
#include "agent_util.h"

// TODO this is not thread-safe :-(

typedef struct _r_fieldTable {
    uint32_t size;
    uint32_t max;
    r_fieldRecord *entries;
} field_map;

r_fieldRecord* find(r_fieldTable table, jlong class_tag, jfieldID field)
{
	uint32_t key, max, index, i;
	uintptr_t fieldAsInt = (intptr_t) field;
	r_fieldRecord *result;

    max = table->max;
	key = (uint32_t) ((fieldAsInt ^ class_tag) % max);
	index = 0;
	while (index < max) {
		i = ((uint32_t)(key + index/2.0f + index*index/2.0f)) % max;
		result = &(table->entries[i]);
		if (result->field == NULL) return result; // empty
		if (result->class_tag == class_tag && result->field == field) return result; // found
		index++;
	}
	fatal_error("ERROR: probed all hash table entries without finding entry\n");
	return NULL;
}

void find_field(r_fieldTable table, jlong class_tag, jfieldID field, r_fieldRecord* target)
{
    memcpy(target, find(table, class_tag, field), sizeof(r_fieldRecord));
}

void store_fields(r_fieldTable *table, r_fieldRecord *toStore, uint32_t len)
{
    r_fieldTable old_table, new_table;
    uint32_t size, max, i, old;
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

    while (size > 0.7f * max) max *= 2;

    new_table = malloc(sizeof(field_map));
    if (new_table == NULL) {
        fatal_error("ERROR: unable to allocate space for a field table\n");
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
}

void visit_fields(r_fieldTable table, void (*callback) (r_fieldRecord*))
{
    uint32_t i, max;
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


// ==== Class List Functions

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