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
package org.openaviationmap.model;

/**
 * A Magnetic variation, including the year of measurement.
 */
public class MagneticVariation {
    /**
     * The magnetic variation.
     */
    private double variation;

    /**
     * The year of measurement.
     */
    private int year;

    /**
     * Default constructor.
     */
    public MagneticVariation() {
        variation = 0;
        year      = 0;
    }

    /**
     * Constructor with initialization values.
     *
     * @param variation the magnetic variation.
     * @param year the year of measurement.
     */
    public MagneticVariation(double variation, int year) {
        this.variation = variation;
        this.year = year;
    }

    /**
     * @return the variation
     */
    public double getVariation() {
        return variation;
    }

    /**
     * @param variation the variation to set
     */
    public void setVariation(double variation) {
        this.variation = variation;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(variation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + year;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MagneticVariation other = (MagneticVariation) obj;
        if (Double.doubleToLongBits(variation) != Double
                .doubleToLongBits(other.variation)) {
            return false;
        }
        if (year != other.year) {
            return false;
        }
        return true;
    }

}
