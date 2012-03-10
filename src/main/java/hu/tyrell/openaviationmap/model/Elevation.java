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
}
