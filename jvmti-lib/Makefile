LIBNAME=rprof
SOURCES=src/rprof.c src/agent_util.c src/java_crw_demo.c src/comm.c

JAVA_SOURCES=src/nz/ac/vuw/ecs/rprof/*.java src/Test.java src/Example.java
JARFILE=bin/rprof.jar

OBJECTS=objects/rprof.o objects/agent_util.o objects/java_crw_demo.o objects/comm.o

# Required flags
COMMON_FLAGS=-fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS+= -W -Wall  -Wno-unused -Wno-parentheses

OS=$(shell uname)
LIBRARY_PATH_NAME=LD_LIBRARY_PATH

ifeq ($(OS),Darwin)
	#JDK=/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home
	#JDK=/Library/Java/JavaVirtualMachines/openjdk-1.7-x86_64/Contents/Home
	JDK=/Library/Java/JavaVirtualMachines/JDK\ 1.7.0\ Developer\ Preview.jdk/Contents/Home
	HEADERS=
	CC=cc
	CFLAGS=-02 -g $(COMMON_FLAGS) -arch x86_64 -I$(JDK)/include -I$(JDK)/include/darwin
	DEBUG=-D DEBUG
	LIBRARY=bin/lib$(LIBNAME).dylib
	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text
	LIBRARY_PATH=$(shell pwd)/bin/
	LIBRARY_PATH_NAME=DYLD_LIBRARY_PATH
	HOST=127.0.0.1:8888
endif

#ifeq ($(OS),Darwin)
#	JDK=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
#	CC=cc
#	CFLAGS=-02 -g $(COMMON_FLAGS) -arch x86_64 -arch i386 -I/Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Headers/ -D DEBUG
#	LIBRARY=bin/lib$(LIBNAME).jnilib
#	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text
#	LIBRARY_PATH=
#	HOST=127.0.0.1:8888
#endif

ifeq ($(OS),NetBSD)
	JDK=/usr/pkg/java/jdk-1.6.0
	CC=gcc
	CFLAGS=-g $(COMMON_FLAGS) -I/usr/pkg/include -I$(JDK)/include -I$(JDK)/include/netbsd
	LIBRARY=bin/lib$(LIBNAME).so
	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text -L/usr/pkg/lib
	LIBRARY_PATH=.:/usr/pkg/lib
	HOST=[::1]:8888
endif

ifeq ($(OS),Linux)
	JDK=/usr/pkg/java/sun-6
	ifeq ($(wildcard /usr/pkg/java/sun-6),)
		JDK=/usr/lib/jvm/java-6-openjdk
	endif
	CC=gcc
	CFLAGS=-g $(COMMON_FLAGS) -I/usr/pkg/include -I$(JDK)/include -I$(JDK)/include/linux
	LIBRARY=bin/lib$(LIBNAME).so
	LIBRARY_PATH=$(shell pwd)/bin/:/usr/pkg/lib
	LIBRARY_PATH_NAME=LD_LIBRARY_PATH
	LDFLAGS=-dynamiclib -static-libgcc -mimpure-text -L/usr/pkg/lib
	HOST=[::1]:8888
endif

LIBRARIES=-lc -lcurl
LINK_SHARED=$(LINK.c) -shared -o $@

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(OBJECTS)
	mkdir -p bin
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

objects/%.o: src/%.c
	mkdir -p objects
	$(CC) $(CFLAGS) $(DEBUG) -c $? -o $@

# Build jar file
$(JARFILE): $(JAVA_SOURCES)
	mkdir -p bin
	mkdir -p classes
	$(JDK)/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/bin/jar cf ../$@ *)

clean:
	rm -f -r bin classes objects tmp/pre/* tmp/post/*

test: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),test -classpath ../classes Test)

example: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),example -classpath ../classes Example)

debug: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),test -agentlib:jdwp=transport=dt_socket,address=localhost:8000 -classpath ../classes Test)

server: all
	(cd classes; $(JDK)/bin/java nz.ac.vuw.ecs.rprof.Logger)

avrora: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),avrora -classpath ../classes -jar ../dacapo-20110822.jar -n 1 -t 1 avrora)

pmd: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),pmd -classpath ../classes -jar ../dacapo-20110822.jar -n 1 -t 1 pmd)

fop: all
	(cd bin; $(LIBRARY_PATH_NAME)=$(LIBRARY_PATH) $(JDK)/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME)=$(HOST),fop -classpath ../classes -jar ../dacapo-20110822.jar -n 1 -t 1 fop)
