# Makefile for rprof JVMTI agent
#
#       OPT=true        : compile with optimizations/no debug
#       J_DEBUG=true    : connect to java debugger at localhost:8000
#       GDB=true        : run inside gdb
#

OS=$(shell uname)

LIBNAME=agent
COMMON_SOURCES = rprof_fields.c rprof_classes.c
BUILD_SOURCES  = $(COMMON_SOURCES) rprof.c rprof_comm.c agent_util.c
TEST_SOURCES   = $(COMMON_SOURCES) tests.c test_utils.c

BUILD_OBJECTS  = $(BUILD_SOURCES:%.c=%.o)
TEST_OBJECTS   = $(TEST_SOURCES:%.c=%.o)

AGENT_HOME     = $(shell pwd)
TEST_CLASSES   = ../jvmti-lib/target/test-classes

# Required flags
COMMON_FLAGS = -fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS += -W -Wall -Wno-unused-parameter -Wno-parentheses
# To prevent include of procfs.h (what does this do?)
COMMON_FLAGS += -DLINUX
# To make sure code is reentrant
COMMON_FLAGS += -D_REENTRANT

ifdef OPT
    CFLAGS = -Os $(COMMON_FLAGS)
else
    CFLAGS = -g -O0 $(COMMON_FLAGS)
endif

ifdef VALGRIND_OPTS
else
    VALGRIND_OPTS=--suppressions=jdk.supp --num-callers=20
endif

ifdef DEBUG
    CFLAGS += -DDEBUG
endif

ifdef TEST
    CFLAGS += -D_RPROF_TEST
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
	LPATH	= .
	LPATHN  = DYLD_LIBRARY_PATH
	LDFLAGS = -dynamiclib -mimpure-text
	DACAPO  = dacapo-20110822.jar
endif

ifeq ($(OS),Linux)
	JDK1	= ../../jdk1.8.0
#	JDK1    = /usr/lib/jvm/java-7-openjdk-amd64
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
    -agentlib:$(LIBNAME)=$(HOST),$(AGENT_HOME),$@ \
    -Xmx1024m -Xint

ifdef DEBUG
    J_OPTS += -Xcheck:jni
endif

ifdef J_HOST
else
	J_HOST = [::1]:8000
endif

ifdef J_DEBUG
    J_OPTS  += -agentlib:jdwp=transport=dt_socket,address=$(J_HOST)
endif

ifdef GDB
    JAVA    = $(LPATHN)=$(LPATH) gdb --args $(JDK)/bin/java $(J_OPTS)
else
    JAVA    = $(LPATHN)=$(LPATH) $(JDK)/bin/java $(J_OPTS)
endif

ifdef VALGRIND
    JAVA    = $(LPATHN)=$(LPATH) valgrind $(VALGRIND_OPTS) $(JDK)/bin/java $(J_OPTS)
endif

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(BUILD_OBJECTS)
	$(LINK_SHARED) $(BUILD_OBJECTS) $(LIBRARIES)

tests: $(TEST_OBJECTS)
	$(CC) -o tests $(TEST_OBJECTS) $(LIBRARIES)

%.o: %.c
	$(CC) $(CFLAGS) -c $? -o $@

clean:
	rm -f -r $(LIBRARY) tests $(TEST_OBJECTS) $(BUILD_OBJECTS) tmp scratch
	mkdir -p tmp/pre
	mkdir -p tmp/post

version: all
	$(JAVA) -version

test: all
	$(JAVA) -classpath $(TEST_CLASSES) nz.ac.vuw.ecs.rprof.test.RProfTest

colltest: all
	$(JAVA) -classpath $(TEST_CLASSES) nz.ac.vuw.ecs.rprof.test.CollectionTest

example: all
	$(JAVA) -classpath $(TEST_CLASSES) nz.ac.vuw.ecs.rprof.test.RProfExample

hashmap: all
	$(JAVA) -classpath $(TEST_CLASSES) nz.ac.vuw.ecs.rprof.test.HashMapError

threads: all
	$(JAVA) -classpath $(TEST_CLASSES) nz.ac.vuw.ecs.rprof.test.ThreadsTest

list: all
	$(JAVA) -jar $(DACAPO) -l

avrora: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 avrora

batik: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 batik

eclipse: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 eclipse

fop: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 fop

h2: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 h2

jython: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 jython

luindex: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 luindex

lusearch: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 lusearch

pmd: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 pmd

tomcat: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 tomcat

tradebeans: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 tradebeans

tradesoap: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 tradesoap

xalan: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 xalan

