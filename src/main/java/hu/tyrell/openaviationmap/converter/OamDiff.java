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
package hu.tyrell.openaviationmap.converter;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Member;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Relation;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * Utility program to generate a diff based on OAM / OSM files.
 */
public final class OamDiff {
    /**
     * Private default constructor.
     */
    private OamDiff() {
    }

    /**
     * Print a help message.
     */
    private static void printHelpMessage() {
        System.out.println(
            "Open Aviation Map diff utility");
        System.out.println();
        System.out.println(
            "usage:");
        System.out.println();
        System.out.println(
            "  -i | --input <input.file>    specify the input file, required");
        System.out.println(
            "  -b | --base <base.file>      base file to compare the input to");
        System.out.println(
            "  -t | --idtag <id.tag>        the tag used to identify each way");
        System.out.println(
            "                               in a unique manner");
        System.out.println(
            "  -o | --output <output.file>  the output file");
        System.out.println(
            "  -n | --new                   output new content");
        System.out.println(
            "  -c | --changed               output changed (updated) content");
        System.out.println(
            "  -d | --deleted               output deleted content");
        System.out.println(
            "  -u | --unchanged             output unchanged content");
        System.out.println(
            "  -h | --help                  show this usage page");
        System.out.println();
        System.out.println(
            "at least one of --output, --new, --changed, --deleted or"
           + " --unchanged required");
    }

    /**
     * Program entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        LongOpt[] longopts = new LongOpt[9];

        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("input", LongOpt.REQUIRED_ARGUMENT,
                null, 'i');
        longopts[2] = new LongOpt("base", LongOpt.REQUIRED_ARGUMENT,
                null, 'b');
        longopts[3] = new LongOpt("idtag", LongOpt.REQUIRED_ARGUMENT,
                null, 't');
        longopts[4] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                null, 'o');
        longopts[5] = new LongOpt("new", LongOpt.NO_ARGUMENT, null, 'n');
        longopts[6] = new LongOpt("changed", LongOpt.NO_ARGUMENT, null, 'c');
        longopts[7] = new LongOpt("deleted", LongOpt.NO_ARGUMENT, null, 'd');
        longopts[8] = new LongOpt("unchanged", LongOpt.NO_ARGUMENT, null, 'u');

        Getopt g = new Getopt("OamDiff", args, "hi:b:t:o:ncdu", longopts);

        int c;

        String                  inputFile    = null;
        String                  baseFile     = null;
        String                  idTag        = null;
        String                  outputFile   = null;
        boolean                 newContent       = false;
        boolean                 changedContent   = false;
        boolean                 deletedContent   = false;
        boolean                 unchangedContent = false;
        List<ParseException>    errors       = new Vector<ParseException>();

        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'i':
                inputFile = g.getOptarg();
                break;

            case 'b':
                baseFile = g.getOptarg();
                break;

            case 't':
                idTag = g.getOptarg();
                break;

            case 'o':
                outputFile = g.getOptarg();
                break;

            case 'n':
                newContent = true;
                break;

            case 'c':
                changedContent = true;
                break;

            case 'd':
                deletedContent = true;
                break;

            case 'u':
                unchangedContent = true;
                break;

            default:
            case 'h':
                printHelpMessage();
                return;

            case '?':
                System.out.println("Invalid option '" + g.getOptopt()
                                   + "' specified");
                return;
            }
        }

        if (inputFile == null) {
            System.out.println("Required option input not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (baseFile == null) {
            System.out.println("Required option base not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (idTag == null) {
            System.out.println("Required option idtag not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (outputFile == null) {
            System.out.println("Required option output not specified");
            System.out.println();
            printHelpMessage();
            return;
        }

        if (!newContent && !deletedContent && !changedContent
         && !unchangedContent) {
            System.out.println(
                    "Specify at least one of --new, --changed, --deleted"
                  + "or --unchanged");
            System.out.println();
            printHelpMessage();
            return;
        }

        try {
            diff(inputFile,
                 baseFile,
                 idTag,
                 outputFile,
                 newContent,
                 changedContent,
                 deletedContent,
                 unchangedContent,
                 errors);
        } catch (Exception e) {
            System.out.println("Diff failed.");
            System.out.println();
            e.printStackTrace(System.out);
            return;
        }

        if (!errors.isEmpty()) {
            System.out.println("The following errors were encountered:");
            for (ParseException e : errors) {
                System.out.println(e.toString());
            }
            System.out.println();
        }

        System.out.println("Diff successful");
    }

    /**
     * Mark the nodes specified by a certain way with a specified tag.
     * Invert the id of the nodes if needed.
     *
     * @param way the way to manage the nodes for
     * @param nodes a map of nodes, keyed by ids
     * @param action the action to specify for each node
     * @param negative if true, make sure all ids are negatives, otherwise
     *        make sure they are positive
     */
    static void markNodes(Way                       way,
                          Map<Integer, OsmNode>     nodes,
                          Action                    action,
                          boolean                   negative) {

        for (int ix = 0; ix < way.getNodeList().size(); ++ix) {

            int i = way.getNodeList().get(ix);

            if (nodes.containsKey(i)) {
                OsmNode n = nodes.get(i);

                n.setAction(action);

                if ((negative && i >= 0) || (!negative && i < 0)) {
                    // reverse the node id
                    nodes.remove(i);
                    n.setId(-i);
                    nodes.put(-i, n);
                    way.getNodeList().set(ix, -i);
                } else {
                    nodes.put(i, n);
                }
            } else if ((ix == way.getNodeList().size() - 1)
                  &&  ((negative && i >= 0) || (!negative && i < 0))) {
                // this is the last node reference, which is the same as
                // the first one, and the node id has been turned negative
                // already. thus, just update the node reference
                way.getNodeList().set(ix, -i);
            }
        }
    }

