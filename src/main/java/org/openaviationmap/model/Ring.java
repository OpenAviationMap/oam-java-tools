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

import java.util.List;

/**
 * A series of points, where the last point is the same as the first one.
 */
public class Ring implements Boundary {
    /**
     * The points that make up the closed polygon.
     */
    private List<Point> pointList;

    /**
     * Return the type of this boundary.
     *
     * @return Ring
     */
    @Override
    public Type getType() {
        return Type.RING;
    }

    /**
     * @return the pointList
     */
    public List<Point> getPointList() {
        return pointList;
    }

    /**
     * @param pointList the pointList to set
     */
    public void setPointList(List<Point> pointList) {
        this.pointList = pointList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((pointList == null) ? 0 : pointList.hashCode());
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
        Ring other = (Ring) obj;
        if (pointList == null) {
            if (other.pointList != null) {
                return false;
            }
        } else if (!pointList.equals(other.pointList)) {
            return false;
        }
        return true;
    }
}
