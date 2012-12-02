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

CONVERT_SCRIPT=$BASE_DIR/bin/eaip_to_oam.sh

if [ ! -x $CONVERT_SCRIPT ]; then
    echo "OAM convert script not found at $CONVERT_SCRIPT"
    echo "can't continue"
    exit 1;
fi

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-2.1-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-2.1.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-2.2-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-2.2.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-4.1-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-4.1.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-4.4-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-4.4.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-5.1-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-5.1.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-5.2-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-5.2.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-5.5-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-5.5.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-ENR-5.6-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-5.6.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHBC-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhbc.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHBP-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhbp.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHDC-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhdc.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHFM-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhfm.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHNY-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhny.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHPP-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhpp.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHPR-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhpr.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHSM-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhsm.xml

$CONVERT_SCRIPT --input $BASE_DIR/var/LH-AD-LHUD-en-HU.xml \
                --output $BASE_DIR/var/oam-hungary-lhud.xml

