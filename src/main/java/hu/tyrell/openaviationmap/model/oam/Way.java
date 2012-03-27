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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An Open Street Map 'way' element.
 */
public class Way {
    /**
     * The id of the way.
     */
    private int id;

    /**
     * The version of the way.
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
     * The points that make up the closed polygon. These are id references
     * into some OsmNode map.
     */
    private List<Integer> nodeList;

    /**
     * Tags of this way element.
     */
    private Map<String, String> tags;

    /**
     * Default constructor.
     */
    public Way() {
        nodeList = new Vector<Integer>();
        tags     = new HashMap<String, String>();
        action   = Action.CREATE;
    }

    /**
     * Copy constructor.
     *
     * @param other the other object to copy from.
     */
    public Way(Way other) {
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

        nodeList = new Vector<Integer>(other.nodeList);
        tags     = new HashMap<String, String>(other.tags);
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
        if (!tags.equals(other.tags)) {
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
}
