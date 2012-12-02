#!/bin/bash
#
#    Open Aviation Map
#    Copyright (C) 2012 Ákos Maróy
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
#  Diff all unit test result files againts a baseline file
#
#  It is assumed that this script resides in the oam-java-tools/bin
#  directory.
#


SCRIPT_DIR=$(dirname $0)
BASE_DIR=$(cd $SCRIPT_DIR/..; pwd)

CONVERT_SCRIPT=$BASE_DIR/bin/oamconverter.sh

OPTS="--input-format eAIP.Hungary \
      --output-format OAM \
      --border var/hungary.osm \
      --aerodromes var/LH-AD-1.3-en-HU.xml \
      --validity-start 2012-04-08T00:00:00Z \
      --version 1"

if [ ! -x $CONVERT_SCRIPT ]; then
    echo "OAM convert script not found at $CONVERT_SCRIPT"
    echo "can't continue"
    exit 1;
fi

usage() {
    echo "Open Aviation Map eAIP to OAM conversion script"
    echo ""
    echo "Usage:";
    echo "";
    echo " -i | --input <input.xml>         input eAIP file";
    echo " -o | --output <output.aixm51>    output AIXM file";
    echo " -h | --help                      display this usage info";
}

OPTIONS=`getopt -o i:o:h --long input:,output:,help -n "$0" -- "$@"`

eval set -- "$OPTIONS"

while true; do
    case "$1" in
    -h|--help)
        usage;
        exit 0;
        ;;
    -i|--input)
        shift
        INPUT=$1
        shift
        ;;
    -o|--output)
        shift
        OUTPUT=$1
        shift
        ;;
    --)
        shift
        break
        ;;
    *)
        echo "unrecognized option $1"
        echo ""
        usage;
        exit 1;
    esac
done

if [ "x$INPUT" == "x" ]; then
    echo "required option --input missing";
    echo ""
    usage;
    exit 1;
fi

if [ "x$OUTPUT" == "x" ]; then
    echo "required option --output missing";
    echo ""
    usage;
    exit 1;
fi


$CONVERT_SCRIPT $OPTS --input $INPUT --output $OUTPUT

