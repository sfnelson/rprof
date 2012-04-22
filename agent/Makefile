# Makefile for rprof JVMTI agent
#
#       OPT=true        : compile with optimizations/no debug
#       J_DEBUG=true    : connect to java debugger at localhost:8000
#       GDB=true        : run inside gdb
#

OS=$(shell uname)

LIBNAME=agent
SOURCES=rprof.c agent_util.c rprof_util.c rprof_comm.c stringlist.c
TEST_SOURCES=test_utils.c

JARFILE=../jvmti-lib/target/jvmti-lib-1.0-SNAPSHOT.jar


OBJECTS=$(SOURCES:%.c=%.o)
TEST_OBJECTS=$(OBJECTS) $(TEST_SOURCES:%.c=%.o)

# Required flags
COMMON_FLAGS = -fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS += -W -Wall -Wno-unused-parameter -Wno-parentheses
# To prevent include of procfs.h (what does this do?)
COMMON_FLAGS += -DLINUX
# To make sure code is reentrant
COMMON_FLAGS += -D_REENTRANT

ifdef OPT
    CFLAGS=-Os $(COMMON_FLAGS) -DNDEBUG
else
    CFLAGS=-g $(COMMON_FLAGS) -DDEBUG
endif

# Library name and options needed to build it
LIBRARY=lib$(LIBNAME).so
LDFLAGS=-Wl,-soname=$(LIBRARY) -static-libgcc -mimpure-text
# Libraries we are dependent on
LIBRARIES= -lcurl
# Building a shared library
LINK_SHARED=$(LINK.c) -shared -o $@

ifdef HOST
else
	HOST=[::1]:8888
endif

ifeq ($(OS),Darwin)
	JDK     = /Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home
	CC      = clang
	CFLAGS += -arch x86_64 -I$(JDK)/include -I$(JDK)/include/darwin
	LIBRARY = lib$(LIBNAME).dylib
	#LPATH  = /Users/stephen/Library/Developer/Xcode/DerivedData/Agent-futybmjwxpndsxcvqeoojdbzvigz/Build/Products/Debug/
	LPATH	= .
	LPATHN  = DYLD_LIBRARY_PATH
	LDFLAGS = -dynamiclib -mimpure-text
	DACAPO  = dacapo-20110822.jar
endif

ifeq ($(OS),Linux)
	JDK1    = /usr/lib/jvm/java-7-openjdk-amd64
	JDK2    = /vol/projects/stephen/oracle-jdk7-x86_64
	ifeq ($(wildcard $(JDK1)),$(JDK1))
		JDK = $(JDK1)
	else
		JDK = $(JDK2)
	endif
	CC      = gcc
	CFLAGS += -I/usr/pkg/include -I$(JDK)/include -I$(JDK)/include/linux -I/vol/projects/stephen/curl/include
	LIBRARY = lib$(LIBNAME).so
	LPATH   = .:/usr/pkg/lib:/vol/projects/stephen/curl/lib
	LPATHN  = LD_LIBRARY_PATH
	LDFLAGS+= -dynamiclib -static-libgcc -mimpure-text -L/usr/pkg/lib -L/vol/projects/stephen/curl/lib
	DACAPO  = /vol/projects/stephen/dacapo/dacapo-9.12-bach.jar
endif

LINK_SHARED=$(LINK.c) -shared -o $@

J_OPTS = \
    -Xbootclasspath/a:$(JARFILE) \
    -agentlib:$(LIBNAME)=$(HOST),$@

ifeq ($(J_DEBUG),true)
    J_OPTS  += -agentlib:jdwp=transport=dt_socket,address=localhost:8000
endif

ifeq ($(GDB),true)
    JAVA    = $(LPATHN)=$(LPATH) gdb --args $(JDK)/bin/java $(J_OPTS)
else
    JAVA    = $(LPATHN)=$(LPATH) $(JDK)/bin/java $(J_OPTS)
endif

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(OBJECTS)
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

tests: test-utils

test-utils: $(TEST_OBJECTS)
	$(CC) -o test-utils $(TEST_OBJECTS) $(LIBRARIES)

%.o: %.c
	$(CC) $(CFLAGS) -c $? -o $@

clean:
	rm -f -r $(LIBRARY) $(TEST_OBJECTS) test-utils tmp/pre/* tmp/post/*

test: all
	$(JAVA) -version

kitchensink: all
	$(JAVA) -classpath classes Test

example: all
	$(JAVA) -classpath classes Example

hashmap: all
	$(JAVA) -classpath classes HashMapError

list: all
	$(JAVA) -jar $(DACAPO) -l

batik: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 batik

eclipse: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 eclipse

fop: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 fop

luindex: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 luindex

lusearch: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 lusearch

pmd: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 pmd

