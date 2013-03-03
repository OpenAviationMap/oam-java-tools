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
 * An airspace boundary.
 */
public interface Boundary {
    /** The type of the boundary. */
    public enum Type {
        /**
         * A boundary which is a closed polygon - aka. ring.
         */
        RING,

        /**
         * A boundary which is a circle, with a center point and a radius.
         */
        CIRCLE,

        /**
         * A boundary made up of a number of other boundaries.
         */
        COMPOUND;
    }

    /**
     * Return the type of boundary.
     *
     * @return the type of boundary.
     */
    Type getType();
}
