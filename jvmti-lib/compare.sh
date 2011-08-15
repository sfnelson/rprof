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

ASM_CLASSPATH="$HOME/.m2/repository/asm/asm/3.3.1/asm-3.3.1.jar:$HOME/.m2/repository/asm/asm-util/3.3.1/asm-util-3.3.1.jar"

java -classpath $ASM_CLASSPATH org.objectweb.asm.util.TraceClassVisitor pre.class > pre.trace
java -classpath $ASM_CLASSPATH org.objectweb.asm.util.TraceClassVisitor post.class > post.trace

java -classpath $ASM_CLASSPATH org.objectweb.asm.util.ASMifierClassVisitor pre.class > pre.asm
java -classpath $ASM_CLASSPATH org.objectweb.asm.util.ASMifierClassVisitor post.class > post.asm

if [ "x$type" == "xjavap" ]; then
	diff pre.javap post.javap
else
	diff pre.trace post.trace
fi