    /**
     * Mark the nodes specified by a certain relation with a
     * specified tag. Invert the id of the nodes if needed.
     *
     * @param relation the relation to manage the nodes for
     * @param nodes a map of nodes, keyed by ids
     * @param action the action to specify for each node
     * @param negative if true, make sure all ids are negatives, otherwise
     *        make sure they are positive
     */
    static void markNodes(Relation                  relation,
                          Map<Integer, OsmNode>     nodes,
                          Action                    action,
                          boolean                   negative) {

        for (int ix = 0; ix < relation.getMembers().size(); ++ix) {

            Member m = relation.getMembers().get(ix);
            int    i = m.getRef();

            if (m.getType() == Member.Type.NODE && nodes.containsKey(i)) {
                OsmNode n = nodes.get(i);

                n.setAction(action);

                if ((negative && i >= 0) || (!negative && i < 0)) {
                    // reverse the node id
                    nodes.remove(i);
                    n.setId(-i);
                    nodes.put(-i, n);
                    m.setRef(-i);
                } else {
                    nodes.put(i, n);
                }
            }
        }
    }

    /**
     * Mark the ways specified by a certain relation with a
     * specified tag. Invert the id of the nodes if needed.
     *
     * @param relation the relation to manage the nodes for
     * @param nodes a map of nodes, keyed by ids
     * @param ways a map of ways, keyed by ids
     * @param action the action to specify for each node
     * @param negative if true, make sure all ids are negatives, otherwise
     *        make sure they are positive
     */
    static void markWays(Relation                  relation,
                         Map<Integer, OsmNode>     nodes,
                         Map<Integer, Way>         ways,
                         Action                    action,
                         boolean                   negative) {

        for (int ix = 0; ix < relation.getMembers().size(); ++ix) {

            Member m = relation.getMembers().get(ix);
            int    i = m.getRef();

            if (m.getType() == Member.Type.WAY &&ways.containsKey(i)) {
                Way w = ways.get(i);

                w.setAction(action);

                if ((negative && i >= 0) || (!negative && i < 0)) {
                    // reverse the node id
                    ways.remove(i);
                    w.setId(-i);
                    ways.put(-i, w);
                    m.setRef(-i);
                } else {
                    ways.put(i, w);
                }

                markNodes(w, nodes, action, negative);
            }
        }
    }

    /**
     * Set the changeset attribute in all nodes & ways to null.
     *
     * @param oam the oam to purge the changeset attribute for.
     */
    static void purgeChangeset(Oam oam) {
        for (OsmNode node : oam.getNodes().values()) {
            node.setChangeset(null);
        }
        for (Way way : oam.getWays().values()) {
            way.setChangeset(null);
        }
    }

