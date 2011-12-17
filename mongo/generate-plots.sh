#!/bin/bash

#mongo --quiet alex-proj.pvt:27017 plot-fine.js > fieldsFine.dat
#mongo --quiet alex-proj.pvt:27017 plot-coarse.js > fieldsCoarse.dat
#mongo --quiet alex-proj.pvt:27017 plot-constructor.js > constructorReturn.dat

function print_table {
	cat plot.js | sed -e "s/TABLE_NAME/$1/" | sed -e "s/RESULT_SET/$2/" | mongo --quiet alex-proj.pvt:27017 | grep -v "^bye$" > $3
}

for TYPE in resultsClasses resultsObjects; do
	for SET in fieldsFine fieldsCoarse constructorReturn firstEquals composite; do
		print_table $TYPE $SET "$TYPE-$SET.dat"
	done
done
