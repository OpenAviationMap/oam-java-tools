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
package org.openaviationmap.model.oam;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * An OAM / OSM document, which is a collection of nodes & ways.
 */
public class Oam {
    /**
     * The nodes in the OAM document.
     */
    private Map<Integer, OsmNode> nodes;

    /**
     * The ways in the OAM document.
     */
    private Map<Integer, Way> ways;

    /**
     * The relations in the OAM document.
     */
    private Map<Integer, Relation> relations;

    /**
     * Default constructor.
     */
    public Oam() {
        nodes     = new TreeMap<Integer, OsmNode>();
        ways      = new TreeMap<Integer, Way>();
        relations = new TreeMap<Integer, Relation>();
    }

    /**
     * Return the maximum absolute value of node ids. Useful for generating
     * new unique ids.
     *
     * @return the maximum absolute value of node ids.
     */
    public int getMaxNodeId() {
        int max = 0;
        int min = 0;
        for (int k : nodes.keySet()) {
            if (max < k) {
                max = k;
            }
            if (min > k) {
                min = k;
            }
        }

        return max > -min ? max : -min;
    }

    /**
     * Import nodes related to a Way object.
     * Note: it is assumed that the Way & associated nodes have ids
     * that will not collide with ids in this Oam.
     *
     * @param way the Way object to import the nodes for.
     * @param nodeMap a map of OsmNode objects, into which the Way objects
     *        supplied references point to.
     */
    public void importWayNodes(Way way, Map<Integer, OsmNode> nodeMap) {
        for (Integer i : way.getNodeList()) {
            OsmNode n = new OsmNode(nodeMap.get(i));
            nodes.put(n.getId(), n);
        }
    }

    /**
     * Import nodes and ways related to a Relation object.
     * Note: it is assumed that the Relation & associated nodes and ways have
     * ids that will not collide with ids in this Oam.
     *
     * @param relation the Relation object to import the nodes for.
     * @param nodeMap a map of OsmNode objects, into which the Relation objects
     *        supplied references point to.
     * @param wayMap a map of Way objects, into which the Relation objects
     *        supplied references point to.
     */
    public void importRelNodesWays(Relation              relation,
                                   Map<Integer, OsmNode> nodeMap,
                                   Map<Integer, Way>     wayMap) {
        for (Member m : relation.getMembers()) {
            switch (m.getType()) {
            case NODE:
                OsmNode n = new OsmNode(nodeMap.get(m.getRef()));
                nodes.put(n.getId(), n);
                break;

            case WAY:
                Way w = new Way(wayMap.get(m.getRef()));
                ways.put(w.getId(), w);
                importWayNodes(w, nodeMap);
                break;

            default:
            }
        }
    }

    /**
     * Compare this OAM to another one, by comparing the values of the
     * nodes and ways.
     * Metadata, such as id, version and action are not compared.
     * Nodes are identified by a tag named idTag. Nodes that don't have this
     * tag are simply ignored.
     *
     * @param other the other OAM to compare
     * @param idTag the tag used to uniquely identify nodes.
     * @return true if the to OAM objects are the same by value, false otherwise
     */
    public boolean compare(Oam other, String idTag) {

        // compare the nodes
        Map<String, OsmNode> tagNodeMap      = new HashMap<String, OsmNode>();
        Map<String, OsmNode> otherTagNodeMap = new HashMap<String, OsmNode>();

        for (OsmNode n : nodes.values()) {
            if (n.getTags().containsKey(idTag)) {
                tagNodeMap.put(n.getTags().get(idTag), n);
            }
        }

        for (OsmNode n : other.nodes.values()) {
            if (n.getTags().containsKey(idTag)) {
                otherTagNodeMap.put(n.getTags().get(idTag), n);
            }
        }

        if (!tagNodeMap.keySet().containsAll(otherTagNodeMap.keySet())
         || !otherTagNodeMap.keySet().containsAll(tagNodeMap.keySet())) {
             return false;
         }

        for (String tag : tagNodeMap.keySet()) {
            OsmNode n1 = tagNodeMap.get(tag);
            OsmNode n2 = otherTagNodeMap.get(tag);

            if (!n1.compare(n2)) {
                return false;
            }
        }

        // compare the ways
        Map<String, Way> tagWayMap      = new HashMap<String, Way>();
        Map<String, Way> otherTagWayMap = new HashMap<String, Way>();

        for (Way w : ways.values()) {
            if (w.getTags().containsKey(idTag)) {
                tagWayMap.put(w.getTags().get(idTag), w);
            }
        }

        for (Way w : other.ways.values()) {
            if (w.getTags().containsKey(idTag)) {
                otherTagWayMap.put(w.getTags().get(idTag), w);
            }
        }

        if (!tagWayMap.keySet().containsAll(otherTagWayMap.keySet())
         || !otherTagWayMap.keySet().containsAll(tagWayMap.keySet())) {
             return false;
         }

        for (String tag : tagWayMap.keySet()) {
            Way w1 = tagWayMap.get(tag);
            Way w2 = otherTagWayMap.get(tag);

            if (!w1.compare(nodes, w2, other.nodes)) {
                return false;
            }
        }

        // TODO: compare the relations

        return true;
    }

    /**
     * Merge content from another OAM into this one.
     * Note: it is assumed that the Way & associated nodes on the other Oam
     * have ids that will not collide with ids in this Oam.

     * @param other the other OAM to merge content from.
     */
    public void merge(Oam other) {
        for (OsmNode node : other.nodes.values()) {
            nodes.put(node.getId(), node);
        }
        for (Way way : other.ways.values()) {
            ways.put(way.getId(), way);
        }
        for (Relation relation : other.relations.values()) {
            relations.put(relation.getId(), relation);
        }
    }

    /**
     * Limit the lenght of tag values to 255 characters, as the OSM schema
     * doesn't take more.
     */
    public void limitTagLength() {
        for (OsmNode node : nodes.values()) {
            node.limitTagLength();
        }
        for (Way way : ways.values()) {
            way.limitTagLength();
        }
        for (Relation relation : relations.values()) {
            relation.limitTagLength();
        }
    }

    /**
     * @return the nodes
     */
    public Map<Integer, OsmNode> getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(Map<Integer, OsmNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the ways
     */
    public Map<Integer, Way> getWays() {
        return ways;
    }

    /**
     * @param ways the ways to set
     */
    public void setWays(Map<Integer, Way> ways) {
        this.ways = ways;
    }

    /**
     * @return the relations
     */
    public Map<Integer, Relation> getRelations() {
        return relations;
    }

    /**
     * @param relations the relations to set
     */
    public void setRelations(Map<Integer, Relation> relations) {
        this.relations = relations;
    }
}