    /**
     * Compare a baseline OAM to another, 'input' OAM, and output the
     * differences between them. The results will contain ids that correspond
     * to the elements in the base OAM document, except for the newOam set.
     * Use the supplied id tag to uniquely identify elements in the OAMs.
     * Nodes that don't have this property set are simply ignored.
     * Note: currently only 'way' elements are handled properly.
     *
     * @param baseOam the baseline to compare against
     * @param inputOam the input to compare the baseline to
     * @param idTag the OSM tag used to identify nodes or ways uniquely
     * @param newOam an Oam containing content that is not in the baseline,
     *        but is present in the input OAM
     * @param deletedOam an Oam containing content that is in the baseline, but
     *        not in the input
     * @param updatedOam an Oam containing contant that is present both in the
     *        baseline and in the input, but has changed
     * @param unchangedOam an Oam containing content that is both present in
     *        the baseline and the input, and is in fact the same.
     */
    static void compareOams(Oam     baseOam,
                            Oam     inputOam,
                            String  idTag,
                            Oam     newOam,
                            Oam     deletedOam,
                            Oam     updatedOam,
                            Oam     unchangedOam) {

        compareNodes(baseOam, inputOam, idTag,
                newOam, deletedOam, updatedOam, unchangedOam);

        compareWays(baseOam, inputOam, idTag,
                newOam, deletedOam, updatedOam, unchangedOam);

        compareRelations(baseOam, inputOam, idTag,
                newOam, deletedOam, updatedOam, unchangedOam);
    }

    /**
     * Compare a baseline OAM to another, 'input' OAM, and output the
     * differences between them. The results will contain ids that correspond
     * to the elements in the base OAM document, except for the newOam set.
     * Use the supplied id tag to uniquely identify elements in the OAMs.
     * Nodes that don't have this property set are simply ignored.
     *
     * This function compares 'node' elements.
     *
     * @param baseOam the baseline to compare against
     * @param inputOam the input to compare the baseline to
     * @param idTag the OSM tag used to identify nodes or ways uniquely
     * @param newOam an Oam containing content that is not in the baseline,
     *        but is present in the input OAM
     * @param deletedOam an Oam containing content that is in the baseline, but
     *        not in the input
     * @param updatedOam an Oam containing contant that is present both in the
     *        baseline and in the input, but has changed
     * @param unchangedOam an Oam containing content that is both present in
     *        the baseline and the input, and is in fact the same.
     */
    static void compareNodes(Oam     baseOam,
                             Oam     inputOam,
                             String  idTag,
                             Oam     newOam,
                             Oam     deletedOam,
                             Oam     updatedOam,
                             Oam     unchangedOam) {

        Map<String, OsmNode> baseNodes  = new HashMap<String, OsmNode>();
        Map<String, OsmNode> inputNodes = new HashMap<String, OsmNode>();

        mapByTagNode(baseOam.getNodes(), idTag, baseNodes);
        mapByTagNode(inputOam.getNodes(), idTag, inputNodes);

        // get the unchanged, updated & deleted ways
        for (Map.Entry<String, OsmNode> entry : baseNodes.entrySet()) {
            String   key      = entry.getKey();
            OsmNode  baseNode = entry.getValue();

            if (inputNodes.containsKey(key)) {
                OsmNode node = inputNodes.get(key);

                if (baseNode.compare(node)) {
                    // the entry exists in both collections and hasn't changed
                    unchangedOam.getNodes().put(baseNode.getId(), baseNode);
                } else {
                    // the entry exists in both collections, and changed
                    OsmNode n = new OsmNode(node);
                    n.setId(baseNode.getId());
                    n.setVersion(baseNode.getVersion());
                    n.setAction(Action.MODIFY);

                    updatedOam.getNodes().put(n.getId(), n);
                }
            } else {
                // the entry doesn't exist in the new collection anymore
                OsmNode n = new OsmNode(baseNode);
                n.setAction(Action.DELETE);

                // make sure all ids are positive
                if (n.getId() < 0) {
                    n.setId(-n.getId());
                }

                deletedOam.getNodes().put(n.getId(), n);
            }
        }

        // get the new nodes
        for (Map.Entry<String, OsmNode> entry : inputNodes.entrySet()) {
            String key = entry.getKey();

            if (!baseNodes.containsKey(key)) {
                OsmNode n = new OsmNode(entry.getValue());
                n.setAction(Action.CREATE);
                if (n.getId() > 0) {
                    n.setId(-n.getId());
                }

                newOam.getNodes().put(n.getId(), n);
            }
        }
    }

