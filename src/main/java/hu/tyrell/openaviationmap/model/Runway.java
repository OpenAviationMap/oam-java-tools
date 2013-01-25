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
 * A class representing a runway.
 */
public class Runway {
    /**
     * Runway designator.
     */
    private String designator;

    /**
     * True bearing in degrees, magnetic.
     */
    private double bearing;

    /**
     * The length of the runway.
     */
    private Distance length;

    /**
     * The width of the runway.
     */
    private Distance width;

    /**
     * Threshold coordinates, on the runway center line.
     */
    private Point threshold;

    /**
     * Runway end coordinates, on the runway center line.
     */
    private Point end;

    /**
     * Elevation at the threshold.
     */
    private Elevation elevation;

    /**
     * The surface type.
     */
    private SurfaceType surface;

    /**
     * Slope, in percentage.
     */
    private double slope;

    /**
     * Take-off runway available.
     */
    private Distance tora;

    /**
     * Take-off distance available.
     */
    private Distance toda;

    /**
     * Accelerate and stop distance available.
     */
    private Distance asda;

    /**
     * Landing distance available.
     */
    private Distance lda;

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
     * @return the bearing
     */
    public double getBearing() {
        return bearing;
    }

    /**
     * @param bearing the bearing to set
     */
    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    /**
     * @return the length
     */
    public Distance getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(Distance length) {
        this.length = length;
    }

    /**
     * @return the width
     */
    public Distance getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Distance width) {
        this.width = width;
    }

    /**
     * @return the threshold
     */
    public Point getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(Point threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the end
     */
    public Point getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Point end) {
        this.end = end;
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
     * @return the surface
     */
    public SurfaceType getSurface() {
        return surface;
    }

    /**
     * @param surface the surface to set
     */
    public void setSurface(SurfaceType surface) {
        this.surface = surface;
    }

    /**
     * @return the slope
     */
    public double getSlope() {
        return slope;
    }

    /**
     * @param slope the slope to set
     */
    public void setSlope(double slope) {
        this.slope = slope;
    }

    /**
     * @return the tora
     */
    public Distance getTora() {
        return tora;
    }

    /**
     * @param tora the tora to set
     */
    public void setTora(Distance tora) {
        this.tora = tora;
    }

    /**
     * @return the toda
     */
    public Distance getToda() {
        return toda;
    }

    /**
     * @param toda the toda to set
     */
    public void setToda(Distance toda) {
        this.toda = toda;
    }

    /**
     * @return the asda
     */
    public Distance getAsda() {
        return asda;
    }

    /**
     * @param asda the asda to set
     */
    public void setAsda(Distance asda) {
        this.asda = asda;
    }

    /**
     * @return the lda
     */
    public Distance getLda() {
        return lda;
    }

    /**
     * @param lda the lda to set
     */
    public void setLda(Distance lda) {
        this.lda = lda;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((asda == null) ? 0 : asda.hashCode());
        long temp;
        temp = Double.doubleToLongBits(bearing);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((designator == null) ? 0 : designator.hashCode());
        result = prime * result
                + ((elevation == null) ? 0 : elevation.hashCode());
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((lda == null) ? 0 : lda.hashCode());
        result = prime * result + ((length == null) ? 0 : length.hashCode());
        temp = Double.doubleToLongBits(slope);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((surface == null) ? 0 : surface.hashCode());
        result = prime * result
                + ((threshold == null) ? 0 : threshold.hashCode());
        result = prime * result + ((toda == null) ? 0 : toda.hashCode());
        result = prime * result + ((tora == null) ? 0 : tora.hashCode());
        result = prime * result + ((width == null) ? 0 : width.hashCode());
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
        Runway other = (Runway) obj;
        if (asda == null) {
            if (other.asda != null) {
                return false;
            }
        } else if (!asda.equals(other.asda)) {
            return false;
        }
        if (Double.doubleToLongBits(bearing) != Double
                .doubleToLongBits(other.bearing)) {
            return false;
        }
        if (designator == null) {
            if (other.designator != null) {
                return false;
            }
        } else if (!designator.equals(other.designator)) {
            return false;
        }
        if (elevation == null) {
            if (other.elevation != null) {
                return false;
            }
        } else if (!elevation.equals(other.elevation)) {
            return false;
        }
        if (end == null) {
            if (other.end != null) {
                return false;
            }
        } else if (!end.equals(other.end)) {
            return false;
        }
        if (lda == null) {
            if (other.lda != null) {
                return false;
            }
        } else if (!lda.equals(other.lda)) {
            return false;
        }
        if (length == null) {
            if (other.length != null) {
                return false;
            }
        } else if (!length.equals(other.length)) {
            return false;
        }
        if (Double.doubleToLongBits(slope) != Double
                .doubleToLongBits(other.slope)) {
            return false;
        }
        if (surface != other.surface) {
            return false;
        }
        if (threshold == null) {
            if (other.threshold != null) {
                return false;
            }
        } else if (!threshold.equals(other.threshold)) {
            return false;
        }
        if (toda == null) {
            if (other.toda != null) {
                return false;
            }
        } else if (!toda.equals(other.toda)) {
            return false;
        }
        if (tora == null) {
            if (other.tora != null) {
                return false;
            }
        } else if (!tora.equals(other.tora)) {
            return false;
        }
        if (width == null) {
            if (other.width != null) {
                return false;
            }
        } else if (!width.equals(other.width)) {
            return false;
        }
        return true;
    }
}
