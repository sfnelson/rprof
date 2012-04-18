OS=$(shell uname)

LIBNAME=rprof
SOURCES=src/rprof.c src/agent_util.c src/rprof_util.c src/rprof_comm.c src/stringlist.c
TEST_SOURCES=src/test_utils.c

JAVA_SOURCES=src/nz/ac/vuw/ecs/rprof/*.java src/Test.java src/Example.java src/HashMapError.java
JARFILE=bin/rprof.jar

DACAPO=dacapo-20110822.jar

OBJECTS=$(SOURCES:src/%.c=objects/%.o)
TEST-OBJECTS=$(OBJECTS) $(TEST_SOURCES:src/%.c=objects/%.o)

# Required flags
COMMON_FLAGS = -fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS += -W -Wall -Wno-unused-parameter -Wno-parentheses
# To prevent include of procfs.h (what does this do?)
COMMON_FLAGS += -DLINUX
# To make sure code is reentrant
COMMON_FLAGS += -D_REENTRANT

ifeq ($(OPT), true)
    CFLAGS=-O2 $(COMMON_FLAGS) -DNDEBUG
else
    CFLAGS=-g $(COMMON_FLAGS) -DDEBUG
endif

# Library name and options needed to build it
LIBRARY=lib$(LIBNAME).so
LDFLAGS=-Wl,-soname=$(LIBRARY) -static-libgcc -mimpure-text
# Libraries we are dependent on
LIBRARIES= -ldl -lc
# Building a shared library
LINK_SHARED=$(LINK.c) -shared -o $@

HOST=[::1]:8888

ifeq ($(OS),Darwin)
	JDK     = /Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home
	CC      = clang
	CFLAGS += -analyze -arch x86_64 -I$(JDK)/include -I$(JDK)/include/darwin
	LIBRARY = bin/lib$(LIBNAME).dylib
	LPATH   = ./bin/
	LPATHN  = DYLD_LIBRARY_PATH
	LDFLAGS = -dynamiclib -mimpure-text
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
	LIBRARY = bin/lib$(LIBNAME).so
	LPATH   = bin/:/usr/pkg/lib:/vol/projects/stephen/curl/lib
	LPATHN  = LD_LIBRARY_PATH
	LDFLAGS+= -dynamiclib -static-libgcc -mimpure-text -L/usr/pkg/lib -L/vol/projects/stephen/curl/lib
endif

LIBRARIES=-lcurl
LINK_SHARED=$(LINK.c) -shared -o $@

J_OPTS = \
    -Xcheck:jni \
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
	mkdir -p bin
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

objects/%.o: src/%.c
	mkdir -p objects
	$(CC) $(CFLAGS) -c $? -o $@

$(JARFILE): $(JAVA_SOURCES)
	mkdir -p bin
	mkdir -p classes
	$(JDK)/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/bin/jar cf ../$@ *)

clean:
	rm -f -r bin classes objects tmp/pre/* tmp/post/*

test: all
	$(JAVA) -version

testrun: all
	$(JAVA) -classpath classes Test

example: all
	$(JAVA) -classpath classes Example

testhash: all
	$(JAVA) -classpath classes HashMapError

list: all
	$(JAVA) -jar $(DACAPO) -l

batik: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 fop

fop: all
	$(JAVA) -jar $(DACAPO) -n 1 -t 1 fop
