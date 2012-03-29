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
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
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
            "  -i | --input <input.file>");
        System.out.println(
            "                             specify the input file, required");
        System.out.println(
            "  -b | --base <base.file>");
        System.out.println(
            "                             base file to compare the input to");
        System.out.println(
            "  -t | --idtag <id.tag>");
        System.out.println(
            "                             the tag used to identify each way");
        System.out.println(
            "                             in a unique manner");
        System.out.println(
            "  -o | --output <output.file>");
        System.out.println(
            "                             the output file");
        System.out.println(
            "  -n | --new");
        System.out.println(
            "                             output new content");
        System.out.println(
            "  -c | --changed");
        System.out.println(
            "                             output changed (updated) content");
        System.out.println(
            "  -d | --deleted");
        System.out.println(
            "                             output deleted content");
        System.out.println(
            "  -u | --unchanged");
        System.out.println(
            "                             output unchanged content");
        System.out.println(
            "  -h | --help");
        System.out.println(
            "                             show this usage page");
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
     * Set the changeset attribute in all nodes & ways to null.
     *
     * @param oam the oam to purge the changeset attribute for.
     */
    static void purgeChangeset(Oam oam) {
        for (OsmNode node : oam.getNodes().values()) {
            node.setChangeset(null);
        }
        for (Way way: oam.getWays().values()) {
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

        Map<String, Way> baseWays  = new HashMap<String, Way>();
        Map<String, Way> inputWays = new HashMap<String, Way>();

        mapByTag(baseOam.getWays(), idTag, baseWays);
        mapByTag(inputOam.getWays(), idTag, inputWays);

        // get the unchanged, updated & deleted ways
        for (Map.Entry<String, Way> entry : baseWays.entrySet()) {
            String key     = entry.getKey();
            Way    baseWay = entry.getValue();

            if (inputWays.containsKey(key)) {
                Way way = inputWays.get(key);

                if (entry.getValue().compare(baseOam.getNodes(),
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
                w.setId(-w.getId());

                newOam.getWays().put(w.getId(), w);

                newOam.importWayNodes(w, inputOam.getNodes());
                markNodes(w, newOam.getNodes(), Action.CREATE, true);
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
    static void mapByTag(Map<Integer, Way>      idWayMap,
                         String                 idTag,
                         Map<String, Way>       tagWayMap) {

        for (Way w : idWayMap.values()) {
            if (w.getTags().containsKey(idTag)) {
                tagWayMap.put(w.getTags().get(idTag), w);
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
