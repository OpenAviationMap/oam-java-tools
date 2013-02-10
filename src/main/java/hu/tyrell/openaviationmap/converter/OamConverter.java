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
package hu.tyrell.openaviationmap.converter;

import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.CompoundBoundary;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.Runway;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Member;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmBaseNode;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Relation;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class that converts aviation data into OAM.
 */
public final class OamConverter {
    /**
     * Private default constructor.
     */
    private OamConverter() {
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
        case STD:
            tags.put(prefix + ":class", "std");
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

        Ring r = circle.approximate();

        return processRing(oam, r, nodeIdIx, action, version);
    }

    /**
     * Process a Compund boundary object, by joining the boundaries and creating
     * a single series of nodes.
     *
     * @param oam the OAM object that will contain the nodes
     * @param compoundBoundary the compound boundary to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param action specify this action for each created node
     * @param version the version of the node
     * @return the highest used node id
     */
    private static int processCompoundBoundary(Oam              oam,
                                              CompoundBoundary compoundBoundary,
                                              int              nodeIdIx,
                                              Action           action,
                                              int              version) {

        // first calculate the union of the embedded boundaries
        // using JTS
        GeometryFactory gf = new GeometryFactory();
        Polygon[]       gs = new Polygon[
                                    compoundBoundary.getBoundaryList().size()];
        int             ix = 0;

        for (Boundary b : compoundBoundary.getBoundaryList()) {
            Ring r = null;

            switch (b.getType()) {
            case CIRCLE:
                r = ((Circle) b).approximate();
                break;

            case RING:
                r  = (Ring) b;
            break;

            default:
                // TODO: handle nested compound boundaries
            }

            if (r != null) {
                List<Point>  pl = r.getPointList();

                Coordinate[] cs = new Coordinate[pl.size()];
                int          i  = 0;
                for (Point p : pl) {
                    cs[i++] = new Coordinate(p.getLongitude(),
                                             p.getLatitude());
                }

                gs[ix++] = gf.createPolygon(gf.createLinearRing(cs), null);
            }
        }


        // if no geometries, don't proceed
        if (gs.length == 0) {
            return nodeIdIx;
        }

        // create a union of all the geometries available
        Geometry union = gs[0];
        for (int i = 1; i < gs.length; ++i) {
            union = union.union(gs[i]);
        }

        int                nodeIx = nodeIdIx;
        // as we have the union, export these as points
        // disregard the last one, as it will be the same as the first one
        for (int i = 0; i < union.getCoordinates().length - 1; ++i) {
            Coordinate c = union.getCoordinates()[i];

            OsmNode node = new OsmNode();
            node.setLongitude(c.x);
            node.setLatitude(c.y);
            node.setId(action == Action.CREATE ? -nodeIx : nodeIx);
            node.setVersion(version);
            node.setAction(action);

            oam.getNodes().put(nodeIx++, node);
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

        case COMPOUND:
            return processCompoundBoundary(oam, (CompoundBoundary) boundary,
                                           nodeIdIx, action, version);

        default:
            return nodeIdIx;
        }
    }

