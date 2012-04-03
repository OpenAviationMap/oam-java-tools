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
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorAd13;
import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.Runway;
import hu.tyrell.openaviationmap.model.UOM;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Member;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Relation;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A command line tool to convert between various formats.
 */
public final class Converter {
    /**
     * Private default constructor.
     */
    private Converter() {
    }

    /**
     * Print a help message.
     */
    private static void printHelpMessage() {
        System.out.println(
            "Open Aviation Map converter utility");
        System.out.println("");
        System.out.println(
            "usage:");
        System.out.println("");
        System.out.println(
            "  -i | --input <input.file>");
        System.out.println(
            "                             specify the input file, required");
        System.out.println(
            "  -f | --input-format <input.format>");
        System.out.println(
            "                             specify the input format, required");
        System.out.println(
            "                             supported formats: eAIP.Hungary");
        System.out.println(
            "  -o | --output <output.file>");
        System.out.println(
            "                             specify the output file, required");
        System.out.println(
            "  -F | --output-format <output.format>");
        System.out.println(
            "                             specify the output format, required");
        System.out.println(
            "                             supported formats: OAM");
        System.out.println(
            "  -c | --create");
        System.out.println(
            "                             if specified, the OAM output file");
        System.out.println(
            "                             is created in 'create' mode, for");
        System.out.println(
            "                             adding new OAM nodes & ways");
        System.out.println(
                "  -b | --border");
        System.out.println(
                "                         a border polygon file in OSM format");
        System.out.println(
                "                         to be used for airspaces that refer");
        System.out.println(
                "                         to national borders. optional");
        System.out.println(
                "  -a | --aerodromes");
        System.out.println(
                "                         an aerodrome index file, in the");
        System.out.println(
                "                         eAIP section AD 1.3 format. used");
        System.out.println(
                "                         when processing aerodromes from");
        System.out.println(
                "                         eAIP. optional.");
        System.out.println(
            "  -v | --version");
        System.out.println(
            "                             specify the OAM node versions");
        System.out.println(
            "                             required if output format is OAM");
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
        longopts[2] = new LongOpt("input-format", LongOpt.REQUIRED_ARGUMENT,
                null, 'f');
        longopts[3] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                null, 'o');
        longopts[4] = new LongOpt("output-format", LongOpt.REQUIRED_ARGUMENT,
                null, 'F');
        longopts[5] = new LongOpt("create", LongOpt.NO_ARGUMENT,
                null, 'c');
        longopts[6] = new LongOpt("border", LongOpt.REQUIRED_ARGUMENT,
                null, 'b');
        longopts[7] = new LongOpt("aerodromes", LongOpt.REQUIRED_ARGUMENT,
                null, 'a');
        longopts[8] = new LongOpt("version", LongOpt.REQUIRED_ARGUMENT,
                null, 'v');

        Getopt g = new Getopt("Converter", args, "hi:f:o:F:cb:a:v:", longopts);

        int c;

        String                  inputFile    = null;
        String                  inputFormat  = null;
        String                  outputFile   = null;
        String                  outputFormat = null;
        boolean                 create       = false;
        String                  borderFile   = null;
        String                  adFile       = null;
        int                     version      = 0;
        List<ParseException>    errors       = new Vector<ParseException>();

        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'i':
                inputFile = g.getOptarg();
                break;

            case 'f':
                inputFormat = g.getOptarg();
                break;

            case 'o':
                outputFile = g.getOptarg();
                break;

            case 'F':
                outputFormat = g.getOptarg();
                break;

            case 'c':
                create = true;
                break;

            case 'b':
                borderFile = g.getOptarg();
                break;

            case 'a':
                adFile = g.getOptarg();
                break;