    /**
     * Compare a baseline OAM to another, 'input' OAM, and output the
     * differences between them. The results will contain ids that correspond
     * to the elements in the base OAM document, except for the newOam set.
     * Use the supplied id tag to uniquely identify elements in the OAMs.
     * Nodes that don't have this property set are simply ignored.
     *
     * This function compares 'way' elements.
     *
     * @param baseOam the baseline to compare against
     * @param inputOam the input to compare the baseline to
     * @param idTag the OSM tag used to identify nodes or ways uniquely
     * @param newOam an Oam containing content that is not in the baseline,
     *        but is present in the input OAM
     * @param deletedOam an Oam containing content that is in the baseline, but
     *        not in the input
     * @param updatedOam an Oam containing contant that is present both in the
     *        baseline and in the input, but has changed
     * @param unchangedOam an Oam containing content that is both present in
     *        the baseline and the input, and is in fact the same.
     */
    static void compareWays(Oam     baseOam,
                            Oam     inputOam,
                            String  idTag,
                            Oam     newOam,
                            Oam     deletedOam,
                            Oam     updatedOam,
                            Oam     unchangedOam) {

        Map<String, Way> baseWays  = new HashMap<String, Way>();
        Map<String, Way> inputWays = new HashMap<String, Way>();

        mapByTagWay(baseOam.getWays(), idTag, baseWays);
        mapByTagWay(inputOam.getWays(), idTag, inputWays);

        // get the unchanged, updated & deleted ways
        for (Map.Entry<String, Way> entry : baseWays.entrySet()) {
            String key     = entry.getKey();
            Way    baseWay = entry.getValue();

            if (inputWays.containsKey(key)) {
                Way way = inputWays.get(key);

                if (baseWay.compare(baseOam.getNodes(),
                                    way,
                                    inputOam.getNodes())) {
                    // the entry exists in both collections and hasn't changed
                    unchangedOam.importWayNodes(baseWay,
                                                baseOam.getNodes());
                    unchangedOam.getWays().put(baseWay.getId(), baseWay);
                } else {
                    // the entry exists in both collections, and changed
                    Way w = new Way(way);
                    w.setId(baseWay.getId());
                    w.setVersion(baseWay.getVersion());
                    w.setAction(Action.MODIFY);

                    updatedOam.getWays().put(w.getId(), w);

                    // import the baseline nodes, and mark them all as deleted
                    updatedOam.importWayNodes(baseWay,
                                              baseOam.getNodes());
                    markNodes(baseWay, updatedOam.getNodes(),
                              Action.DELETE, false);

                    // import the new nodes, and mark them all as modified
                    updatedOam.importWayNodes(w, inputOam.getNodes());
                    markNodes(w, updatedOam.getNodes(), Action.MODIFY, true);
                }
            } else {
                // the entry doesn't exist in the new collection anymore
                Way w = new Way(baseWay);
                w.setAction(Action.DELETE);

                deletedOam.importWayNodes(w, baseOam.getNodes());
                markNodes(w, deletedOam.getNodes(), Action.DELETE, false);

                // make sure all ids are positive
                if (w.getId() < 0) {
                    w.setId(-w.getId());
                }
                for (int i = 0; i < w.getNodeList().size(); ++i) {
                    int j = w.getNodeList().get(i);
                    if (j < 0) {
                        w.getNodeList().set(i,  -j);
                    }
                }

                deletedOam.getWays().put(w.getId(), w);
            }
        }

        // get the new ways
        for (Map.Entry<String, Way> entry : inputWays.entrySet()) {
            String key = entry.getKey();

            if (!baseWays.containsKey(key)) {
                Way w = new Way(entry.getValue());
                w.setAction(Action.CREATE);
                if (w.getId() > 0) {
                    w.setId(-w.getId());
                }

                newOam.getWays().put(w.getId(), w);

                newOam.importWayNodes(w, inputOam.getNodes());
                markNodes(w, newOam.getNodes(), Action.CREATE, true);
            }
        }
    }

