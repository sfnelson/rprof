#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <jni.h>

#include "rprof_fields.h"
#include "rprof_classes.h"

#define NUM_FIELDS 50

uint64_t visited = 0;
uint64_t check[NUM_FIELDS];
uint64_t count = 0;
uint64_t passed = 0;
uint64_t failed = 0;

static void
assert(uint64_t id, uint64_t expected, uint64_t actual)
{
    count++;
    if (expected == actual) {
        passed++;
    }
    else {
        failed++;
        printf("  (%llu:%llu) expected: %llu but got: %llu\n", count, id, expected, actual);
    }
}

static void
visitor(r_fieldRecord* target)
{
    uint64_t val = (uintptr_t) (target->cls);
    check[val-1] = val;
    assert(10, val % 4 + 1, (uint64_t) (target->field));
    assert(20, val, (uint64_t) (target->id.raw));
    visited++;
}

size_t class_count = 0;

static void
class_visitor(jvmtiEnv *jvmti, JNIEnv *jni, const char* cname)
{
    assert(30, (uint64_t) class_count, (uint64_t) (cname[0] - 'a'));
    class_count++;
}

static void testFieldTable();
static void testClassList();

int
main() {
    testFieldTable();
    testClassList();
}

static void testFieldTable()
{
    FieldTable table = NULL;
    jvmtiEnv *jvmti = NULL;
    uint32_t i;
    uintptr_t ptr;
    r_fieldRecord tmp;
    r_fieldRecord records[NUM_FIELDS];
    
    table = fields_create(jvmti, "field table");
    
    memset(&records, 0, sizeof(records));
    
    for (i = 0; i < NUM_FIELDS; i++) {
        ptr = (uintptr_t) (i+1);
        records[i].class_tag = (jlong) ptr;
        records[i].field = (jfieldID) (ptr % 4 + 1);
        records[i].cls = (jclass) ptr;
        records[i].id.raw = i + 1;
        check[i] = 0;
    }
    
    fields_store(table, records, 5);
    
    visited = 0;
    fields_visit(table, &visitor);
    assert(40, 5, visited);
    
    for (i = 0; i < 5; i++) {
        assert(50, i+1, check[i]);
        check[i] = 0;
    }
    
    fields_find(table, (jlong) 4, (jfieldID) 1, &tmp);
    assert(60, 1, (uintptr_t) tmp.field);
    assert(70, 4, (uintptr_t) tmp.cls);
    assert(80, 4, (uint64_t) tmp.id.raw);
    
    fields_store(table, &(records[4]), NUM_FIELDS/10);
    fields_store(table, records, NUM_FIELDS/2);
    fields_store(table, records, NUM_FIELDS);
    
    visited = 0;
    fields_visit(table, &visitor);
    assert(90, 50, visited);
    
    for (i = 0; i < NUM_FIELDS; i++) {
        assert(100, i+1, check[i]);
    }
    
    printf("Test: %llu/%llu assertions passed\n", passed, count);
    passed = 0;
    count = 0;
}

static void testClassList() {
    ClassList classes;
    size_t i;
    char * name;
    const char * input[] = { "a", "b", "c", "d", "e", "f", "g", "h",
        "i", "j", "k", "l", "m", "n", "o", "p",
        "q", "r", "s", "t", "u", "v", "w", "x" };

    classes = NULL;
    
    for (i = 0; i < 24; i++) {
        name = malloc(2);
        strcpy(name, input[i]);
        classes_add(classes, name);
    }
    
    classes_visit(classes, NULL, NULL, &class_visitor);
    
    classes_destroy(classes);
    
    printf("Test: %llu/%llu assertions passed\n", passed, count);
    passed = 0;
    count = 0;
}
