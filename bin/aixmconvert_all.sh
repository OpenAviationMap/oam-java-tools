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
      --output-format AIXM \
      --border var/hungary.osm \
      --aerodromes var/LH-AD-1.3-en-HU.xml \
      --validity-start 2012-04-08T00:00:00Z \
      --version 1"

if [ ! -x $CONVERT_SCRIPT ]; then
    echo "OAM convert script not found at $CONVERT_SCRIPT"
    echo "can't continue"
    exit 1;
fi

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-2.1-en-HU.xml \
                      --output $BASE_DIR/var/hungary-2.1.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-2.2-en-HU.xml \
                      --output $BASE_DIR/var/hungary-2.2.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-4.1-en-HU.xml \
                      --output $BASE_DIR/var/hungary-4.1.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-4.4-en-HU.xml \
                      --output $BASE_DIR/var/hungary-4.4.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-5.1-en-HU.xml \
                      --output $BASE_DIR/var/hungary-5.1.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-5.2-en-HU.xml \
                      --output $BASE_DIR/var/hungary-5.2.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-5.5-en-HU.xml \
                      --output $BASE_DIR/var/hungary-5.5.aixm51

$CONVERT_SCRIPT $OPTS --input $BASE_DIR/var/LH-ENR-5.6-en-HU.xml \
                      --output $BASE_DIR/var/hungary-5.6.aixm51

