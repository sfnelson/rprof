#!/bin/bash
mongo --quiet alex-proj.pvt:27017 plot-fine.js > fieldsFine.dat
mongo --quiet alex-proj.pvt:27017 plot-coarse.js > fieldsCoarse.dat
mongo --quiet alex-proj.pvt:27017 plot-constructor.js > constructorReturn.dat
