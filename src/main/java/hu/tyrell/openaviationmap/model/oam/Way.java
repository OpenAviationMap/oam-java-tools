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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An Open Street Map 'way' element.
 */
public class Way extends OsmBaseNode {
    /**
     * The points that make up the closed polygon. These are id references
     * into some OsmNode map.
     */
    private List<Integer> nodeList;

    /**
     * Default constructor.
     */
    public Way() {
        super();

        nodeList = new Vector<Integer>();
    }

    /**
     * Copy constructor.
     *
     * @param other the other object to copy from.
     */
    public Way(Way other) {
        super(other);

        nodeList = new Vector<Integer>(other.nodeList);
    }

    /**
     * Compare a way object to another one, by only comparing real values,
     * that is the list of nodes (by value, not by id), and the tags.
     * Metadata such as id, version, action are not compared.
     *
     * @param osmNodes the map of nodes that this Way object is referring
     *        to in the nodeList property
     * @param other the other way to compare to this one
     * @param otherOsmNodes the map of nodes that the other Way object
     *        is referring to in the nodeList property
     * @return true if the two ways are equal by value, false otherwise
     */
    public boolean compare(Map<Integer, OsmNode> osmNodes,
                           Way                   other,
                           Map<Integer, OsmNode> otherOsmNodes) {
        if (!super.compare(other)) {
            return false;
        }

        if (nodeList.size() != other.nodeList.size()) {
            return false;
        }

        Iterator<Integer>   it1 = nodeList.iterator();
        Iterator<Integer>   it2 = other.nodeList.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Integer i1 = it1.next();
            Integer i2 = it2.next();

            if (!osmNodes.containsKey(i1) || !otherOsmNodes.containsKey(i2)) {
                throw new IllegalArgumentException();
            }

            OsmNode n1 = osmNodes.get(i1);
            OsmNode n2 = otherOsmNodes.get(i2);

            if (!n1.compare(n2)) {
                return false;
            }
        }

        return it1.hasNext() == it2.hasNext();
    }

    /**
     * @return the nodeList
     */
    public List<Integer> getNodeList() {
        return nodeList;
    }

    /**
     * @param nodeList the nodeList to set
     */
    public void setNodeList(List<Integer> nodeList) {
        this.nodeList = nodeList;
    }

}