    /**
     * Compare a baseline OAM to another, 'input' OAM, and output the
     * differences between them. The results will contain ids that correspond
     * to the elements in the base OAM document, except for the newOam set.
     * Use the supplied id tag to uniquely identify elements in the OAMs.
     * Nodes that don't have this property set are simply ignored.
     *
     * This function compares 'relation' elements.
     *
     * @param baseOam the baseline to compare against
     * @param inputOam the input to compare the baseline to
     * @param idTag the OSM tag used to identify nodes or ways uniquely
     * @param newOam an Oam containing content that is not in the baseline,
     *        but is present in the input OAM
     * @param deletedOam an Oam containing content that is in the baseline, but
     *        not in the input
     * @param updatedOam an Oam containing contant that is present both in the
     *        baseline and in the input, but has changed
     * @param unchangedOam an Oam containing content that is both present in
     *        the baseline and the input, and is in fact the same.
     */
    static void compareRelations(Oam     baseOam,
                                 Oam     inputOam,
                                 String  idTag,
                                 Oam     newOam,
                                 Oam     deletedOam,
                                 Oam     updatedOam,
                                 Oam     unchangedOam) {

        Map<String, Relation> baseRels  = new HashMap<String, Relation>();
        Map<String, Relation> inputRels = new HashMap<String, Relation>();

        mapByTagRel(baseOam.getRelations(), idTag, baseRels);
        mapByTagRel(inputOam.getRelations(), idTag, inputRels);

        // get the unchanged, updated & deleted relations
        for (Map.Entry<String, Relation> entry : baseRels.entrySet()) {
            String      key     = entry.getKey();
            Relation    baseRel = entry.getValue();

            if (inputRels.containsKey(key)) {
                Relation rel = inputRels.get(key);

                if (baseRel.compare(baseOam.getNodes(), baseOam.getWays(),
                                    rel,
                                    inputOam.getNodes(), inputOam.getWays())) {
                    // the entry exists in both collections and hasn't changed
                    unchangedOam.importRelNodesWays(baseRel,
                                                    baseOam.getNodes(),
                                                    baseOam.getWays());
                    unchangedOam.getRelations().put(baseRel.getId(), baseRel);
                } else {
                    // the entry exists in both collections, and changed
                    Relation r = new Relation(rel);
                    r.setId(baseRel.getId());
                    r.setVersion(baseRel.getVersion());
                    r.setAction(Action.MODIFY);

                    updatedOam.getRelations().put(r.getId(), r);

                    // import the baseline nodes, and mark them all as deleted
                    updatedOam.importRelNodesWays(baseRel,
                                                  baseOam.getNodes(),
                                                  baseOam.getWays());
                    markNodes(baseRel, updatedOam.getNodes(),
                              Action.DELETE, false);
                    markWays(baseRel, updatedOam.getNodes(),
                             updatedOam.getWays(), Action.DELETE, false);

                    // import the new nodes, and mark them all as modified
                    updatedOam.importRelNodesWays(r,
                                                  inputOam.getNodes(),
                                                  inputOam.getWays());
                    markNodes(r, updatedOam.getNodes(), Action.MODIFY, true);
                    markWays(r, updatedOam.getNodes(), updatedOam.getWays(),
                             Action.MODIFY, true);
                }
            } else {
                // the entry doesn't exist in the new collection anymore
                Relation r = new Relation(baseRel);
                r.setAction(Action.DELETE);

                deletedOam.importRelNodesWays(r,
                                              baseOam.getNodes(),
                                              baseOam.getWays());
                markNodes(r, deletedOam.getNodes(), Action.DELETE, false);
                markWays(r, deletedOam.getNodes(), deletedOam.getWays(),
                         Action.DELETE, false);

                // make sure all ids are positive
                if (r.getId() < 0) {
                    r.setId(-r.getId());
                }
                for (int i = 0; i < r.getMembers().size(); ++i) {
                    Member m = r.getMembers().get(i);
                    int    j = m.getRef();
                    if (j < 0) {
                        m.setRef(-j);
                    }
                }

                deletedOam.getRelations().put(r.getId(), r);
            }
        }

        // get the new relations
        for (Map.Entry<String, Relation> entry : inputRels.entrySet()) {
            String key = entry.getKey();

            if (!baseRels.containsKey(key)) {
                Relation r = new Relation(entry.getValue());
                r.setAction(Action.CREATE);
                if (r.getId() > 0) {
                    r.setId(-r.getId());
                }

                newOam.getRelations().put(r.getId(), r);

                newOam.importRelNodesWays(r,
                                          inputOam.getNodes(),
                                          inputOam.getWays());
                markNodes(r, newOam.getNodes(), Action.CREATE, true);
                markWays(r, newOam.getNodes(), newOam.getWays(),
                         Action.CREATE, true);
            }
        }
    }

