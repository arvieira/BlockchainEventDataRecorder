#!/bin/bash


cp ipanema.db ../application/
rm ../sumo/*.*
cp ipanema.con.xml ipanema.edg.xml ipanema.net.xml ipanema.nod.xml ipanema.rou.xml ipanema.sumocfg ../sumo/
