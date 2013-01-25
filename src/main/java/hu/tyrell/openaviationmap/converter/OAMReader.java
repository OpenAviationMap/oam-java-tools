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
import hu.tyrell.openaviationmap.model.Distance;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Frequency;
import hu.tyrell.openaviationmap.model.MagneticVariation;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.Runway;
import hu.tyrell.openaviationmap.model.SurfaceType;
import hu.tyrell.openaviationmap.model.UOM;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Member;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmBaseNode;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Relation;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to read Open Aviation Map (and Open Street Map) files.
 */
public class OAMReader {
    /**
     * The date format used to read timestamps.
     */
    private static DateFormat df =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Process a generic OSM node.
     *
     * @param element the XML node that represents an OSM node
     * @param osmNode the base OSM node to put the information into
     * @throws ParseException on parsing errors
     */
    void processBaseNode(Element element, OsmBaseNode osmNode)
                                                    throws ParseException {

        String idStr  = element.getAttribute("id");
        Integer id  = Integer.parseInt(idStr);

        osmNode.setId(id);

        String  str  = element.getAttribute("version");
        if (str != null && !str.isEmpty()) {
            osmNode.setVersion(Integer.parseInt(str));
        }

        str = element.getAttribute("action");
        if (str != null && !str.isEmpty()) {
            if ("create".equals(str)) {
                osmNode.setAction(Action.CREATE);
            } else if ("modify".equals(str)) {
                osmNode.setAction(Action.CREATE);
            } else if ("delete".equals(str)) {
                osmNode.setAction(Action.DELETE);
            }
        }

        str  = element.getAttribute("timestamp");
        if (str != null && !str.isEmpty()) {
            try {
                osmNode.setTimestamp(df.parse(str));
            } catch (java.text.ParseException e) {
                throw new ParseException(idStr,
                                         "bad timestamp format: '" + str + "'",
                                         e);
            }
        }

        str  = element.getAttribute("uid");
        if (str != null && !str.isEmpty()) {
            osmNode.setUid(Integer.parseInt(str));
        }

        str  = element.getAttribute("user");
        if (str != null && !str.isEmpty()) {
            osmNode.setUser(str);
        }

        str  = element.getAttribute("visible");
        if (str != null && !str.isEmpty()) {
            osmNode.setVisible("true".equals(str));
        }

        str  = element.getAttribute("changeset");
        if (str != null && !str.isEmpty()) {
            osmNode.setChangeset(Integer.parseInt(str));
        }

        // process the tags
        try {
            XPath    xpath = XPathFactory.newInstance().newXPath();
            NodeList n = (NodeList) xpath.evaluate("tag", element,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processTag((Element) n.item(i), osmNode);
            }
        } catch (XPathExpressionException e) {
            throw new ParseException(e);
        }
    }


    /**
     * Process a 'node' element in an OAM file.
     *
     * @param node the XML node that represents an OSM 'node' element
     * @param nodes a map that contains the processed nodes, with the nodes
     *        id as the key
     * @throws ParseException on parsing errors
     */
    void processNode(Element node, Map<Integer, OsmNode> nodes)
                                                    throws ParseException {
        if (!"node".equals(node.getTagName())) {
            return;
        }

        OsmNode osmNode = new OsmNode();

        processBaseNode(node,  osmNode);

        String latStr = node.getAttribute("lat");
        String lonStr = node.getAttribute("lon");

        osmNode.setLatitude(Double.parseDouble(latStr));
        osmNode.setLongitude(Double.parseDouble(lonStr));

        nodes.put(osmNode.getId(), osmNode);
    }

