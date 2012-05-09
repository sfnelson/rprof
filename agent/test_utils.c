#include "agent_util.h"

#ifdef _RPROF_TEST

void
stdout_message(const char * format, ...)
{
    va_list ap;
    
    va_start(ap, format);
    (void)vfprintf(stdout, format, ap);
    va_end(ap);
}

void
fatal_error(const char * format, ...) __attribute__ ((__noreturn__));

void
fatal_error(const char * format, ...)
{
    va_list ap;
    
    va_start(ap, format);
    (void)vfprintf(stderr, format, ap);
    (void)fflush(stderr);
    va_end(ap);
    exit(3);
}

void
deallocate(jvmtiEnv *jvmti, void *ptr)
{
    free(ptr);
}

/* Allocation of JVMTI managed memory */
void *
allocate(jvmtiEnv *jvmti, size_t len)
{
    void * ptr = malloc(len);
    memset(ptr, 0xCB, len);
    return ptr;
}

#endif
