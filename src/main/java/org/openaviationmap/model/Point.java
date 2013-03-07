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
 * A 2 dimensional geographical point in space.
 */
public class Point {
    /**
     * The latitude.
     */
    private double latitude;

    /**
     * The longitude.
     */
    private double longitude;

    /**
     * Default constructor.
     */
    public Point() {
    }

    /**
     * Constructor that accepts the latitude and longitude directly.
     *
     * @param latitude The double value for the latitude.
     *
     * @param longitude The double value for the longitude.
     *
     */
    public Point(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Copy constructor.
     *
     * @param other the other object to get values from.
     */
    public Point(Point other) {
        latitude = other.latitude;
        longitude = other.longitude;
    }

    /**
     * Calculate the distance between two points.
     *
     * @param other the other point to calculate the distance from.
     * @return the distance between the two points
     */
    public double distance(Point other) {
        double latDif = latitude - other.latitude;
        double lonDif = longitude - other.longitude;

        return Math.sqrt(latDif * latDif + lonDif * lonDif);
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Give a distance and a bearing, create a new Point that is displaced from
     * the current Point.
     * 
     * @param displacement The distance to displace.
     * 
     * @param bearing The bear to display to.
     * 
     * @return A new Point at the given displacement.
     */
    public Point displace(Distance displacement, double bearing) {
        Point newPoint = new Point();
        double distance = (displacement.inUom(UOM.M)).getDistance() / 1000.0;
        double PI180 = Math.PI / 180.0d;
        double PIUnder180 = 180.0d / Math.PI;

        /* The radius of the earth such that d/R = angular distance in radians. */
        double angularDisplacement = distance / 6378.137d;
        double cosAngularDisplacement = Math.cos(angularDisplacement);
        double sinAngularDisplacement = Math.sin(angularDisplacement);
        double lat1 = this.getLatitude() * PI180;
        double lon1 = this.getLongitude() * PI180;
        double bearingRad = bearing * PI180;

        double lat2 = Math.asin(Math.sin(lat1) * cosAngularDisplacement
                + Math.cos(lat1) * sinAngularDisplacement
                * Math.cos(bearingRad));

        double lon2 = lon1
                + Math.atan2(
                        Math.sin(bearingRad) * sinAngularDisplacement
                                * Math.cos(lat1),
                        cosAngularDisplacement - Math.sin(lat1)
                                * Math.sin(lat2));

        return new Point(lat2 * PIUnder180, lon2 * PIUnder180);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Point other = (Point) obj;
        if (Double.doubleToLongBits(latitude) != Double
                .doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(longitude) != Double
                .doubleToLongBits(other.longitude)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Point [latitude=" + latitude + ", longitude=" + longitude + "]";
    }
}