    /**
     * Process all 'node' elements that are the descendants of an OSM / OAM
     * element.
     *
     * @param parent the OSM / OAM root node element
     * @param nodes the nodes map to contain the process nodes, with the node
     *        id as key.
     * @throws ParseException on parsing issues.
     */
    void processNodes(Element parent, Map<Integer, OsmNode> nodes)
                                                        throws ParseException {
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            // get the 'node' elements
            NodeList n = (NodeList) xpath.evaluate("//node", parent,
                                                   XPathConstants.NODESET);

            for (int i = 0; i < n.getLength(); ++i) {
                processNode((Element) n.item(i), nodes);
            }

        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    /**
     * Process a 'tag' element, which is part of a 'node' element.
     *
     * @param node the XML 'tag' element
     * @param osmNode the 'node' to add this tag to.
     */
    void processTag(Element node, OsmBaseNode osmNode) {
        if (!"tag".equals(node.getTagName())) {
            return;
        }

        String key   = node.getAttribute("k");
        String value = node.getAttribute("v");

        osmNode.getTags().put(key, value);
    }

    /**
     * Process an 'nd' element, which is part of a 'way' element.
     *
     * @param node the XML 'nd' element
     * @param points a map of existing points, into which this node reference
     *        points by an id
     * @param way the 'way' to add this tag to.
     * @throws ParseException on document parsing errors
     */
    void processNodeRef(Element               node,
                        Map<Integer, OsmNode> points,
                        Way                   way)      throws ParseException {
        if (!"nd".equals(node.getTagName())) {
            return;
        }

        String  refStr = node.getAttribute("ref");
        Integer ref    = Integer.parseInt(refStr);

        if (!points.containsKey(ref)) {
            throw new ParseException("node reference points to nonexistent "
                                   + "node: " + refStr);
        }

        way.getNodeList().add(ref);
    }

    /**
     * Process am OSM / OAM 'way' node.
     *
     * @param node an XML 'way' node
     * @param points a map of OAM 'nodes', with the key being the nodes id
     * @param ways a map of OAM 'ways', with the key being the ways id
     * @throws ParseException on document parsing errors
     */
    void processWay(Element               node,
                    Map<Integer, OsmNode> points,
                    Map<Integer, Way>     ways)       throws ParseException {
        if (!"way".equals(node.getTagName())) {
            return;
        }

        Way way = new Way();

        processBaseNode(node, way);

        // get the all 'nd' (node reference) elements
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            NodeList n = (NodeList) xpath.evaluate("nd", node,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processNodeRef((Element) n.item(i), points, way);
            }

        } catch (Exception e) {
            throw new ParseException(e);
        }

        ways.put(way.getId(), way);
    }

    /**
     * Process an 'member' element, which is part of a 'relation' element.
     *
     * @param node the XML 'member' element
     * @param points a map of OAM 'nodes', with the key being the nodes id
     * @param ways a map of OAM 'ways', with the key being the nodes id
     * @param relation the 'relation' to add this tag to.
     * @throws ParseException on document parsing errors
     */
    void processMember(Element               node,
                       Map<Integer, OsmNode> points,
                       Map<Integer, Way>     ways,
                       Relation              relation)  throws ParseException {
        if (!"member".equals(node.getTagName())) {
            return;
        }

        String  refStr = node.getAttribute("ref");
        Integer ref    = Integer.parseInt(refStr);

        String roleStr = node.getAttribute("role");

        Member.Type type      = null;
        String      typeStr   = node.getAttribute("type");
        if ("node".equals(typeStr)) {
            type = Member.Type.NODE;

            if (!points.containsKey(ref)) {
                throw new ParseException("node reference points to nonexistent "
                                       + "node: " + refStr);
            }
        } else if ("way".equals(typeStr)) {
            type = Member.Type.WAY;

            if (!ways.containsKey(ref)) {
                throw new ParseException("way reference points to nonexistent "
                                       + "way: " + refStr);
            }
        }

        relation.getMembers().add(new Member(type, ref, roleStr));
    }

    /**
     * Process am OSM / OAM 'relation' node.
     *
     * @param node an XML 'relation' node
     * @param points a map of OAM 'nodes', with the key being the nodes id
     * @param ways a map of OAM 'ways', with the key being the nodes id
     * @param relations a map of OAM 'relations', with the key being the ways id
     * @throws ParseException on document parsing errors
     */
    void processRelation(Element                 node,
                         Map<Integer, OsmNode>   points,
                         Map<Integer, Way>       ways,
                         Map<Integer, Relation>  relations)
                                                         throws ParseException {
        if (!"relation".equals(node.getTagName())) {
            return;
        }

        Relation rel = new Relation();

        processBaseNode(node, rel);

        // get the all 'member' (node reference) elements
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            NodeList n = (NodeList) xpath.evaluate("member", node,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processMember((Element) n.item(i), points, ways, rel);
            }

        } catch (Exception e) {
            throw new ParseException(e);
        }

