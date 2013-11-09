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

RENDERMAP=$BASEDIR/bin/rendermap.sh
RENDERING_DIR=file:///home/akos/src/rendering
OUTPUT_DIR=/var/www/static/generated_new
OAM_DB=localhost,oam_test,oam,XXX
OSM_DB=localhost,osm_world,osm,XXX
export JAVA_OPTS=-Xmx12g


$RENDERMAP --oam $OAM_DB \
           --osm $OSM_DB \
           --levels 0,4 \
           --output $OUTPUT_DIR \
           --sldurl $RENDERING_DIR/zoom/0 \
           --dpi 96 --type tileset --force \
           --coverage -20,30,40,89


$RENDERMAP --oam $OAM_DB \
           --osm $OSM_DB \
           --levels 5,7 \
           --output $OUTPUT_DIR \
           --sldurl $RENDERING_DIR/zoom/5 \
           --dpi 96 --type tileset --force \
           --coverage -20,30,40,89


$RENDERMAP --oam $OAM_DB \
           --osm $OSM_DB \
           --levels 8,15 \
           --output $OUTPUT_DIR \
           --sldurl $RENDERING_DIR/zoom/9 \
           --dpi 96 --type tileset --force \
           --coverage -20,30,40,89