            case 'v':
                version = Integer.parseInt(g.getOptarg());
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
        if (inputFormat == null) {
            System.out.println("Required option input-format not specified");
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
        if (outputFormat == null) {
            System.out.println("Required option output-format not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (version <= 0) {
            System.out.println("Version not specified as positive ingeger");
            System.out.println();
            printHelpMessage();
            return;
        }

        try {
            convert(inputFile,
                    inputFormat,
                    outputFile,
                    outputFormat,
                    create ? Action.CREATE : Action.NONE,
                    borderFile,
                    adFile,
                    version,
                    errors);
        } catch (Exception e) {
            System.out.println("Conversion failed.");
            System.out.println();
            System.out.println("error details: " + e.toString());
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

        System.out.println("Conversion successful");
    }

    /**
     * Add all tags that describe an elevation. This involves three tags,
     * the "prefix" tag with the elevation, the "prefix:unit" tag with
     * the unit if measurement, and the "prefix:reference" tag with the
     * reference (e.g. AGL or AMSL).
     *
     * @param elevation the elevation to create the tags for.
     * @param prefix the prefix to use for the tag names
     * @param tags the tag map to put the tags into.
     */
    private static void addElevationTags(Elevation              elevation,
                                         String                 prefix,
                                         Map<String, String>    tags) {

        tags.put(prefix, Double.toString(elevation.getElevation()));
        tags.put(prefix + ":unit",
                elevation.getUom().toString().toLowerCase());

        switch (elevation.getReference()) {
        default:
        case MSL:
            tags.put(prefix + ":class", "amsl");
            break;
        case SFC:
            tags.put(prefix + ":class", "agl");
            break;
        }
    }

    /**
     * Process a Circle boundary object, by turning it into a series of node
     * elements.
     *
     * @param oam the OAM object that will contain the nodes
     * @param circle the circle to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param action specify this action for each created node
     * @param version the version of the node
     * @return the highest used node id
     */
    private static int processCircle(Oam                 oam,
                                     Circle              circle,
                                     int                 nodeIdIx,
                                     Action              action,
                                     int                 version) {

        int nodeIx = nodeIdIx;

        double radiusInNm  = circle.getRadius().inUom(UOM.NM).getDistance();
        double radiusInDeg = radiusInNm / 60.0;
        double radiusLat   = radiusInDeg;
        double radiusLon   = radiusInDeg / Math.cos(
                              Math.toRadians(circle.getCenter().getLatitude()));

        // FIXME: calculate number of points on some required precision metric
        int totalPoints = 32;
        double tpHalf = totalPoints / 2.0;
        for (int i = 0; i < totalPoints; ++i, ++nodeIx) {
            double theta = Math.PI * i / tpHalf;
            double x = circle.getCenter().getLongitude()
                    + (radiusLon * Math.cos(theta));
            double y = circle.getCenter().getLatitude()
                    + (radiusLat * Math.sin(theta));

            OsmNode node = new OsmNode();
            node.setLongitude(x);
            node.setLatitude(y);
            node.setId(action == Action.CREATE ? -nodeIx : nodeIx);
            node.setVersion(version);
            node.setAction(action);

            oam.getNodes().put(nodeIx, node);
        }

        return nodeIx;
    }

    /**
     * Process a Ring boundary object, by turning it into a series of node
     * elements.
     *
     * @param oam the OAM object that will contain the nodes
     * @param ring the ring to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param action specify this action for each created node
     * @param version the version of the node
     * @return the highest used node id
     */
    private static int processRing(Oam                 oam,
                                   Ring                ring,
                                   int                 nodeIdIx,
                                   Action              action,
                                   int                 version) {

        int nodeIx = nodeIdIx;

        // omit the last node, as it will be a duplicate of the first one
        for (int i = 0; i < ring.getPointList().size() - 1; ++i, ++nodeIx) {
            Point point = ring.getPointList().get(i);

            OsmNode node = new OsmNode();

            node.setLatitude(point.getLatitude());
            node.setLongitude(point.getLongitude());
            node.setId(action == Action.CREATE ? -nodeIx : nodeIx);
            node.setVersion(version);
            node.setAction(action);

            oam.getNodes().put(nodeIx, node);
        }

        return nodeIx;
    }

    /**
     * Process a Boundary object, by turning it into a series of node
     * elements.
     *
     * @param oam the OAM object that will contain the nodes
     * @param boundary the boundary to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param action specify this action for each created node
     * @param version the version of the node
     * @return the highest used node id
     */
    private static int processBoundary(Oam                 oam,
                                       Boundary            boundary,
                                       int                 nodeIdIx,
                                       Action              action,
                                       int                 version) {

        switch (boundary.getType()) {
        case RING:
            return processRing(oam, (Ring) boundary,
                               nodeIdIx, action, version);

        case CIRCLE:
            return processCircle(oam, (Circle) boundary,
                                 nodeIdIx, action, version);

        default:
            return nodeIdIx;
        }
    }

    /**
     * Add OAM tags related to elevation limits.
     *
     * @param airspace the airspace the limits are about
     * @param way the OAM way to add the limits to.
     */
    private static void addElevationLimits(Airspace airspace,
                                           Way      way) {

        if (airspace.getLowerLimit() != null) {
            addElevationTags(airspace.getLowerLimit(),
                             "height:lower",
                             way.getTags());
        }

        if (airspace.getUpperLimit() != null) {
            addElevationTags(airspace.getUpperLimit(),
                             "height:upper",
                             way.getTags());
        }
    }

    /**
     * Convert an Airspace list to an Oam object.
     *
     * @param airspaces the airspaces to convert
     * @param oam the Oam object to put the airspaces into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     */
    public static void airspacesToOam(List<Airspace> airspaces,
                                      Oam            oam,
                                      Action         action,
                                      int            version,
                                      int            nodeIdIx) {

        int nIdIx = nodeIdIx;

        for (Airspace airspace : airspaces) {
            nIdIx = airspaceToOam(airspace, oam, action, version, nIdIx);
        }
    }

    /**
     * Convert an airspace into an OAM object.
     *
     * @param airspace the airspace to convert
     * @param oam the Oam object to put the airspaces into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     * @return the highest used node id during the process
     */
    private static int airspaceToOam(Airspace airspace,
                                     Oam      oam,
                                     Action   action,
                                     int      version,
                                     int      nodeIdIx) {
        // create a node for each point in the airspace
        int nIdIx = nodeIdIx;
        int minIx  = nIdIx;
        int nodeIx = processBoundary(oam, airspace.getBoundary(),
                                     nIdIx, action, version);

        // create a 'way' object for the airspace itself
        Way way = new Way();

        way.setId(action == Action.CREATE ? -nodeIx : nodeIx);
        way.setVersion(version);
        way.setAction(action);

        // insert the node references
        if (action == Action.CREATE) {
            for (int i = minIx; i < nodeIx; ++i) {
                way.getNodeList().add(-i);
            }
            way.getNodeList().add(-minIx);
        } else {
            for (int i = minIx; i < nodeIx; ++i) {
                way.getNodeList().add(i);
            }
            way.getNodeList().add(minIx);
        }

        nIdIx = nodeIx;

        // insert the airspace metadata
        way.getTags().put("airspace", "yes");

        if (airspace.getDesignator() != null
         && !airspace.getDesignator().isEmpty()) {

            way.getTags().put("icao", airspace.getDesignator());
        }

        if (airspace.getName() != null && !airspace.getName().isEmpty()) {

            way.getTags().put("name", airspace.getName());
        }

        if (airspace.getRemarks() != null
         && !airspace.getRemarks().isEmpty()) {

            way.getTags().put("remarks", airspace.getRemarks());
        }

        if (airspace.getOperator() != null
         && !airspace.getOperator().isEmpty()) {

            way.getTags().put("operator", airspace.getOperator());
        }

        if (airspace.getActiveTime() != null
         && !airspace.getActiveTime().isEmpty()) {

            way.getTags().put("activetime", airspace.getActiveTime());
        }

        if (airspace.getType() != null) {

            way.getTags().put("airspace:type", airspace.getType());
        }

        if (airspace.getAirspaceClass() != null) {

            way.getTags().put("airspace:class",
                              airspace.getAirspaceClass());
        }

        if (airspace.getCommFrequency() != null) {

            way.getTags().put("comm:ctrl",
                              airspace.getCommFrequency());
        }

        addElevationLimits(airspace, way);

        if (airspace.getBoundary().getType() == Boundary.Type.CIRCLE) {
            Circle c = (Circle) airspace.getBoundary();

            way.getTags().put("airspace:center:lat",
                    Double.toString(c.getCenter().getLatitude()));
            way.getTags().put("airspace:center:lon",
                    Double.toString(c.getCenter().getLongitude()));
            way.getTags().put("airspace:center:radius",
                    Double.toString(c.getRadius().getDistance()));
            way.getTags().put("airspace:center:unit",
                    c.getRadius().getUom().toString());
        }

        oam.getWays().put(way.getId(), way);

        return nIdIx + 1;
    }

    /**
     * Convert a Navaid list to an Oam object.
     *
     * @param navaids the navaids to convert
     * @param oam the Oam object to put the navaids into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     * @return the maximum node id used, in absolute value
     */
    public static int navaidsToOam(List<Navaid>   navaids,
                                   Oam            oam,
                                   Action         action,
                                   int            version,
                                   int            nodeIdIx) {

        int nIdIx = nodeIdIx;

        for (Navaid navaid : navaids) {

            OsmNode node = new OsmNode();

            node.setLatitude(navaid.getLatitude());
            node.setLongitude(navaid.getLongitude());
            node.setId(action == Action.CREATE ? -nIdIx : nIdIx);
            node.setVersion(version);
            node.setAction(action);

            nIdIx++;

            // insert the airspace metadata
            node.getTags().put("navaid", "yes");

            if (navaid.getId() != null) {
                node.getTags().put("id", navaid.getId());
            }

            if (navaid.getName() != null) {
                node.getTags().put("name", navaid.getName());
            }

            if (navaid.getIdent() != null) {
                node.getTags().put("navaid:ident", navaid.getIdent());
            }

            switch (navaid.getType()) {
            case VOR:
                node.getTags().put("navaid:type", "VOR");
                break;

            case VORDME:
                node.getTags().put("navaid:type", "VOR/DME");
                break;

            case DME:
                node.getTags().put("navaid:type", "DME");
                break;

            case NDB:
                node.getTags().put("navaid:type", "NDB");
                break;

            case DESIGNATED:
                node.getTags().put("navaid:type", "DESIGNATED");
                break;

            default:
            }

            if (navaid.getDeclination() != 0.0) {
                node.getTags().put("navaid:declination",
                                   Double.toString(navaid.getDeclination()));
            }

            if (navaid.getVariation() != null) {
                node.getTags().put("navaid:variation",
                        Double.toString(navaid.getVariation().getVariation()));
                node.getTags().put("navaid:variation:year",
                        Integer.toString(navaid.getVariation().getYear()));
            }

            if (navaid.getFrequency() != null) {
                switch (navaid.getType()) {
                case VOR:
                    node.getTags().put("navaid:vor",
                                        navaid.getFrequency().toString());
                    break;

                case VORDME:
                    node.getTags().put("navaid:vor",
                                       navaid.getFrequency().toString());
                    break;

                case DME:
                    break;

                case NDB:
                    node.getTags().put("navaid:ndb",
                                       navaid.getFrequency().toString());
                    break;

                default:
                }
            }

            if (navaid.getDmeChannel() != null) {
                node.getTags().put("navaid:dme", navaid.getDmeChannel());
            }

            if (navaid.getActivetime() != null) {
                node.getTags().put("navaid:activetime", navaid.getActivetime());
            }

            if (navaid.getElevation() != null) {
                addElevationTags(navaid.getElevation(),
                                 "height",
                                 node.getTags());
            }

            if (navaid.getCoverage() != null) {
                node.getTags().put("navaid:coverage",
                        Double.toString(navaid.getCoverage().getDistance()));
                node.getTags().put("navaid:coverage:unit",
                        navaid.getCoverage().getUom().toString());
            }

            if (navaid.getRemarks() != null) {
                node.getTags().put("navaid:remarks", navaid.getRemarks());
            }


            oam.getNodes().put(node.getId(), node);
        }

        return nIdIx;
    }

    /**
     * Convert a Runway to an Oam Way.
     *
     * @param runway the runway to process.
     * @param oam the Oam object to put the aerodromes into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     * @return one above the highest node index used during creation
     */
    public static int processRunway(Runway          runway,
                                    Oam             oam,
                                    Action          action,
                                    int             version,
                                    int             nodeIdIx) {
        int nIdIx = nodeIdIx;

        // create the two endpoint nodes of the runway
        OsmNode threshold = new OsmNode();
        threshold.setLatitude(runway.getThreshold().getLatitude());
        threshold.setLongitude(runway.getThreshold().getLongitude());
        threshold.setId(action == Action.CREATE ? -nIdIx : nIdIx);
        threshold.setAction(action);
        threshold.setVersion(version);

        ++nIdIx;
        OsmNode end = new OsmNode();
        end.setLatitude(runway.getEnd().getLatitude());
        end.setLongitude(runway.getEnd().getLongitude());
        end.setId(action == Action.CREATE ? -nIdIx : nIdIx);
        end.setAction(action);
        end.setVersion(version);

        oam.getNodes().put(threshold.getId(), threshold);
        oam.getNodes().put(end.getId(), end);

        ++nIdIx;
        Way w = new Way();
        w.setId(action == Action.CREATE ? -nIdIx : nIdIx);
        w.setAction(action);
        w.setVersion(version);
        w.getNodeList().add(threshold.getId());
        w.getNodeList().add(end.getId());

        w.getTags().put("aeroway", "runway");

        if (runway.getDesignator() != null) {
            w.getTags().put("name", runway.getDesignator());
        }
        if (runway.getWidth() != null) {
            w.getTags().put("width", runway.getWidth().toString());
        }
        if (runway.getLength() != null) {
            w.getTags().put("length", runway.getLength().toString());
        }
        if (runway.getSurface() != null) {
            switch (runway.getSurface()) {
            case ASPHALT:
                w.getTags().put("surface", "asphalt");
                break;

            case GRASS:
                w.getTags().put("surface", "grass");
                break;

            default:
            }

        }
        if (runway.getBearing() != 0.0) {
            w.getTags().put("bearing", Double.toString(runway.getBearing()));
        }
        if (runway.getSlope() != 0.0) {
            w.getTags().put("slope", Double.toString(runway.getSlope()));
        }

        if (runway.getElevation() != null) {
            addElevationTags(runway.getElevation(), "height", w.getTags());
        }

        if (runway.getTora() != null) {
            w.getTags().put("tora", runway.getTora().toString());
        }
        if (runway.getToda() != null) {
            w.getTags().put("toda", runway.getToda().toString());
        }
        if (runway.getAsda() != null) {
            w.getTags().put("asda", runway.getAsda().toString());
        }
        if (runway.getLda() != null) {
            w.getTags().put("lda", runway.getLda().toString());
        }

        oam.getWays().put(w.getId(), w);


        return nIdIx + 1;
    }

    /**
     * Convert an Aerodrome list to an Oam object.
     *
     * @param aerodromes a list of aerodromes to convert
     * @param oam the Oam object to put the aerodromes into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     */
    public static void aerodromesToOam(List<Aerodrome>  aerodromes,
                                       Oam              oam,
                                       Action           action,
                                       int              version,
                                       int              nodeIdIx) {

        int nIdIx = nodeIdIx;

        for (Aerodrome ad : aerodromes) {

            // don't consider aerodromes which don't have an ARP
            if (ad.getArp() == null) {
                continue;
            }

            Relation adRel = new Relation();

            // process runways
            for (Runway rwy : ad.getRunways()) {
                nIdIx = processRunway(rwy, oam, action, version, nIdIx);
                int i = action == Action.CREATE ? -(nIdIx - 1) : (nIdIx - 1);
                adRel.getMembers().add(
                                    new Member(Member.Type.WAY, i, "runway"));
            }

            // process navaids
            int minNavaidIx = nIdIx;
            nIdIx = navaidsToOam(ad.getNavaids(), oam, action, version, nIdIx);
            for (int i = minNavaidIx; i < nIdIx; ++i) {
                int ix = action == Action.CREATE ? -i : i;
                adRel.getMembers().add(
                                    new Member(Member.Type.NODE, ix, "navaid"));
            }


            // process airspaces
            if (ad.getAirspace() != null) {
                nIdIx = airspaceToOam(ad.getAirspace(), oam, action,
                                      version, nIdIx);
                int i = action == Action.CREATE ? -(nIdIx - 1) : (nIdIx - 1);
                adRel.getMembers().add(
                                    new Member(Member.Type.WAY, i, "airspace"));
            }

            // add the ARP node
            OsmNode arpNode = new OsmNode();
            arpNode.setId(action == Action.CREATE ? -nIdIx : nIdIx);
            arpNode.setAction(action);
            arpNode.setVersion(version);
            if (ad.getName() != null) {
                arpNode.getTags().put("name", ad.getName() + " ARP");
            }
            if (ad.getArp() != null) {
                arpNode.setLatitude(ad.getArp().getLatitude());
                arpNode.setLongitude(ad.getArp().getLongitude());
            }
            if (ad.getElevation() != null) {
                addElevationTags(ad.getElevation(), "height",
                                 arpNode.getTags());
            }
            oam.getNodes().put(arpNode.getId(), arpNode);
            adRel.getMembers().add(new Member(Member.Type.NODE,
                                              arpNode.getId(),
                                              "arp"));
            ++nIdIx;

            // create the aerodrome relation
            adRel.setId(action == Action.CREATE ? -nIdIx : nIdIx);
            adRel.setAction(action);
            adRel.setVersion(version);
            adRel.getTags().put("aerodrome", "yes");

            if (ad.getName() != null) {
                adRel.getTags().put("name", ad.getName());
            }
            if (ad.getIcao() != null) {
                adRel.getTags().put("icao", ad.getIcao());
            }
            if (ad.getIata() != null) {
                adRel.getTags().put("iata", ad.getIata());
            }
            if (ad.getRemarks() != null) {
                adRel.getTags().put("remarks", ad.getRemarks());
            }
            if (ad.getElevation() != null) {
                addElevationTags(ad.getElevation(), "height", adRel.getTags());
            }

            if (!ad.getRunways().isEmpty()) {
                String rwyDesignators = "";
                String rwyLengths     = "";
                String rwySurfaces    = "";
                for (Runway rwy : ad.getRunways()) {
                    rwyDesignators += rwy.getDesignator() + ", ";
                    rwyLengths     += rwy.getLength().toString() + ", ";
                    rwySurfaces    += rwy.getSurface().toString() + ", ";
                }

                adRel.getTags().put("runway:designator",
                    rwyDesignators.substring(0, rwyDesignators.length() - 2));
                adRel.getTags().put("runway:length",
                        rwyLengths.substring(0, rwyLengths.length() - 2));
                adRel.getTags().put("runway:surface",
                        rwySurfaces.substring(0, rwySurfaces.length() - 2));
            }

            if (ad.getTower() != null) {
                adRel.getTags().put("comm:twr", ad.getTower().toString());
            }
            if (ad.getGround() != null) {
                adRel.getTags().put("comm:gnd", ad.getGround().toString());
            }
            if (ad.getAfis() != null) {
                adRel.getTags().put("comm:afis", ad.getAfis().toString());
            }

            oam.getRelations().put(adRel.getId(), adRel);

            ++nIdIx;
        }
    }

    /**
     * Perform the conversion itself.
     *
     * @param inputFile the name of the input file
     * @param inputFormat the name of the input format
     * @param outputFile the name of the output file
     * @param outputFormat the name of the output format
     * @param action the action to specify for all OAM elements
     * @param borderFile a file describing the country border to be used
     *        for airspaces that refer to country borders. may be null
     * @param adFile a file containing an aerodrome index, in the eAIP AD 1.3
     *        format. used when processing eAIP Aerodrome definitions.
     *        may be null.
     * @param version the OAM node / way version to be set
     * @param errors all parsing errors will be put into this list
     * @throws Exception on conversion problems.
     */
    public static void convert(String                   inputFile,
                               String                   inputFormat,
                               String                   outputFile,
                               String                   outputFormat,
                               Action                   action,
                               String                   borderFile,
                               String                   adFile,
                               int                      version,
                               List<ParseException>     errors)
                                                           throws Exception {

        List<Airspace>  airspaces    = new Vector<Airspace>();
        List<Navaid>    navaids      = new Vector<Navaid>();
        List<Aerodrome> aerodromes   = new Vector<Aerodrome>();
        List<Point>     borderPoints = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        if (borderFile != null) {
            DocumentBuilder db     = dbf.newDocumentBuilder();
            Document        d      = db.parse(new FileInputStream(borderFile));
            OAMReader       reader = new OAMReader();
            Oam             oam    = new Oam();
            reader.processOsm(d.getDocumentElement(), oam, errors);

            if (!oam.getWays().isEmpty()) {
                // extract the border as a point list
                Way border = oam.getWays().values().iterator().next();

                borderPoints = new Vector<Point>(border.getNodeList().size());
                for (Integer ref : border.getNodeList()) {
                    borderPoints.add(oam.getNodes().get(ref).asPoint());
                }
            }
        }

        if (adFile != null) {
            DocumentBuilder db     = dbf.newDocumentBuilder();
            Document        d      = db.parse(new FileInputStream(adFile));

            if ("e:AD-1.3".equals(d.getDocumentElement().getTagName())) {
                EAipProcessorAd13 p = new EAipProcessorAd13();
                p.processEAIP(d.getDocumentElement(),
                              borderPoints,
                              airspaces,
                              navaids,
                              aerodromes,
                              errors);
            }

        }

        if ("eAIP.Hungary".equals(inputFormat)) {
            Node eAipNode;

            DocumentBuilder db  = dbf.newDocumentBuilder();
            Document   d = db.parse(new FileInputStream(inputFile));
            eAipNode = d.getDocumentElement();

            EAIPHungaryReader reader = new EAIPHungaryReader();
            reader.processEAIP(eAipNode,
                               borderPoints,
                               airspaces,
                               navaids,
                               aerodromes,
                               errors);
        } else {
            throw new Exception("input format " + inputFormat
                              + " not recognized");
        }

        if ("OAM".equals(outputFormat)) {
            // convert the airspaces to OAM
            Oam oam = new Oam();

            airspacesToOam(airspaces, oam, action, version, 0);
            navaidsToOam(navaids, oam, action, version, oam.getMaxNodeId() + 1);
            aerodromesToOam(aerodromes, oam, action, version,
                            oam.getMaxNodeId() + 1);

            OAMWriter.write(oam, new FileWriter(outputFile));

        } else {
            throw new Exception("output format " + outputFormat
                              + " not recognized");
        }
    }

}
