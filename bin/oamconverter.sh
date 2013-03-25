#!/bin/bash
#
#    Open Aviation Map
#    Copyright (C) 2012-2013 Ákos Maróy
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Affero General Public License as
#    published by the Free Software Foundation, either version 3 of the
#    License, or (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU Affero General Public License for more details.
#
#    You should have received a copy of the GNU Affero General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

#
#  Open Aviation Map execution wrapper script
#
#  It is assumed that this script resides in the oam-java-tools/bin
#  directory.
#


SCRIPTDIR=$(dirname $0)
BASEDIR=$(cd $SCRIPTDIR/..; pwd)

JAR_FILE=oam-java-tools-1.0-SNAPSHOT-jar-with-dependencies.jar
MAIN_CLASS=org.openaviationmap.converter.Converter

JAVA=$(which java)
if [ ! -x $JAVA ]; then
    echo "java executable not found, can't continue";
    exit 1;
fi

# check for the jar file to exist, build if not
if [ ! -f "$BASEDIR/target/$JAR_FILE" ]; then
    echo "required jar file not found, building via maven";
    MVN=$(which mvn);
    if [ ! -x $MVN ]; then
        echo "maven executable not found, can't build";
        exit 1;
    fi

    $MVN -DskipTests=true assembly:assembly;
fi


# off we go
java -cp $BASEDIR/target/$JAR_FILE $MAIN_CLASS $*


