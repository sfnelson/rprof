LIBNAME=rprof
SOURCES=src/rprof.c src/agent_util.c src/java_crw_demo.c src/comm.c

JAVA_SOURCES=src/nz/ac/vuw/ecs/rprof/*.java src/Test.java
JARFILE=bin/rprof.jar

OBJECTS=objects/rprof.o objects/agent_util.o objects/java_crw_demo.o objects/comm.o

# Required flags
COMMON_FLAGS=-fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS+= -W -Wall  -Wno-unused -Wno-parentheses

OS=$(shell uname)

ifeq ($(OS),Darwin)
	JDK=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
	CC=cc
	CFLAGS=-02 -g $(COMMON_FLAGS) -arch x86_64 -arch i386 -I$(JDK)/Headers
	LIBRARY=bin/lib$(LIBNAME).jnilib
	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text
	LD_LIBRARY_PATH=
endif

ifeq ($(OS),NetBSD)
	JDK=/usr/pkg/java/jdk-1.6.0
	CC=gcc
	CFLAGS=-g $(COMMON_FLAGS) -I/usr/pkg/include -I$(JDK)/include -I$(JDK)/include/netbsd
	LIBRARY=bin/lib$(LIBNAME).so
	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text -L/usr/pkg/lib
	LD_LIBRARY_PATH=.:/usr/pkg/lib
endif

LIBRARIES=-lc -lcurl
LINK_SHARED=$(LINK.c) -shared -o $@

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(OBJECTS)
	mkdir -p bin
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

objects/%.o: src/%.c
	mkdir -p objects
	$(CC) $(CFLAGS) -c $? -o $@

# Build jar file
$(JARFILE): $(JAVA_SOURCES)
	mkdir -p bin
	mkdir -p classes
	$(JDK)/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/bin/jar cf ../$@ *)

clean:
	rm -f -r bin classes objects tmp/pre/* tmp/post/*

test: all
	(cd bin; LD_LIBRARY_PATH=$(LD_LIBRARY_PATH) $(JDK)/bin/java -Djava.compiler=NONE -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME) -classpath ../classes Test)

server: all
	(cd classes; $(JDK)/bin/java nz.ac.vuw.ecs.rprof.Logger)

