#!/bin/bash

CONVERT="./tools/scenario-convert-21.0.jar"

#Marrom:
#java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98649","-43.20438" --route-end-latlon "-22.9863","-43.20758"
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98649","-43.20438" --route-end-latlon "-22.9863","-43.20797"

#Vermelha:
#java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98374","-43.20705" --route-end-latlon "-22.9863","-43.20758"
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98374","-43.20705" --route-end-latlon "-22.9863","-43.20797"

#Laranja:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98374","-43.20705" --route-end-latlon "-22.98661","-43.20438"

#Azul Escuro:
#java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98661","-43.20438"
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98661","-43.20438"

#Roxo:
#java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98516","-43.20765"
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98516","-43.20765"


#Cinza:
#java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98424","-43.20443"
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98643","-43.20766" --route-end-latlon "-22.98424","-43.20443"


#Verde Escuro:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98374","-43.20705" --route-end-latlon "-22.98424","-43.20443"

#Azul Claro:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98410","-43.20766" --route-end-latlon "-22.98424","-43.20443"

#Amarelo:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98374","-43.20705" --route-end-latlon "-22.98383","-43.20495"

#Verde Claro:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98535","-43.20441" --route-end-latlon "-22.98383","-43.20495"


#Rosa:
java -jar $CONVERT -d ipanema.db -g --route-begin-latlon "-22.98535","-43.20441" --route-end-latlon "-22.98516","-43.20765"


mv ipanema.rou-9.xml ipanema.rou.xml
rm ipanema.rou-*
