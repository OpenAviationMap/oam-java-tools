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

import java.util.List;

/**
 * A series of points, where the last point is the same as the first one.
 */
public class Ring implements Boundary {
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
}
