#!/bin/bash

CONVERT="./tools/scenario-convert-21.0.jar"

java -jar $CONVERT --osm2sumo -i ipanema.osm
#java -jar $CONVERT --db2sumo -d ipanema.db --export-traffic-lights -n
#java -jar $CONVERT --db2sumo -d ipanema.db -n
