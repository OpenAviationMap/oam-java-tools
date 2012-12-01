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

import java.util.ArrayList;

/**
 * A boundary type described as a circle of some radius around a center
 * point.
 */
public class Circle implements Boundary {
    /**
     * The number of points used to approximate a circle with a polygon.
     */
    private static final int POLY_POINTS = 32;

    /**
     * The center point of the circle.
     */
    private Point center;

    /**
     * The radius of the circle.
     */
    private Distance radius;

    /**
     * Create a polygon approximation of the circle.
     *
     * @return a polygon approximation of the circle.
     */
    public Ring approximate() {
        double radiusInNm  = radius.inUom(UOM.NM).getDistance();
        double radiusInDeg = radiusInNm / 60.0;
        double radiusLat   = radiusInDeg;
        double radiusLon   = radiusInDeg / Math.cos(
                                       Math.toRadians(center.getLatitude()));

        double       tpHalf      = POLY_POINTS / 2.0;
        ArrayList<Point> points  = new ArrayList<Point>(POLY_POINTS + 1);
        for (int i = 0; i < POLY_POINTS; ++i) {
            double theta = Math.PI * i / tpHalf;
            double x = center.getLongitude()
                    + (radiusLon * Math.cos(theta));
            double y = center.getLatitude()
                    + (radiusLat * Math.sin(theta));

            Point p = new Point();
            p.setLongitude(x);
            p.setLatitude(y);

            points.add(p);
        }

        // close the ring
        Point p = new Point();
        p.setLongitude(points.get(0).getLongitude());
        p.setLatitude(points.get(0).getLatitude());
        points.add(p);

        Ring r = new Ring();
        r.setPointList(points);

        return r;
    }

    /**
     * Return the type of this boundary.
     *
     * @return Circle
     */
    @Override
    public Type getType() {
        return Type.CIRCLE;
    }

    /**
     * @return the center
     */
    public Point getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Point center) {
        this.center = center;
    }

    /**
     * @return the radius
     */
    public Distance getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(Distance radius) {
        this.radius = radius;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((center == null) ? 0 : center.hashCode());
        result = prime * result + ((radius == null) ? 0 : radius.hashCode());
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
        Circle other = (Circle) obj;
        if (center == null) {
            if (other.center != null) {
                return false;
            }
        } else if (!center.equals(other.center)) {
            return false;
        }
        if (radius == null) {
            if (other.radius != null) {
                return false;
            }
        } else if (!radius.equals(other.radius)) {
            return false;
        }
        return true;
    }

}
