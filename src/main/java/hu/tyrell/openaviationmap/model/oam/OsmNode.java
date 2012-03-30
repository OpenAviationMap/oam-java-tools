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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An OAM / OSM node.
 */
public class OsmNode extends Point {
    /**
     * The id of the node.
     */
    private int id;

    /**
     * The version of the node.
     */
    private int version;

    /**
     * The state / action to be performed with this node.
     */
    private Action action;

    /**
     * Last change timestamp.
     */
    private Date timestamp;

    /**
     * Last change user id.
     */
    private Integer uid;

    /**
     * Last change user name.
     */
    private String user;

    /**
     * Flag to indicate visibility.
     */
    private Boolean visible;

    /**
     * Last change change set.
     */
    private Integer changeset;

    /**
     * Tags of this way element.
     */
    private Map<String, String> tags;

    /**
     * Default constructor.
     */
    public OsmNode() {
        tags = new HashMap<String, String>();
    }

    /**
     * Copy constructor.
     *
     * @param other the other node the base values on.
     */
    public OsmNode(OsmNode other) {
        super(other);

        id        = other.id;
        version   = other.version;
        action    = other.action;
        timestamp = other.timestamp == null
                  ? null : (Date) other.timestamp.clone();
        uid       = other.uid == null
                  ? null : new Integer(other.uid);
        user      = other.user == null
                  ? null : new String(other.user);
        visible   = other.visible == null
                  ? null : other.visible ? Boolean.TRUE : Boolean.FALSE;
        changeset = other.changeset == null
                   ? null : new Integer(other.changeset);
        tags      = other.tags == null
                  ? new HashMap<String, String>()
                  : new HashMap<String, String>(other.tags);
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
            && tags.equals(other.tags);
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
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the uid
     */
    public Integer getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the visible
     */
    public Boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the changeset
     */
    public Integer getChangeset() {
        return changeset;
    }

    /**
     * @param changeset the changeset to set
     */
    public void setChangeset(Integer changeset) {
        this.changeset = changeset;
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

    /**
     * @return the visible
     */
    public Boolean getVisible() {
        return visible;
    }
}
