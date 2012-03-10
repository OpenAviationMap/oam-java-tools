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
 * A class representing an airspace.
 */
public class Airspace {

    /**
     * The designator of the airspace.
     */
    private String designator;

    /**
     * The name of the airspace.
     */
    private String name;

    /**
     * The airspace class, A, B, C, ..., G.
     */
    private String airspaceClass;

    /**
     * The airspace type, TMA, CTR, military, Restricted, etc.
     */
    private String type;

    /**
     * The lower limit of the airspace.
     */
    private Elevation lowerLimit;

    /**
     * The upper limit of the airspace.
     */
    private Elevation upperLimit;

    /**
     * The shape of the airspace.
     */
    private Boundary boundary;

    /**
     * Remarks about the airspace.
     */
    private String remarks;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the airspaceClass
     */
    public String getAirspaceClass() {
        return airspaceClass;
    }

    /**
     * @param airspaceClass the airspaceClass to set
     */
    public void setAirspaceClass(String airspaceClass) {
        this.airspaceClass = airspaceClass;
    }

    /**
     * @return the designator
     */
    public String getDesignator() {
        return designator;
    }

    /**
     * @param designator the designator to set
     */
    public void setDesignator(String designator) {
        this.designator = designator;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the lowerLimit
     */
    public Elevation getLowerLimit() {
        return lowerLimit;
    }

    /**
     * @param lowerLimit the lowerLimit to set
     */
    public void setLowerLimit(Elevation lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * @return the upperLimit
     */
    public Elevation getUpperLimit() {
        return upperLimit;
    }

    /**
     * @param upperLimit the upperLimit to set
     */
    public void setUpperLimit(Elevation upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * @return the boundary
     */
    public Boundary getBoundary() {
        return boundary;
    }

    /**
     * @param boundary the boundary to set
     */
    public void setBoundary(Boundary boundary) {
        this.boundary = boundary;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((airspaceClass == null) ? 0 : airspaceClass.hashCode());
        result = prime * result
                + ((boundary == null) ? 0 : boundary.hashCode());
        result = prime * result
                + ((designator == null) ? 0 : designator.hashCode());
        result = prime * result
                + ((lowerLimit == null) ? 0 : lowerLimit.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((remarks == null) ? 0 : remarks.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result
                + ((upperLimit == null) ? 0 : upperLimit.hashCode());
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
        Airspace other = (Airspace) obj;
        if (airspaceClass == null) {
            if (other.airspaceClass != null) {
                return false;
            }
        } else if (!airspaceClass.equals(other.airspaceClass)) {
            return false;
        }
        if (boundary == null) {
            if (other.boundary != null) {
                return false;
            }
        } else if (!boundary.equals(other.boundary)) {
            return false;
        }
        if (designator == null) {
            if (other.designator != null) {
                return false;
            }
        } else if (!designator.equals(other.designator)) {
            return false;
        }
        if (lowerLimit == null) {
            if (other.lowerLimit != null) {
                return false;
            }
        } else if (!lowerLimit.equals(other.lowerLimit)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (remarks == null) {
            if (other.remarks != null) {
                return false;
            }
        } else if (!remarks.equals(other.remarks)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (upperLimit == null) {
            if (other.upperLimit != null) {
                return false;
            }
        } else if (!upperLimit.equals(other.upperLimit)) {
            return false;
        }
        return true;
    }
}
