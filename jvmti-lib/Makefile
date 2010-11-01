LIBNAME=rprof
SOURCES=src/rprof.c src/agent_util.c src/java_crw_demo.c src/comm.c

JAVA_SOURCES=src/nz/ac/vuw/ecs/rprof/*.java src/Test.java
JARFILE=bin/rprof.jar

OBJECTS=objects/rprof.o objects/agent_util.o objects/java_crw_demo.o objects/comm.o

JDK=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0

# Required flags
COMMON_FLAGS=-fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS+= -W -Wall  -Wno-unused -Wno-parentheses

CFLAGS=-O2 -g $(COMMON_FLAGS)
CFLAGS += -arch x86_64 -arch i386

CFLAGS += -I$(JDK)/Headers

LIBRARY=bin/lib$(LIBNAME).jnilib
LDFLAGS=-dynamiclib -static-libgcc -mimpure-text
LIBRARIES=-lc -lcurl
LINK_SHARED=$(LINK.c) -shared -o $@

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(OBJECTS)
	mkdir -p bin
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

objects/%.o: src/%.c
	mkdir -p objects
	cc $(CFLAGS) -c $? -o $@

# Build jar file
$(JARFILE): $(JAVA_SOURCES)
	mkdir -p bin
	mkdir -p classes
	$(JDK)/Home/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/Home/bin/jar cf ../$@ *)

clean:
	rm -f -r bin classes objects tmp/pre/* tmp/post/*

test: all
	(cd bin; $(JDK)/Home/bin/java -Djava.compiler=NONE -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME) -classpath ../classes Test)

test2: all
	(cd bin; $(JDK)/Home/bin/java -Djava.compiler=NONE -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME) -classpath ~/Desktop/jhotdraw/#prof/ CH/ifa/draw/samples/javadraw/JavaDrawApp)

server: all
	(cd classes; $(JDK)/Home/bin/java nz.ac.vuw.ecs.rprof.Logger)

