/*
 * @(#)agent_util.c	1.15 05/11/17
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

#include "agent_util.h"

/* ------------------------------------------------------------------- */
/* Generic C utility functions */

/* Send message to stdout or whatever the data output location is */
void
stdout_message(const char * format, ...)
{
    va_list ap;
    
    va_start(ap, format);
    (void)vfprintf(stdout, format, ap);
    va_end(ap);
}

/* Send message to stderr or whatever the error output location is and exit  */
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

/* ------------------------------------------------------------------- */
/* Generic JVMTI utility functions */

/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void
check_jvmti_error(jvmtiEnv* jvmti, jvmtiError errnum, const char* str)
{
    if ( errnum != JVMTI_ERROR_NONE ) {
        char       *errnum_str;
        
        errnum_str = NULL;
        (void)(*jvmti)->GetErrorName(jvmti, errnum, &errnum_str);
        
        fatal_error("ERROR: JVMTI: %d(%s): %s\n", errnum, 
                    (errnum_str==NULL?"Unknown":errnum_str),
                    (str==NULL?"":str));
    }
}

/* All memory allocated by JVMTI must be freed by the JVMTI Deallocate
 *   interface.
 */
void
deallocate(jvmtiEnv *jvmti, void *ptr)
{
    jvmtiError error;
    
#ifdef DEBUG
    size_t *p = (size_t *) ptr;
    char *start = (char *) ptr;
    
    size_t len = p[-2];
    char *end = &start[len];
    
    size_t start_guard = p[-1];
    size_t end_guard = ((size_t *)end)[0];
    
    if (start_guard != 0xdfdfdfdfdfdfdfdf) {
        fatal_error("start guard got stomped");
    }
    
    if (end_guard != 0xdfdfdfdfdfdfdfdf) {
        fatal_error("end guard got stomped");
    }
    
    ptr = &(p[-2]);
#endif
    
    error = (*jvmti)->Deallocate(jvmti, ptr);
    check_jvmti_error(jvmti, error, "Cannot deallocate memory");
}

/* Allocation of JVMTI managed memory */
void *
allocate(jvmtiEnv *jvmti, size_t len)
{
    jvmtiError error;
    void      *ptr = NULL;
    
#ifdef DEBUG
    size_t    *p = NULL;
    size_t     l = len;
    
    len += 3 * sizeof(size_t);
#endif
    
    error = (*jvmti)->Allocate(jvmti, (jlong) len, (unsigned char **)&ptr);
    check_jvmti_error(jvmti, error, "Cannot allocate memory");
    
#ifdef DEBUG
    memset(ptr, 0xdf, len);
    
    p = (size_t *) ptr;
    p[0] = l;
    
    ptr = &(p[2]);
#endif
    
    return ptr;
}

void
parse_agent_args(char *args, char **host, char **agent_home, char **benchmark)
{
    if (NULL == args || 0 == strlen(args));
    else if (NULL == strchr(args, ','));
    else {
        *host = args;
        *agent_home = &strchr(args, ',')[1];
        
        if (NULL != strchr(*agent_home, ',')) {
            *benchmark = &strchr(*agent_home, ',')[1];
            (*agent_home)[-1] = 0;
            (*benchmark)[-1] = 0;
            
            return;
        }
    }
    
    fatal_error("error parsing agent arguments: expected -agentlib:agent=host,path,program");
}

void
load_agent_jar(jvmtiEnv *jvmti, const char *agent_home)
{
    jvmtiError error;
    char       agent_path[FILENAME_MAX+1];
    
    sprintf(agent_path, AGENT_JAR_NAME_FORMAT_STR, agent_home);
    
    error = (*jvmti)->AddToBootstrapClassLoaderSearch(jvmti, agent_path);
    check_jvmti_error(jvmti, error, "Cannot add to boot classpath");
}

/* ------------------------------------------------------------------- */
