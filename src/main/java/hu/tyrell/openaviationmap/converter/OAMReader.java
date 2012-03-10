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

import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Distance;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.UOM;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class to read Open Aviation Map (and Open Street Map) files.
 */
public class OAMReader {
    /**
     * Process a 'node' element in an OAM file.
     *
     * @param node the XML node that represents an OSM 'node' element
     * @param nodes a map that contains the processed nodes, with the nodes
     *        id as the key
     */
    void processNode(Element node, Map<Integer, Point> nodes) {
        if (!"node".equals(node.getTagName())) {
            return;
        }

        String idStr  = node.getAttribute("id");
        String latStr = node.getAttribute("lat");
        String lonStr = node.getAttribute("lon");

        Integer id  = Integer.parseInt(idStr);
        double  lat = Double.parseDouble(latStr);
        double  lon = Double.parseDouble(lonStr);

        Point point = new Point();
        point.setLatitude(lat);
        point.setLongitude(lon);

        nodes.put(id, point);
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
    void processNodes(Element parent, Map<Integer, Point> nodes)
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
     * Process a 'tag' element, which is part of a 'way' element.
     *
     * @param node the XML 'tag' element
     * @param way the 'way' to add this tag to.
     */
    void processTag(Element node, Way way) {
        if (!"tag".equals(node.getTagName())) {
            return;
        }

        String key   = node.getAttribute("k");
        String value = node.getAttribute("v");

        way.getTags().put(key, value);
    }

    /**
     * Process an 'nd' element, which is part of a 'way' element.
     *
     * @param node the XML 'tag' element
     * @param points a map of existing points, into which this node reference
     *        points by an id
     * @param way the 'way' to add this tag to.
     * @throws ParseException on document parsing errors
     */
    void processNodeRef(Element             node,
                        Map<Integer, Point> points,
                        Way                 way)        throws ParseException {
        if (!"nd".equals(node.getTagName())) {
            return;
        }

        String  refStr = node.getAttribute("ref");
        Integer ref    = Integer.parseInt(refStr);

        if (!points.containsKey(ref)) {
            throw new ParseException("node reference points to nonexistent "
                                   + "node: " + refStr);
        }

        way.getPointList().add(points.get(ref));
    }

    /**
     * Process am OSM / OAM 'way' node.
     *
     * @param node an XML 'way' node
     * @param points a map of OAM 'nodes', with the key being the nodes id
     * @param ways a map of OAM 'ways', with the key being the ways id
     * @throws ParseException on document parsing errors
     */
    void processWay(Element             node,
                    Map<Integer, Point> points,
                    Map<Integer, Way>   ways)       throws ParseException {
        if (!"way".equals(node.getTagName())) {
            return;
        }

        String  idStr = node.getAttribute("id");
        Integer id    = Integer.parseInt(idStr);

        Way way = new Way();

        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            // get the 'tag' elements
            NodeList n = (NodeList) xpath.evaluate("tag", node,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processTag((Element) n.item(i), way);
            }

            // get the all 'nd' (node reference) elements
            n = (NodeList) xpath.evaluate("nd", node, XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processNodeRef((Element) n.item(i), points, way);
            }

        } catch (Exception e) {
            throw new ParseException(e);
        }

        ways.put(id, way);
    }

    /**
     * Process an OAM / OSM document and generate a number of Way elements.
     *
     * @param root the document root element to process
     * @param ways a map of Way elements, where the processed Way elements will
     *        be placed. the way elements are keyd by their ids.
     * @throws ParseException on document parsing errors
     */
    public void processOsm(Element root, Map<Integer, Way> ways)
                                                    throws ParseException {
        if (!"osm".equals(root.getTagName())) {
            return;
        }

        TreeMap<Integer, Point> points = new TreeMap<Integer, Point>();
        processNodes(root, points);

        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            // get the 'way' elements
            NodeList n = (NodeList) xpath.evaluate("//way", root,
                                                   XPathConstants.NODESET);
            for (int i = 0; i < n.getLength(); ++i) {
                processWay((Element) n.item(i), points, ways);
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(e);
        }
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
     * @return the airspace corresponding to the supplied 'way' element.
     * @throws ParseException on parsing errors
     */
    Airspace wayToAirspace(Way way) throws ParseException {
        Map<String, String> tags = way.getTags();

        if (!tags.containsKey("airspace")
         || !"yes".equals(tags.get("airspace"))) {

            throw new ParseException("way is not an airspace");
        }

        Airspace airspace = new Airspace();
        String   k;

        k = "icao";
        if (tags.containsKey(k)) {
            airspace.setDesignator(tags.get(k));
        }

        k = "name";
        if (tags.containsKey(k)) {
            airspace.setName(tags.get(k));
        }

        k = "longname";
        if (tags.containsKey(k)) {
            airspace.setRemarks(tags.get(k));
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

        if (tags.containsKey("airspace:center:lat")
         && tags.containsKey("airspace:center:lon")
         && tags.containsKey("airspace:radius")
         && tags.containsKey("airspace:radius:unit")) {

            // this is a circle airspace
            double lat    = Double.parseDouble(tags.get("airspace:center:lat"));
            double lon    = Double.parseDouble(tags.get("airspace:center:lon"));
            double rDist  = Double.parseDouble(tags.get("airspace:radius"));
            String unit   = tags.get("airspace:radius:unit");

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
            List<Point> pl = new Vector<Point>(way.getPointList());

            Ring r = new Ring();
            r.setPointList(pl);

            airspace.setBoundary(r);
        }

        return airspace;
    }

    /**
     * Process an OAM document and generate a number of Airspace elements.
     *
     * @param root the document root element to process
     * @param airspaces a list of Airspace elements, which will contain
     *        the airspaces contained on the OAM document.
     * @throws ParseException on document parsing errors
     */
    public void processOam(Element root, List<Airspace> airspaces)
                                                     throws ParseException {
        TreeMap<Integer, Way> ways = new TreeMap<Integer, Way>();
        processOsm(root, ways);

        for (Way way : ways.values()) {
            Airspace airspace = wayToAirspace(way);
            airspaces.add(airspace);
        }
    }
}
