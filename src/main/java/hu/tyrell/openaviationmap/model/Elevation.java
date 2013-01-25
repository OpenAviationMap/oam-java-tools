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
package hu.tyrell.openaviationmap.model;

/**
 * A class to represent a certain elevation.
 */
public class Elevation {
    /**
     * The elevation itself.
     */
    private double elevation;

    /**
     * The unit of measurement used in the elevation.
     */
    private UOM uom;

    /**
     * The reference used in the elevations.
     */
    private ElevationReference reference;

    /**
     * Default constructor.
     */
    public Elevation() {
        elevation = 0;
        uom = null;
    }

    /**
     * Elevation with initialization values.
     *
     * @param elevation the elevation
     * @param uom the unit of measurement
     * @param reference the elevation reference
     */
    public Elevation(double elevation, UOM uom, ElevationReference reference) {
        this.elevation = elevation;
        this.uom       = uom;
        this.reference = reference;
    }

    /**
     * Get the elevation in a specific unit of measurement.
     *
     * @param requestedUom the desired unit of measurement
     * @return the elevation object converted to the desired
     *         unit of measurement. returns this very object if the
     *         desired unit of measurement is the same as this one.
     */
    public Elevation inUom(UOM requestedUom) {
        if (this.uom == requestedUom) {
            return this;
        }

        double inMeters = elevation * uom.getInMeters();
        Elevation convertedElevation = new Elevation();
        convertedElevation.setUom(requestedUom);
        convertedElevation.setElevation(inMeters / requestedUom.getInMeters());
        convertedElevation.setReference(reference);

        return convertedElevation;
    }

    /**
     * @return the elevation
     */
    public double getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
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

    /**
     * @return the reference
     */
    public ElevationReference getReference() {
        return reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(ElevationReference reference) {
        this.reference = reference;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(elevation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((reference == null) ? 0 : reference.hashCode());
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
        Elevation other = (Elevation) obj;

        if (reference != other.reference) {
            return false;
        }
        if (uom != other.uom) {
            other = other.inUom(uom);
        }
        if (Math.abs(elevation - other.elevation) > 0.000001) {
            return false;
        }

        return true;
    }
}
