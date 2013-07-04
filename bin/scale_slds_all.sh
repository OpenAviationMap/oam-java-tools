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

SCALE_SCRIPT=$BASEDIR/bin/scale_sld.sh

if [ ! -x $SCALE_SCRIPT ]; then
    echo "OAM SLD scale script not found at $SCALE_SCRIPT"
    echo "can't continue"
    exit 1;
fi

usage() {
    echo "Open Aviation Map Scale all SLDs script"
    echo ""
    echo "Usage:";
    echo "";
    echo " -d | --directory <path/to/dir> path to the OAM rendering directory";
    echo " -h | --help                    display this usage info";
}

OPTIONS=`getopt -o dh --long directory,help -n "$0" -- "$@"`

eval set -- "$OPTIONS"

while true; do
    case "$1" in
    -h|--help)
        usage;
        exit 0;
        ;;
    -d|--directory)
        shift
        DIRECTORY=$2
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

if [ "x$DIRECTORY" == "x" ]; then
    echo "required option --directory missing";
    echo ""
    usage;
    exit 1;
fi


$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_cities.sldt --output $DIRECTORY/oam_cities.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_city_markers.sldt --output $DIRECTORY/oam_city_markers.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_peaks.sldt --output $DIRECTORY/oam_peaks.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_roads.sldt --output $DIRECTORY/oam_roads.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_waters.sldt --output $DIRECTORY/oam_waters.sld


$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_airspaces.sldt --output $DIRECTORY/oam_airspaces.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_navaids.sldt --output $DIRECTORY/oam_navaids.sld

$SCALE_SCRIPT --crs EPSG:900913 --scales EPSG:900913 \
              --input $DIRECTORY/oam_labels.sldt --output $DIRECTORY/oam_labels.sld


