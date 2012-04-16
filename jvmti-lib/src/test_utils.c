#import <stdio.h>
#import <stdlib.h>
#import <stdint.h>
#import <string.h>
#import <jni.h>

#import "rprof_util.h"

#define NUM_FIELDS 50

uint64_t visited = 0;
uint64_t check[NUM_FIELDS];
uint64_t count = 0;
uint64_t passed = 0;
uint64_t failed = 0;

void assert(uint32_t id, uint64_t expected, uint64_t actual) {
    count++;
    if (expected == actual) {
        passed++;
    }
    else {
        failed++;
        printf("  (%llu:%u) expected: %llu but got: %llu\n", count, id, expected, actual);
    }
}

void visitor(r_fieldRecord* target) {
    uint32_t val = (uint64_t) (target->cls);
    check[val-1] = val;
    assert(10, val % 4 + 1, (uint64_t) (target->field));
    assert(20, val, (uint64_t) (target->id.raw));
    visited++;
}

size_t class_count = 0;

void class_visitor(jvmtiEnv* jvmti, JNIEnv* env, const char* cname) {
    assert(30, (uint64_t) class_count, (uint64_t) (cname[0] - 'a'));
    class_count++;
}

int main() {
    r_fieldTable table = NULL;
    uint32_t i;
    uintptr_t ptr;
    r_fieldRecord tmp;
    r_fieldRecord records[NUM_FIELDS];

    memset(&records, 0, sizeof(records));

    for (i = 0; i < NUM_FIELDS; i++) {
        ptr = (uintptr_t) (i+1);
        records[i].class_tag = (jlong) ptr;
        records[i].field = (jfieldID) (ptr % 4 + 1);
        records[i].cls = (jclass) ptr;
        records[i].id.raw = i + 1;
        check[i] = 0;
    }

    store_fields(&table, records, 5);

    visited = 0;
    visit_fields(table, &visitor);
    assert(40, 5, visited);

    for (i = 0; i < 5; i++) {
        assert(50, i+1, check[i]);
        check[i] = 0;
    }

    find_field(table, (jlong) 4, (jfieldID) 1, &tmp);
    assert(60, 1, (uintptr_t) tmp.field);
    assert(70, 4, (uintptr_t) tmp.cls);
    assert(80, 4, tmp.id.raw);

    store_fields(&table, &(records[4]), NUM_FIELDS/10);
    store_fields(&table, records, NUM_FIELDS/2);
    store_fields(&table, records, NUM_FIELDS);

    visited = 0;
    visit_fields(table, &visitor);
    assert(90, 50, visited);

    for (i = 0; i < NUM_FIELDS; i++) {
        assert(100, i+1, check[i]);
    }

    printf("Test: %llu/%llu assertions passed\n", passed, count);
    passed = 0;
    count = 0;

    // == test class list ==

    r_classList classes = NULL;
    const char * input[] = { "a", "b", "c", "d", "e", "f", "g", "h",
                        "i", "j", "k", "l", "m", "n", "o", "p",
                        "q", "r", "s", "t", "u", "v", "w", "x" };
    char * class_names[24];
    char * name;

    for (i = 0; i < 24; i++) {
        name = malloc(2);
        strcpy(name, input[i]);
        store_class(&classes, name);
    }

    visit_classes(classes, NULL, NULL, &class_visitor);

    cleanup_classes(&classes);

    assert(110, 0, (uintptr_t) classes);

    printf("Test: %llu/%llu assertions passed\n", passed, count);
    passed = 0;
    count = 0;

    return 0;
}