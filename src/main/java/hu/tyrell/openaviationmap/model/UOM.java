/*
    Open Aviation Map
    Copyright (C) 2012 Ákos Maróy

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
package hu.tyrell.openaviationmap.model;

/**
 * Enumeration of units of measurements, like feet, meters, flight level,
 * mostly for elevation / height.
 */
public enum UOM {
    /**
     * Feet.
     */
    FT(0.3048),

    /**
     * Meters.
     */
    M(1.0),

    /**
     * Flight level = in 100 feet units.
     */
    FL(304.8),

    /**
     * Nautical miles.
     */
    NM(1852);

    /**
     * Return an UOM value based on a string description.
     *
     * @param str the string representation of the unit of measurement.
     * @return the UOM corresponding the string representation.
     */
    public static UOM fromString(String str) {
        String s = str.trim().toLowerCase();

        if ("ft".equals(s) || "feet".equals(s)) {
            return FT;
        }
        if ("m".equals(s) || "meter".equals(s) || "meters".equals(s)) {
            return M;
        }
        if ("fl".equals(s)) {
            return FL;
        }
        if ("nm".equals(s)) {
            return NM;
        }

        throw new IllegalArgumentException();
    }

    /**
     * The length of this unit of measurement in meters.
     */
    private double inMeters;

    /**
     * Constructor.
     *
     * @param inMeters the length of this unit of measurement in meters.
     */
    private UOM(double inMeters) {
        this.inMeters = inMeters;
    }

    /**
     * @return the inMeters
     */
    public double getInMeters() {
        return inMeters;
    }
}
