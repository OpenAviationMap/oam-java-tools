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

DIFF_SCRIPT=$BASE_DIR/bin/oamdiff.sh

if [ ! -x $DIFF_SCRIPT ]; then
    echo "OAM diff script not found at $DIFF_SCRIPT"
    echo "can't continue"
    exit 1;
fi

OAM_FILES_NAME="oam-hungary-2.1.xml \
                oam-hungary-2.2.xml"

OAM_FILES_ICAO="oam-hungary-5.1.xml \
                oam-hungary-5.2.xml \
                oam-hungary-5.5.xml \
                oam-hungary-5.6.xml \
                oam-hungary-lhbc.xml \
                oam-hungary-lhbp.xml \
                oam-hungary-lhdc.xml \
                oam-hungary-lhfm.xml \
                oam-hungary-lhny.xml \
                oam-hungary-lhpp.xml \
                oam-hungary-lhpr.xml \
                oam-hungary-lhsm.xml \
                oam-hungary-lhud.xml"

OAM_FILES_ID="oam-hungary-4.1.xml \
              oam-hungary-4.4.xml"

usage() {
    echo "Open Aviation Map diff all to a baseline script"
    echo ""
    echo "Usage:";
    echo "";
    echo " -b | --base <base.file>      base file to compare the input to";
    echo " -h | --help                  display this usage info";
}

OPTIONS=`getopt -o bh --long base,help -n "$0" -- "$@"`

eval set -- "$OPTIONS"

while true; do
    case "$1" in
    -h|--help)
        usage;
        exit 0;
        ;;
    -b|--base)
        shift
        BASE=$2
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

if [ "x$BASE" == "x" ]; then
    echo "required option --base missing";
    echo ""
    usage;
    exit 1;
fi

for OAM_FILE in $OAM_FILES_ID; do
    echo "Creating diff of $BASE_DIR/var/$OAM_FILE";

    FILE_BASE=$(basename $OAM_FILE .xml)
    DIFF_FILE=$FILE_BASE-diff.xml

    $DIFF_SCRIPT --input $BASE_DIR/var/$OAM_FILE \
                 --base $BASE \
                 --idtag id \
                 --output $BASE_DIR/var/$DIFF_FILE \
                 --new --changed
done

for OAM_FILE in $OAM_FILES_NAME; do
    echo "Creating diff of $BASE_DIR/var/$OAM_FILE";

    FILE_BASE=$(basename $OAM_FILE .xml)
    DIFF_FILE=$FILE_BASE-diff.xml

    $DIFF_SCRIPT --input $BASE_DIR/var/$OAM_FILE \
                 --base $BASE \
                 --idtag name \
                 --output $BASE_DIR/var/$DIFF_FILE \
                 --new --changed
done

for OAM_FILE in $OAM_FILES_ICAO; do
    echo "Creating diff of $BASE_DIR/var/$OAM_FILE";

    FILE_BASE=$(basename $OAM_FILE .xml)
    DIFF_FILE=$FILE_BASE-diff.xml

    $DIFF_SCRIPT --input $BASE_DIR/var/$OAM_FILE \
                 --base $BASE \
                 --idtag icao \
                 --output $BASE_DIR/var/$DIFF_FILE \
                 --new --changed
done
