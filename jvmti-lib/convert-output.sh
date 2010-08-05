#!/bin/bash

cd tmp/post
for f in `ls`
do
	dir=$(echo $f | sed -e "s/\./\//g" | sed -e "s/\/[^\/]*\/class$//")
	name=$(echo $f | sed -e "s/^.*\.\([^\.]*\.class\)/\1/")
	echo "$dir/$name"
	mkdir -p $dir && cp $f "$dir/$name"
done