    /**
     * Create a map of OsmNode objects, based on a specific tag in node objects.
     *
     * @param idNodeMap a map of node ids and nodes
     * @param idTag the name of the tag to use as an id in the generated map
     * @param tagNodeMap a map with keys as the id values specified by idTag
     *                  that will contain the results
     */
    static void mapByTagNode(Map<Integer, OsmNode> idNodeMap,
                            String                 idTag,
                            Map<String, OsmNode>   tagNodeMap) {

        for (OsmNode n : idNodeMap.values()) {
            if (n.getTags().containsKey(idTag)) {
                tagNodeMap.put(n.getTags().get(idTag), n);
            }
        }
    }

    /**
     * Create a map of Way objects, based on a specific tag in way objects.
     *
     * @param idWayMap a map of Way ids and Ways
     * @param idTag the name of the tag to use as an id in the generated map
     * @param tagWayMap a map with keys as the id values specified by idTag
     *                  that will contain the results
     */
    static void mapByTagWay(Map<Integer, Way>      idWayMap,
                            String                 idTag,
                            Map<String, Way>       tagWayMap) {

        for (Way w : idWayMap.values()) {
            if (w.getTags().containsKey(idTag)) {
                tagWayMap.put(w.getTags().get(idTag), w);
            }
        }
    }

    /**
     * Create a map of Relation objects, based on a specific tag in the
     * relation objects.
     *
     * @param idRelMap a map of relation ids and relations
     * @param idTag the name of the tag to use as an id in the generated map
     * @param tagRelMap a map with keys as the id values specified by idTag
     *                  that will contain the results
     */
    static void mapByTagRel(Map<Integer, Relation>      idRelMap,
                            String                      idTag,
                            Map<String, Relation>       tagRelMap) {

        for (Relation r : idRelMap.values()) {
            if (r.getTags().containsKey(idTag)) {
                tagRelMap.put(r.getTags().get(idTag), r);
            }
        }
    }

    /**
     * Perform the diff itself.
     *
     * @param inputFile the input file name
     * @param baseFile the base file name
     * @param idTag the id of the tag that uniquely identifies each way
     * @param outputFile the output file name
     * @param newContent output new content in the output file
     * @param changedContent output changed content in the output file
     * @param deletedContent output deleted content in the output file
     * @param unchangedContent output unchanged content in the output file
     * @param errors all parsing errors will be put into this list
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    static void diff(String                 inputFile,
                     String                 baseFile,
                     String                 idTag,
                     String                 outputFile,
                     boolean                newContent,
                     boolean                changedContent,
                     boolean                deletedContent,
                     boolean                unchangedContent,
                     List<ParseException>   errors)
                                     throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        Oam     baseOam      = OAMReader.loadOam(baseFile, errors);
        Oam     inputOam     = OAMReader.loadOam(inputFile, errors);
        Oam     newOam       = new Oam();
        Oam     deletedOam   = new Oam();
        Oam     changedOam   = new Oam();
        Oam     unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            idTag,
                            newOam,
                            deletedOam,
                            changedOam,
                            unchangedOam);

        Oam output = new Oam();
        if (newContent) {
            output.merge(newOam);
        }
        if (changedContent) {
            output.merge(changedOam);
        }
        if (deletedContent) {
            output.merge(deletedOam);
        }
        if (unchangedContent) {
            output.merge(unchangedOam);
        }

        purgeChangeset(output);

        OAMWriter.write(output, new FileWriter(outputFile));
    }
}
