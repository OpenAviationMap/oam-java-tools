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

/**
 * A member of an OSM relationship.
 */
public class Member {
    /**
     * The type of member, e.g. 'node' or 'way'.
     */
    public enum Type {
        /**
         * A node member.
         */
        NODE,

        /**
         * A way member.
         */
        WAY
    }

    /**
     * The type of relation.
     */
    private Type type;

    /**
     * the OSM node id that the relationship refers to.
     */
    private int ref;

    /**
     * The role of this relation.
     */
    private String role;

    /**
     * Default constructor.
     */
    public Member() {
        super();
    }

    /**
     * Constructor based on initial values.
     *
     * @param type the type of relation.
     * @param ref the node that is referenced from this one.
     * @param role the role of the member in the relation.
     */
    public Member(Type          type,
                  int           ref,
                  String        role) {
        this.type  = type;
        this.ref   = ref;
        this.role  = role;
    }

    /**
     * Compare two member objects by value. That is, by their type & role,
     * but not by the ref id.
     *
     * @param other the other Member object to compare this one to.
     * @return true if this object equals the other by value.
     */
    public boolean compare(Member other) {
        if (type != other.type) {
            return false;
        }
        if (role == null && other.role != null) {
            return false;
        }
        if (!role.equals(other.role)) {
            return false;
        }

        return true;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

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
     * @return the ref
     */
    public int getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(int ref) {
        this.ref = ref;
    }
}
