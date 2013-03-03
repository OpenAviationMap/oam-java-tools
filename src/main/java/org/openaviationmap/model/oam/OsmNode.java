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
package org.openaviationmap.model.oam;

import org.openaviationmap.model.Point;

/**
 * An OAM / OSM node.
 */
public class OsmNode extends OsmBaseNode {
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
    public OsmNode() {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param other the other node the base values on.
     */
    public OsmNode(OsmNode other) {
        super(other);

        latitude  = other.latitude;
        longitude = other.longitude;
    }

    /**
     * Compare a node object to another one, by only comparing real values,
     * that is by the coordinates.
     * Metadata such as id, version, action are not compared.
     *
     * @param other the other node to compare to this one
     * @return true if the two nodes are equal by value, false otherwise
     */
    public boolean compare(OsmNode other) {
        return Math.abs(getLatitude() - other.getLatitude()) < 0.0000001
            && Math.abs(getLongitude() - other.getLongitude()) < 0.0000001
            && super.compare(other);
    }

    /**
     * Return a point object based on this OsmNode.
     *
     * @return a point object based on this OsmNode
     */
    public Point asPoint() {
        Point p = new Point();

        p.setLatitude(getLatitude());
        p.setLongitude(getLongitude());

        return p;
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
}
