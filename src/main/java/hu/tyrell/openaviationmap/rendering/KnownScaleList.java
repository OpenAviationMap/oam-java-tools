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
package hu.tyrell.openaviationmap.rendering;

import java.util.ArrayList;
import java.util.Collections;
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
     * The returned list is *not* the 900913 scaling list, but the 'middle
     * values' of this scaling list, so that the resulting scaling can be used
     * in minimum and maximum scale denominators in an SLD file.
     *
     * @param depth the depth of the scaling needed
     * @return a scaling list of the desired depth
     */
    public static List<Double>
    epsg900913ScaleList(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException(
                                        "scale depth should be non-negative");
        }

        final double      level0 = 559082263.9508929 * 1.5d;
        ArrayList<Double> list   = new ArrayList<Double>(depth);
        double            scale  = level0;

        for (int i = 0; i < depth; ++i) {
            list.add(scale);
            scale /= 2.0d;
        }

        Collections.reverse(list);

        return list;
    }
}
