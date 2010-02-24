LIBNAME=rprof
SOURCES=rprof.c agent_util.c java_crw_demo.c

JAVA_SOURCES=HeapTracker.java Test.java
JARFILE=rprof.jar

OBJECTS=rprof.o agent_util.o java_crw_demo.o

JDK=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0

# Required flags
COMMON_FLAGS=-fno-strict-aliasing -fPIC -fno-omit-frame-pointer
# Error checking flags
COMMON_FLAGS+= -W -Wall  -Wno-unused -Wno-parentheses

CFLAGS=-O2 $(COMMON_FLAGS)
CFLAGS += -arch x86_64 -arch i386

CFLAGS += -I.
CFLAGS += -I$(JDK)/Headers

LIBRARY=lib$(LIBNAME).jnilib
LDFLAGS=-dynamiclib -static-libgcc -mimpure-text
#LIBRARIES=-L $(JDK)/Libraries/ -lc
LIBRARIES=-lc
LINK_SHARED=$(LINK.c) -shared -o $@

all: $(LIBRARY) $(JARFILE)

$(LIBRARY): $(OBJECTS)
	$(LINK_SHARED) $(OBJECTS) $(LIBRARIES)

%.o: %.c
	cc $(CFLAGS) -c $?

# Build jar file
$(JARFILE): $(JAVA_SOURCES)
	rm -f -r classes
	mkdir -p classes
	$(JDK)/Home/bin/javac -d classes $(JAVA_SOURCES)
	(cd classes; $(JDK)/Home/bin/jar cf ../$@ *)

clean:
	rm -f $(LIBRARY) $(OBJECTS) $(JARFILE)
	rm -f -r classes

test:
	$(JDK)/Home/bin/java -Xbootclasspath/a:rprof.jar -agentlib:$(LIBNAME) -cp classes Test
