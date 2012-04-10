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
 * A navigation aid.
 */
public class Navaid extends Point {
    /**
     * Navigation aid type.
     */
    public enum Type {
        /**
         * A VOR or DVOR.
         */
        VOR,

        /**
         * A DME.
         */
        DME,

        /**
         * An NDB.
         */
        NDB,

        /**
         * A co-located VOR-DME.
         */
        VORDME,

        /**
         * A marker beacon.
         */
        MARKER,

        /**
         * The localizer of an ILS facility.
         */
        LOC,

        /**
         * The glidepath localizer of an ILS facility.
         */
        GP,

        /**
         * A designated point / GPS reporting point.
         */
        DESIGNATED
    }

    /**
     * The unique id of the navaid.
     */
    private String id;

    /**
     * The name of the navaid.
     */
    private String name;

    /**
     * The ident of the navaid.
     */
    private String ident;

    /**
     * The type of navaid.
     */
    private Type type;

    /**
     * The frequency.
     */
    private Frequency frequency;

    /**
     * The DME channel, if any.
     */
    private String dmeChannel;

    /**
     * The declination at the navaid, in degrees.
     */
    private double declination;

    /**
     * The magnetic variation at the navaid.
     */
    private MagneticVariation variation;

    /**
     * The elevation of the navaid.
     */
    private Elevation elevation;

    /**
     * The glide path angle, if applicable.
     */
    private double angle;

    /**
     * The coverage of the navaid.
     */
    private Distance coverage;

    /**
     * The active time of the navaid.
     */
    private String activetime;

    /**
     * Generic remarks.
     */
    private String remarks;

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the frequency
     */
    public Frequency getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the declination
     */
    public double getDeclination() {
        return declination;
    }

    /**
     * @param declination the declination to set
     */
    public void setDeclination(double declination) {
        this.declination = declination;
    }

    /**
     * @return the elevation
     */
    public Elevation getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

    /**
     * @return the coverage
     */
    public Distance getCoverage() {
        return coverage;
    }

    /**
     * @param coverage the coverage to set
     */
    public void setCoverage(Distance coverage) {
        this.coverage = coverage;
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

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

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
     * @return the ident
     */
    public String getIdent() {
        return ident;
    }

    /**
     * @param ident the ident to set
     */
    public void setIdent(String ident) {
        this.ident = ident;
    }

    /**
     * @return the dmeChannel
     */
    public String getDmeChannel() {
        return dmeChannel;
    }

    /**
     * @param dmeChannel the dmeChannel to set
     */
    public void setDmeChannel(String dmeChannel) {
        this.dmeChannel = dmeChannel;
    }

    /**
     * @return the activetime
     */
    public String getActivetime() {
        return activetime;
    }

    /**
     * @param activetime the activetime to set
     */
    public void setActivetime(String activetime) {
        this.activetime = activetime;
    }

    /**
     * @return the variation
     */
    public MagneticVariation getVariation() {
        return variation;
    }

    /**
     * @param variation the variation to set
     */
    public void setVariation(MagneticVariation variation) {
        this.variation = variation;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((activetime == null) ? 0 : activetime.hashCode());
        long temp;
        temp = Double.doubleToLongBits(angle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((coverage == null) ? 0 : coverage.hashCode());
        temp = Double.doubleToLongBits(declination);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((dmeChannel == null) ? 0 : dmeChannel.hashCode());
        result = prime * result
                + ((elevation == null) ? 0 : elevation.hashCode());
        result = prime * result
                + ((frequency == null) ? 0 : frequency.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ident == null) ? 0 : ident.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((remarks == null) ? 0 : remarks.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result
                + ((variation == null) ? 0 : variation.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Navaid other = (Navaid) obj;
        if (activetime == null) {
            if (other.activetime != null) {
                return false;
            }
        } else if (!activetime.equals(other.activetime)) {
            return false;
        }
        if (Double.doubleToLongBits(angle) != Double
                .doubleToLongBits(other.angle)) {
            return false;
        }
        if (coverage == null) {
            if (other.coverage != null) {
                return false;
            }
        } else if (!coverage.equals(other.coverage)) {
            return false;
        }
        if (Double.doubleToLongBits(declination) != Double
                .doubleToLongBits(other.declination)) {
            return false;
        }
        if (dmeChannel == null) {
            if (other.dmeChannel != null) {
                return false;
            }
        } else if (!dmeChannel.equals(other.dmeChannel)) {
            return false;
        }
        if (elevation == null) {
            if (other.elevation != null) {
                return false;
            }
        } else if (!elevation.equals(other.elevation)) {
            return false;
        }
        if (frequency == null) {
            if (other.frequency != null) {
                return false;
            }
        } else if (!frequency.equals(other.frequency)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (ident == null) {
            if (other.ident != null) {
                return false;
            }
        } else if (!ident.equals(other.ident)) {
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
        if (type != other.type) {
            return false;
        }
        if (variation == null) {
            if (other.variation != null) {
                return false;
            }
        } else if (!variation.equals(other.variation)) {
            return false;
        }
        return true;
    }

    /**
     * @return the angle
     */
    public double getAngle() {
        return angle;
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

}