    /**
     * Add OAM tags related to elevation limits.
     *
     * @param airspace the airspace the limits are about
     * @param node the OAM node to add the limits to.
     */
    private static void addElevationLimits(Airspace     airspace,
                                           OsmBaseNode  node) {

        if (airspace.getLowerLimit() != null) {
            addElevationTags(airspace.getLowerLimit(),
                             "height:lower",
                             node.getTags());
        }

        if (airspace.getUpperLimit() != null) {
            addElevationTags(airspace.getUpperLimit(),
                             "height:upper",
                             node.getTags());
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

        Boundary b = airspace.getBoundary();

        // if a simple airspace, just create an OAM way
        if (b.getType() == Boundary.Type.RING
         || b.getType() == Boundary.Type.CIRCLE) {
            return airspaceBoundaryToOam(airspace, b,
                                         oam, action, version, nodeIdIx);
        }

        // otherwise, create an OAM relation, with the original ways
        // and a generated union way
        CompoundBoundary cb   = (CompoundBoundary) b;
        ArrayList<Way>   ways = new ArrayList<Way>(cb.getBoundaryList().size());
        int              nodeIx = nodeIdIx;

        // add the original ways
        int i = 1;
        for (Boundary bb : cb.getBoundaryList()) {
            // NOTE: this code implies that the airspaceBoundaryToOam()
            //       call uses the returned node index - 1 to create the way
            //       object
            nodeIx = airspaceBoundaryToOam(airspace, bb,
                                           oam, action, version, nodeIx);
            int wayIx = action == Action.CREATE ? -(nodeIx - 1) : (nodeIx - 1);
            Way                 w    = oam.getWays().get(wayIx);
            Map<String, String> tags = w.getTags();
            // mark this way as not to be rendered
            tags.put("compound", "original");
            // make the name of the way unique
            if (tags.containsKey("name")) {
                tags.put("name", tags.get("name") + " part " + i + " of "
                         + cb.getBoundaryList().size());
            }

            ways.add(w);
            ++i;
        }

        // add the unionized way based on parts of the compound boundary
        // NOTE: this code implies that the airspaceBoundaryToOam()
        //       call uses the returned node index - 1 to create the way
        //       object
        nodeIx = airspaceBoundaryToOam(airspace, cb,
                                       oam, action, version, nodeIx);
        int wayIx = action == Action.CREATE ? -(nodeIx - 1) : (nodeIx - 1);
        Way cbWay = oam.getWays().get(wayIx);
        cbWay.getTags().put("compound", "union");

        // now create the relation that combines these ways
        Relation rel = new Relation();

        rel.setId(action == Action.CREATE ? -nodeIx : nodeIx);
        rel.setVersion(version);
        addAirspaceTags(airspace, rel);

        for (Way w : ways) {
            rel.getMembers().add(
                            new Member(Member.Type.WAY, w.getId(), "airspace"));
        }
        rel.getMembers().add(
                        new Member(Member.Type.WAY, cbWay.getId(), "airspace"));

        oam.getRelations().put(rel.getId(), rel);

        return nodeIx + 1;
    }

    /**
     * Convert an airspace and a boundary into an OAM object.
     *
     * @param airspace the airspace to convert
     * @param boundary the boundary to convert. if this is a compound boundary,
     *        a union will be created and this generated boundary rendered
     * @param oam the Oam object to put the airspaces into
     * @param action the OAM action to set for each object
     * @param version the version to set of reach object
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     * @return the highest used node id during the process
     */
    private static int
    airspaceBoundaryToOam(Airspace airspace,
                          Boundary boundary,
                          Oam      oam,
                          Action   action,
                          int      version,
                          int      nodeIdIx) {
        // create a node for each point in the airspace
        int nIdIx  = nodeIdIx;
        int minIx  = nIdIx;
        int nodeIx = processBoundary(oam, boundary,
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
        addAirspaceTags(airspace, way);

        // store the original info about a circle definition
        if (boundary.getType() == Boundary.Type.CIRCLE) {
            Circle c = (Circle) boundary;

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
     * Add tags to an OSM node which related to airspaces.
     *
     * @param airspace the airspace to get the info from
     * @param node the node to add the tags to
     */
    private static void addAirspaceTags(Airspace airspace, OsmBaseNode node) {
        node.getTags().put("airspace", "yes");

        if (airspace.getDesignator() != null
         && !airspace.getDesignator().isEmpty()) {

            node.getTags().put("icao", airspace.getDesignator());
        }

        if (airspace.getName() != null && !airspace.getName().isEmpty()) {

            node.getTags().put("name", airspace.getName());
        }

        if (airspace.getRemarks() != null
         && !airspace.getRemarks().isEmpty()) {

            node.getTags().put("remarks", airspace.getRemarks());
        }

        if (airspace.getOperator() != null
         && !airspace.getOperator().isEmpty()) {

            node.getTags().put("operator", airspace.getOperator());
        }

        if (airspace.getActiveTime() != null
         && !airspace.getActiveTime().isEmpty()) {

            node.getTags().put("activetime", airspace.getActiveTime());
        }

        if (airspace.getType() != null) {

            node.getTags().put("airspace:type", airspace.getType());
        }

        if (airspace.getAirspaceClass() != null) {

            node.getTags().put("airspace:class",
                              airspace.getAirspaceClass());
        }

        if (airspace.getCommFrequency() != null) {

            node.getTags().put("comm:ctrl",
                              airspace.getCommFrequency());
        }

        addElevationLimits(airspace, node);
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

            case VOT:
                node.getTags().put("navaid:type", "VOT");
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

            case LOC:
                node.getTags().put("navaid:type", "LOC");
                break;

            case GP:
                node.getTags().put("navaid:type", "GP");
                break;

            case MARKER:
                node.getTags().put("navaid:type", "MARKER");
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

                case VOT:
                    node.getTags().put("navaid:vot",
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

                case LOC:
                    node.getTags().put("navaid:loc",
                                       navaid.getFrequency().toString());
                    break;

                case GP:
                    node.getTags().put("navaid:gp",
                                       navaid.getFrequency().toString());
                    break;

                case MARKER:
                    node.getTags().put("navaid:marker",
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
                node.getTags().put("remarks", navaid.getRemarks());
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
            for (Airspace as : ad.getAirspaces()) {
                nIdIx = airspaceToOam(as, oam, action,
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
            arpNode.getTags().put("arp", "yes");
            if (ad.getIcao() != null) {
                arpNode.getTags().put("icao", ad.getIcao());
            }
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
            if (ad.getApron() != null) {
                adRel.getTags().put("comm:apron", ad.getApron().toString());
            }
            if (ad.getAfis() != null) {
                adRel.getTags().put("comm:afis", ad.getAfis().toString());
            }
            if (ad.getAtis() != null) {
                adRel.getTags().put("comm:atis", ad.getAtis().toString());
            }
            if (ad.getApproach() != null) {
                adRel.getTags().put("comm:approach",
                                    ad.getApproach().toString());
            }

            oam.getRelations().put(adRel.getId(), adRel);

            ++nIdIx;
        }
    }
}
