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
 * A boundary made up of a number of boundaries.
 */
public class CompoundBoundary implements Boundary {
    /**
     * The boundaries that make up this boundary.
     */
    private List<Boundary> boundaryList;

    /**
     * Return the type of this boundary.
     *
     * @return Compound
     */
    @Override
    public Type getType() {
        return Type.COMPOUND;
    }

    /**
     * @return the boundaryList
     */
    public List<Boundary> getBoundaryList() {
        return boundaryList;
    }

    /**
     * @param boundaryList the boundaryList to set
     */
    public void setBoundaryList(List<Boundary> boundaryList) {
        this.boundaryList = boundaryList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((boundaryList == null) ? 0 : boundaryList.hashCode());
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
        CompoundBoundary other = (CompoundBoundary) obj;
        if (boundaryList == null) {
            if (other.boundaryList != null) {
                return false;
            }
        } else if (!boundaryList.equals(other.boundaryList)) {
            return false;
        }
        return true;
    }
}
