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
 * An object describing distance.
 */
public class Distance {
    /**
     * The distance measurement.
     */
    private double distance;

    /**
     * The unit of measurement used.
     */
    private UOM uom;

    /**
     * Default constructor.
     */
    public Distance() {
        distance = 0;
        uom      = null;
    }

    /**
     * Constructor with initial values.
     *
     * @param distance the distance value
     * @param uom the distance unit of measurement.
     */
    public Distance(double distance, UOM uom) {
        this.distance = distance;
        this.uom      = uom;
    }

    /**
     * Get the distance in a specific unit of measurement.
     *
     * @param requestedUom the desired unit of measurement
     * @return the distance object converted to the desired
     *         unit of measurement. returns this very object if the
     *         desired unit of measurement is the same as this one.
     */
    public Distance inUom(UOM requestedUom) {
        if (this.uom == requestedUom) {
            return this;
        }

        double inMeters = distance * uom.getInMeters();
        Distance convertedDistance = new Distance();
        convertedDistance.setUom(requestedUom);
        convertedDistance.setDistance(inMeters / requestedUom.getInMeters());

        return convertedDistance;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * @return the uom
     */
    public UOM getUom() {
        return uom;
    }

    /**
     * @param uom the uom to set
     */
    public void setUom(UOM uom) {
        this.uom = uom;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((uom == null) ? 0 : uom.hashCode());
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
        Distance other = (Distance) obj;
        if (Double.doubleToLongBits(distance) != Double
                .doubleToLongBits(other.distance)) {
            return false;
        }
        if (uom != other.uom) {
            return false;
        }
        return true;
    }
}
