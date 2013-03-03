/*
    Open Aviation Map
    Copyright (C) 2012-2013 Ákos Maróy

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openaviationmap.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate scaling lists for known scaling sequences.
 *
 * @see ScaleSLD
 */
public final class KnownScaleList {
    /**
     * Private constructor.
     */
    private KnownScaleList() {
    }

    /**
     * Generate a scaling list suitable for an EPSG:900913 CRS.
     *
     * @param dpi the dpi value used to generate the scale
     * @param depth the depth of the scaling needed
     * @return a scaling list of the desired depth
     */
    public static List<Double>
    epsg900913ScaleList(double dpi, int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException(
                                        "scale depth should be non-negative");
        }

        // this magic numbers comes from
        // http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Resolution_and_Scale
        final double equatorInMeters = 40075016.686;
        // a tile is 256 pixels wide
        final double level0MetersPerPixel = equatorInMeters / 256d;
        // there are 39.37 inches per meter
        final double level0Scale = dpi * 39.37d * level0MetersPerPixel;

        ArrayList<Double> list   = new ArrayList<Double>(depth);
        double            scale  = level0Scale;

        for (int i = 0; i < depth; ++i) {
            list.add(scale);
            scale /= 2.0d;
        }

        return list;
    }
}