        relations.put(rel.getId(), rel);
    }

    /**
     * Process an OAM / OSM document and generate a number of Way elements.
     *
     * @param root the document root element to process
     * @param oam the OAM element to put the results into
     * @param errors all parsing errors will be put into this list
     */
    public void processOsm(Element              root,
                           Oam                  oam,
                           List<ParseException> errors) {
        if (!"osm".equals(root.getTagName())) {
            return;
        }

        try {
            processNodes(root, oam.getNodes());

            XPath          xpath     = XPathFactory.newInstance().newXPath();

            // get the 'way' elements
            NodeList n = (NodeList) xpath.evaluate("//way", root,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processWay((Element) n.item(i), oam.getNodes(), oam.getWays());
            }

            // get the 'relation' elements
            n = (NodeList) xpath.evaluate("//relation", root,
                                          XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processRelation((Element) n.item(i),
                                oam.getNodes(), oam.getWays(),
                                oam.getRelations());
            }

        } catch (ParseException e) {
            errors.add(e);
        } catch (Exception e) {
            errors.add(new ParseException(e));
        }
    }

    /**
     * Convert an OAM 'way' element into a Runway object.
     *
     * @param way the OAM 'way' to convert
     * @param nodeList nodes referred to by the nodeList member of the
     *                 supplied way
     * @return the runway corresponding to the supplied 'way' element.
     * @throws ParseException on parsing errors
     */
    Runway wayToRunway(Way                   way,
                       Map<Integer, OsmNode> nodeList)
                                                       throws ParseException {
        Map<String, String> tags = way.getTags();

        if (!tags.containsKey("aeroway")
         || !"runway".equals(tags.get("aeroway"))) {

            throw new ParseException("way is not a runway");
        }

        Runway rwy = new Runway();
        String k;

        k = "name";
        if (tags.containsKey(k)) {
            rwy.setDesignator(tags.get(k));
        }

        k = "bearing";
        if (tags.containsKey(k)) {
            rwy.setBearing(Double.parseDouble(tags.get(k)));
        }

        k = "slope";
        if (tags.containsKey(k)) {
            rwy.setSlope(Double.parseDouble(tags.get(k)));
        }

        k = "length";
        if (tags.containsKey(k)) {
            rwy.setLength(Distance.fromString(tags.get(k)));
        }

        k = "width";
        if (tags.containsKey(k)) {
            rwy.setWidth(Distance.fromString(tags.get(k)));
        }

        k = "tora";
        if (tags.containsKey(k)) {
            rwy.setTora(Distance.fromString(tags.get(k)));
        }

        k = "toda";
        if (tags.containsKey(k)) {
            rwy.setToda(Distance.fromString(tags.get(k)));
        }

        k = "asda";
        if (tags.containsKey(k)) {
            rwy.setAsda(Distance.fromString(tags.get(k)));
        }

        k = "lda";
        if (tags.containsKey(k)) {
            rwy.setLda(Distance.fromString(tags.get(k)));
        }

        k = "surface";
        if (tags.containsKey(k)) {
            String v = tags.get(k);
            if ("asphalt".equals(v)) {
                rwy.setSurface(SurfaceType.ASPHALT);
            } else if ("grass".equals(v)) {
                rwy.setSurface(SurfaceType.GRASS);
            } else {
                throw new ParseException(rwy.getDesignator(),
                                         "unrecognized sufrace type " + v);
            }
        }

        if (tags.containsKey("height")
         && tags.containsKey("height:unit")
         && tags.containsKey("height:class")) {

            Elevation e = new Elevation();

            e.setElevation(Double.parseDouble(tags.get("height")));
            e.setUom(UOM.fromString(tags.get("height:unit")));
            e.setReference(ElevationReference.fromString(
                                          tags.get("height:class")));

            rwy.setElevation(e);
        }

        if (way.getNodeList().size() != 2) {
            throw new ParseException(rwy.getDesignator(),
                                     "runway with incorrect number of nodes: "
                                    + way.getNodeList().size());
        }

        int ix = way.getNodeList().get(0);
        if (!nodeList.containsKey(ix)) {
            throw new ParseException(rwy.getDesignator(),
                                     "node reference does not exist: " + ix);

        }
        rwy.setThreshold(nodeList.get(ix).asPoint());
        nodeList.remove(ix);

        ix = way.getNodeList().get(1);
        if (!nodeList.containsKey(ix)) {
            throw new ParseException(rwy.getDesignator(),
                                     "node reference does not exist: " + ix);

        }
        rwy.setEnd(nodeList.get(ix).asPoint());
        nodeList.remove(ix);


        return rwy;
    }

    /**
     * Convert an OAM 'relation' element into an Aerodrome object.
     *
     * @param relation the OAM 'relation' to convert
     * @param oam the OAM object that contains nodes related to the aerodrome,
     *        e.g. airspaces, navaids
     * @return the airspace corresponding to the supplied 'way' element.
     * @throws ParseException on parsing errors
     */
    Aerodrome relationToAerodrome(Relation   relation,
                                  Oam        oam)        throws ParseException {

        Map<String, String> tags = relation.getTags();

        if (!tags.containsKey("aerodrome")
         || !"yes".equals(tags.get("aerodrome"))) {

            throw new ParseException("relation is not an aerodrome");
        }

        Aerodrome ad = new Aerodrome();
        String   k;

        k = "icao";
        if (tags.containsKey(k)) {
            ad.setIcao(tags.get(k));
        }

        k = "name";
        if (tags.containsKey(k)) {
            ad.setName(tags.get(k));
        }

        k = "remarks";
        if (tags.containsKey(k)) {
            ad.setRemarks(tags.get(k));
        }

        k = "comm:afis";
        if (tags.containsKey(k)) {
            ad.setAfis(Frequency.fromString(tags.get(k)));
        }

        k = "comm:twr";
        if (tags.containsKey(k)) {
            ad.setTower(Frequency.fromString(tags.get(k)));
        }

        k = "comm:apron";
        if (tags.containsKey(k)) {
            ad.setApron(Frequency.fromString(tags.get(k)));
        }

        k = "comm:atis";
        if (tags.containsKey(k)) {
            ad.setAtis(Frequency.fromString(tags.get(k)));
        }

        k = "comm:approach";
        if (tags.containsKey(k)) {
            ad.setApproach(Frequency.fromString(tags.get(k)));
        }

        if (tags.containsKey("height")
         && tags.containsKey("height:unit")
         && tags.containsKey("height:class")) {

            Elevation e = new Elevation();

            e.setElevation(Double.parseDouble(tags.get("height")));
            e.setUom(UOM.fromString(tags.get("height:unit")));
            e.setReference(ElevationReference.fromString(
                                          tags.get("height:class")));

            ad.setElevation(e);
        }

        for (Member m : relation.getMembers()) {
            if ("arp".equals(m.getRole())) {
                if (Member.Type.NODE != m.getType()) {
                    throw new ParseException(ad.getIcao(),
                                        "arp member is not of node type");
                }
                if (!oam.getNodes().containsKey(m.getRef())) {
                    throw new ParseException(ad.getIcao(),
                                    "arp node does not exist: " + m.getRef());
                }

                ad.setArp(oam.getNodes().get(m.getRef()).asPoint());
                oam.getNodes().remove(m.getRef());

            } else if ("airspace".equals(m.getRole())) {
                if (Member.Type.WAY != m.getType()) {
                    throw new ParseException(ad.getIcao(),
                                        "airspace member is not of way type");
                }
                if (!oam.getWays().containsKey(m.getRef())) {
                    throw new ParseException(ad.getIcao(),
                                "airspace way does not exist: " + m.getRef());
                }

                Way apWay = oam.getWays().get(m.getRef());
                ad.getAirspaces().add(wayToAirspace(apWay, oam.getNodes()));
                for (int i : apWay.getNodeList()) {
                    oam.getNodes().remove(i);
                }
                oam.getWays().remove(m.getRef());

            } else if ("navaid".equals(m.getRole())) {
                if (Member.Type.NODE != m.getType()) {
                    throw new ParseException(ad.getIcao(),
                                        "navaid member is not of node type");
                }
                if (!oam.getNodes().containsKey(m.getRef())) {
                    throw new ParseException(ad.getIcao(),
                                "navaid node does not exist: " + m.getRef());
                }

                Navaid navaid = nodeToNavaid(oam.getNodes().get(m.getRef()));
                ad.getNavaids().add(navaid);
                oam.getNodes().remove(m.getRef());

            } else if ("runway".equals(m.getRole())) {
                if (Member.Type.WAY != m.getType()) {
                    throw new ParseException(ad.getIcao(),
                                        "runway member is not of way type");
                }
                if (!oam.getWays().containsKey(m.getRef())) {
                    throw new ParseException(ad.getIcao(),
                                "runway way does not exist: " + m.getRef());
                }

                Way    way = oam.getWays().get(m.getRef());
                Runway rwy = wayToRunway(way, oam.getNodes());
                ad.getRunways().add(rwy);
                for (int i : way.getNodeList()) {
                    oam.getNodes().remove(i);
                }
                oam.getWays().remove(m.getRef());

            } else {
                throw new ParseException(ad.getIcao(),
                                       "unrecognized relation " + m.getRole());
            }
        }

        return ad;
    }

    /**
     * Set the elevation limits on an airspace based on the tags of a 'way'
     * OAM element.
     *
     * @param tags the tags of a 'way' element
     * @param airspace the airspace to set the elevation limits for.
     */
    private void setElevationLimits(Map<String, String> tags,
                                    Airspace            airspace) {
        if (tags.containsKey("height:lower")
         && tags.containsKey("height:lower:unit")
         && tags.containsKey("height:lower:class")) {

            Elevation e = new Elevation();

            e.setElevation(Double.parseDouble(tags.get("height:lower")));
            e.setUom(UOM.fromString(tags.get("height:lower:unit")));
            e.setReference(ElevationReference.fromString(
                                           tags.get("height:lower:class")));

            airspace.setLowerLimit(e);
        }

        if (tags.containsKey("height:upper")
         && tags.containsKey("height:upper:unit")
         && tags.containsKey("height:upper:class")) {

            Elevation e = new Elevation();

            e.setElevation(Double.parseDouble(tags.get("height:upper")));
            e.setUom(UOM.fromString(tags.get("height:upper:unit")));
            e.setReference(ElevationReference.fromString(
                                             tags.get("height:upper:class")));

            airspace.setUpperLimit(e);
        }
    }

    /**
     * Convert an OAM 'way' element into an Airspace object.
     *
     * @param way the OAM 'way' to convert
     * @param nodeList nodes referred to by the nodeList member of the
     *                 supplied way
     * @return the airspace corresponding to the supplied 'way' element.
     * @throws ParseException on parsing errors
     */
    Airspace wayToAirspace(Way                   way,
                           Map<Integer, OsmNode> nodeList)
                                                       throws ParseException {
        Map<String, String> tags = way.getTags();

        if (!tags.containsKey("airspace")
         || !"yes".equals(tags.get("airspace"))) {

            throw new ParseException("way is not an airspace");
        }

        Airspace airspace = new Airspace();

        processAirspaceTags(tags, airspace);

        if (tags.containsKey("airspace:center:lat")
         && tags.containsKey("airspace:center:lon")
         && tags.containsKey("airspace:center:radius")
         && tags.containsKey("airspace:center:unit")) {

            // this is a circle airspace
            double lat    = Double.parseDouble(tags.get("airspace:center:lat"));
            double lon    = Double.parseDouble(tags.get("airspace:center:lon"));
            double rDist  = Double.parseDouble(
                                            tags.get("airspace:center:radius"));
            String unit   = tags.get("airspace:center:unit");

            Point center = new Point();
            center.setLatitude(lat);
            center.setLongitude(lon);

            Distance radius = new Distance();
            radius.setDistance(rDist);
            radius.setUom(UOM.fromString(unit));

            Circle c = new Circle();
            c.setCenter(center);
            c.setRadius(radius);

            airspace.setBoundary(c);
        } else {
            // this is a polygon based airspace
            // convert the list of OsmNodes to a list of Points
            List<Point> pl = new Vector<Point>(way.getNodeList().size());

            for (Integer ref : way.getNodeList()) {
                if (!nodeList.containsKey(ref)) {
                    throw new ParseException(airspace.getDesignator(),
                                             "bad node reference: " + ref);

                }
                pl.add(nodeList.get(ref).asPoint());
            }

            Ring r = new Ring();
            r.setPointList(pl);

            airspace.setBoundary(r);
        }

        return airspace;
    }

    /**
     * Convert an OAM 'relation' element into an Airspace object, with a
     * compound boundary.
     *
     * @param relation the OAM 'relation' to convert
     * @param oam the oam object that contains the relation
     * @return the airspace corresponding to the supplied 'way' element.
     * @throws ParseException on parsing errors
     */
    Airspace relationToAirspace(Relation    relation,
                                Oam         oam)
                                                       throws ParseException {
        Map<String, String> tags = relation.getTags();

        if (!tags.containsKey("airspace")
         || !"yes".equals(tags.get("airspace"))) {

            throw new ParseException("relation is not an airspace");
        }

        Airspace airspace = new Airspace();

        processAirspaceTags(tags, airspace);

        // get the parts of the airspace from the relation
        ArrayList<Boundary> bl =
                    new ArrayList<Boundary>(relation.getMembers().size() - 1);

        for (Member m : relation.getMembers()) {
            if (m.getType() == Member.Type.WAY
             && "airspace".equals(m.getRole())) {

                Way w = oam.getWays().get(m.getRef());

                if (w.getTags().containsKey("compound")
                 && "original".equals(w.getTags().get("compound"))) {

                    // this is a bit redundant, as it will process the
                    // tags as well, which we'll throw away
                    Airspace as = wayToAirspace(w, oam.getNodes());
                    bl.add(as.getBoundary());
                }
            }
        }
        CompoundBoundary cb = new CompoundBoundary();
        cb.setBoundaryList(bl);

        airspace.setBoundary(cb);

        return airspace;
    }

    /**
     * Process tags of an OAM node and add them to an Airspace object.
     *
     * @param tags the tags to process
     * @param airspace the airspace to add the tag contents to
     */
    private void processAirspaceTags(Map<String, String> tags,
                                     Airspace            airspace) {
        String   k;

        k = "icao";
        if (tags.containsKey(k)) {
            airspace.setDesignator(tags.get(k));
        }

        k = "name";
        if (tags.containsKey(k)) {
            airspace.setName(tags.get(k));
        }

        k = "remarks";
        if (tags.containsKey(k)) {
            airspace.setRemarks(tags.get(k));
        }

        k = "operator";
        if (tags.containsKey(k)) {
            airspace.setOperator(tags.get(k));
        }

        k = "activetime";
        if (tags.containsKey(k)) {
            airspace.setActiveTime(tags.get(k));
        }

        k = "comm:ctrl";
        if (tags.containsKey(k)) {
            airspace.setCommFrequency(tags.get(k));
        }

        k = "airspace:type";
        if (tags.containsKey(k)) {
            airspace.setType(tags.get(k));
        }

        k = "airspace:class";
        if (tags.containsKey(k)) {
            airspace.setAirspaceClass(tags.get(k));
        }

        setElevationLimits(tags, airspace);
    }

    /**
     * Convert an OAM 'node' element into a Navaid object.
     *
     * @param node the OAM 'node' to convert
     * @return the navaid corresponding to the supplied 'node' element.
     * @throws ParseException on parsing errors
     */
    Navaid nodeToNavaid(OsmNode node) throws ParseException {

        Map<String, String> tags = node.getTags();

        if (!tags.containsKey("navaid")
         || !"yes".equals(tags.get("navaid"))) {

            throw new ParseException("node is not a navaid");
        }

        Navaid navaid = new Navaid();

        navaid.setLatitude(node.getLatitude());
        navaid.setLongitude(node.getLongitude());

        String k;

        k = "id";
        if (tags.containsKey(k)) {
            navaid.setId(tags.get(k));
        }

        k = "navaid:type";
        if (tags.containsKey(k)) {
            String s = tags.get(k);
            if ("VOR".equals(s)) {
                navaid.setType(Navaid.Type.VOR);
            } else if ("VOT".equals(s)) {
                navaid.setType(Navaid.Type.VOT);
            } else if ("VOR/DME".equals(s)) {
                navaid.setType(Navaid.Type.VORDME);
            } else if ("DME".equals(s)) {
                navaid.setType(Navaid.Type.DME);
            } else if ("NDB".equals(s) || "L".equals(s)) {
                navaid.setType(Navaid.Type.NDB);
            } else if ("LOC".equals(s)) {
                navaid.setType(Navaid.Type.LOC);
            } else if ("GP".equals(s)) {
                navaid.setType(Navaid.Type.GP);
            } else if ("MARKER".equals(s)) {
                navaid.setType(Navaid.Type.MARKER);
            } else if ("DESIGNATED".equals(s)) {
                navaid.setType(Navaid.Type.DESIGNATED);
            }
        }

        k = "name";
        if (tags.containsKey(k)) {
            navaid.setName(tags.get(k));
        }

        k = "navaid:ident";
        if (tags.containsKey(k)) {
            navaid.setIdent(tags.get(k));
        }

        k = "navaid:declination";
        if (tags.containsKey(k)) {
            navaid.setDeclination(Double.parseDouble(tags.get(k)));
        }

        k = "navaid:variation";
        if (tags.containsKey(k)) {
            double variation = Double.parseDouble(tags.get(k));
            int    year      = 0;
            k = "navaid:variation:year";
            if (tags.containsKey(k)) {
                year = Integer.parseInt(tags.get(k));
            }
            navaid.setVariation(new MagneticVariation(variation, year));
        }

        k = "navaid:vor";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:vot";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:ndb";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:loc";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:gp";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:marker";
        if (tags.containsKey(k)) {
            navaid.setFrequency(Frequency.fromString(tags.get(k)));
        }

        k = "navaid:dme";
        if (tags.containsKey(k)) {
            navaid.setDmeChannel(tags.get(k));
        }

        k = "navaid:activetime";
        if (tags.containsKey(k)) {
            navaid.setActivetime(tags.get(k));
        }

        if (tags.containsKey("height")
         && tags.containsKey("height:unit")
         && tags.containsKey("height:class")) {

           Elevation e = new Elevation();

           e.setElevation(Double.parseDouble(tags.get("height")));
           e.setUom(UOM.fromString(tags.get("height:unit")));
           e.setReference(ElevationReference.fromString(
                                          tags.get("height:class")));

           navaid.setElevation(e);
       }

        if (tags.containsKey("navaid:coverage")
         && tags.containsKey("navaid:coverage:unit")) {
            Distance coverage = new Distance();

            coverage.setDistance(Double.parseDouble(
                                                tags.get("navaid:coverage")));
            coverage.setUom(UOM.fromString(tags.get("navaid:coverage:unit")));

            navaid.setCoverage(coverage);
        }

        k = "remarks";
        if (tags.containsKey(k)) {
            navaid.setRemarks(tags.get(k));
        }


        return navaid;
    }

    /**
     * Process an OAM document and generate a number of Airspace elements.
     *
     * @param root the document root element to process
     * @param airspaces a list of Airspace elements, which will contain
     *        the airspaces contained on the OAM document.
     * @param navaids a list of navaid elemets, which will contain the
     *        navaids contained in the OAM document.
     * @param aerodromes a list of Aerodrome objects, which will contain
     *        the aerodromes described by the OAM document
     * @param errors all parsing errors will be put into this list
     */
    public void processOam(Element                  root,
                           List<Airspace>           airspaces,
                           List<Navaid>             navaids,
                           List<Aerodrome>          aerodromes,
                           List<ParseException>     errors) {
        Oam oam = new Oam();
        processOsm(root, oam, errors);

        for (Relation relation : oam.getRelations().values()) {
            if (relation.getTags().containsKey("aerodrome")) {
                try {
                    Aerodrome ad = relationToAerodrome(relation, oam);
                    aerodromes.add(ad);
                } catch (ParseException e) {
                    errors.add(e);
                }
            } else if (relation.getTags().containsKey("airspace")) {
                try {
                    Airspace as = relationToAirspace(relation, oam);
                    airspaces.add(as);
                } catch (ParseException e) {
                    errors.add(e);
                }
            }
        }

        for (OsmNode node : oam.getNodes().values()) {
            if (node.getTags().containsKey("navaid")) {
                try {
                    Navaid n = nodeToNavaid(node);
                    navaids.add(n);
                } catch (ParseException e) {
                    errors.add(e);
                }
            }
        }

        for (Way way : oam.getWays().values()) {
            try {
                if (way.getTags().containsKey("airspace")
                 && "yes".equals(way.getTags().get("airspace"))
                 && !way.getTags().containsKey("compound")) {

                    Airspace airspace = wayToAirspace(way, oam.getNodes());
                    airspaces.add(airspace);
                }
            } catch (ParseException e) {
                errors.add(e);
            }
        }
    }

    /**
     * Load an OAM file.
     *
     * @param inputFile the name of the input file
     * @param errors all parsing errors will be put into this list
     * @return the Oam object described by the input file
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     */
    public static Oam loadOam(String                 inputFile,
                              List<ParseException>   errors)
                                            throws ParserConfigurationException,
                                                   SAXException,
                                                   IOException,
                                                   ParseException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        FileReader   fReader = new FileReader(inputFile);
        InputSource  fSource = new InputSource(fReader);
        Document     d = db.parse(fSource);

        OAMReader fOamReader = new OAMReader();
        Oam       oam        = new Oam();
        fOamReader.processOsm(d.getDocumentElement(), oam, errors);

        return oam;
    }

}
