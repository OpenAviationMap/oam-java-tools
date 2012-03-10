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
package hu.tyrell.openaviationmap.model.oam;

import hu.tyrell.openaviationmap.model.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An Open Street Map 'way' element.
 */
public class Way {
    /**
     * The points that make up the closed polygon.
     */
    private List<Point> pointList;

    /**
     * Tags of this way element.
     */
    private Map<String, String> tags;

    /**
     * Default constructor.
     */
    public Way() {
        pointList = new Vector<Point>();
        tags      = new HashMap<String, String>();
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

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
