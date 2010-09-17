#!/bin/bash

cls=$(echo $1 | sed -e "s/^.*\///")
type=$2

if [ "x$type" == "x" ]; then
	type="asm"
fi

if [ "x$cls" == "x" ]; then
	echo "usage: compare.sh [some/path/to/]class [asm|javap]"
	exit 1
fi

echo "Comparing: $cls ($type)"

mkdir -p tmp/compare
cd tmp/compare

cp ../pre/$cls pre.class
cp ../post/$cls post.class

javap -verbose pre > pre.javap
javap -verbose post > post.javap

java -classpath /Applications/eclipse-3.5-cocoa/plugins/com.google.gwt.eclipse.sdkbundle.2.0.4_2.0.4.v201006301254/gwt-2.0.4/gwt-dev.jar com.google.gwt.dev.asm.util.TraceClassVisitor pre.class > pre.asm
java -classpath /Applications/eclipse-3.5-cocoa/plugins/com.google.gwt.eclipse.sdkbundle.2.0.4_2.0.4.v201006301254/gwt-2.0.4/gwt-dev.jar com.google.gwt.dev.asm.util.TraceClassVisitor post.class > post.asm

if [ "x$type" == "xjavap" ]; then
	diff pre.javap post.javap
else
	diff pre.asm post.asm
fi
