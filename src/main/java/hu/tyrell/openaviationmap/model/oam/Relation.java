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
package hu.tyrell.openaviationmap.model.oam;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * A class representing an OSM relation.
 */
public class Relation extends OsmBaseNode {
    /**
     * The members of this relation.
     */
    private List<Member>    members;

    /**
     * Default constructor.
     */
    public Relation() {
        super();

        members = new Vector<Member>();
    }

    /**
     * Copy constructor.
     *
     * @param relation the relation to copy.
     */
    public Relation(Relation relation) {
        super(relation);

        members = new Vector<Member>(relation.members);
    }

    /**
     * @return the members
     */
    public List<Member> getMembers() {
        return members;
    }

    /**
     * @param members the members to set
     */
    public void setMembers(List<Member> members) {
        this.members = members;
    }

    /**
     * Compare a relation object to another one, by only comparing real values,
     * that is the list of members (by value, not by id), and the tags.
     * Metadata such as id, version, action are not compared.
     *
     * @param osmNodes the map of nodes that this Relation object is referring
     *        to in the members property
     * @param osmWays the map of ways that this Relation object is referring
     *        to in the members property
     * @param other the other relation to compare to this one
     * @param otherOsmNodes the map of nodes that the other Relation object
     *        is referring to in the members property
     * @param otherOsmWays the map of ways that the other Relation object
     *        is referring to in the members property
     * @return true if the two relations are equal by value, false otherwise
     */
    public boolean compare(Map<Integer, OsmNode> osmNodes,
                           Map<Integer, Way>     osmWays,
                           Relation              other,
                           Map<Integer, OsmNode> otherOsmNodes,
                           Map<Integer, Way>     otherOsmWays) {
        if (!super.compare(other)) {
            return false;
        }

        if (members.size() != other.members.size()) {
            return false;
        }

        Iterator<Member>    it1 = members.iterator();
        Iterator<Member>    it2 = other.members.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Member m1 = it1.next();
            Member m2 = it2.next();

            if (!m1.compare(m2)) {
                return false;
            }

            switch (m1.getType()) {
            case NODE:
                if (!osmNodes.containsKey(m1.getRef())
                 || !otherOsmNodes.containsKey(m2.getRef())) {
                    throw new IllegalArgumentException();
                }

                OsmNode n1 = osmNodes.get(m1.getRef());
                OsmNode n2 = otherOsmNodes.get(m2.getRef());

                if (!n1.compare(n2)) {
                    return false;
                }
                break;

            case WAY:
                if (!osmWays.containsKey(m1.getRef())
                 || !otherOsmWays.containsKey(m2.getRef())) {
                    throw new IllegalArgumentException();
                }

                Way w1 = osmWays.get(m1.getRef());
                Way w2 = otherOsmWays.get(m2.getRef());

                if (!w1.compare(w2)) {
                    return false;
                }
                break;

            default:
            }
        }

        return it1.hasNext() == it2.hasNext();
    }

}
