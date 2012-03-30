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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Unit test for the OAMReader class.
 */
public class OAMReaderTest {

    /**
     * Test parsing an OSM / OAM 'node' element.
     *
     * @throws ParserConfigurationException on XML parser issues
     * @throws ParseException on parsing errors
     */
    @Test
    public void testNode() throws ParserConfigurationException, ParseException {
        HashMap<Integer, OsmNode> points = new HashMap<Integer, OsmNode>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document document = db.newDocument();
        Element  node     = document.createElement("node");
        node.setAttribute("id", "-2");
        node.setAttribute("lat", "46.658302");
        node.setAttribute("lon", "16.413235");

        assertEquals(0, points.size());

        OAMReader reader = new OAMReader();
        reader.processNode(node, points);

        assertEquals(1, points.size());
        assertTrue(points.containsKey(-2));

        Point point = points.get(-2);
        assertNotNull(point);
        assertEquals(point.getLatitude(), 46.658302, 0.0);
        assertEquals(point.getLongitude(), 16.413235, 0.0);
    }

    /**
     * Test parsing a number of OSM / OAM 'node' elements.
     *
     * @throws ParserConfigurationException on XML parser issues
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on document parsing issues
     */
    @Test
    public void testNodes() throws ParserConfigurationException,
                                   SAXException,
                                   IOException,
                                   ParseException {
        HashMap<Integer, OsmNode> points = new HashMap<Integer, OsmNode>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream("var/hungary.osm"));

        OAMReader reader = new OAMReader();
        reader.processNodes(d.getDocumentElement(), points);

        assertEquals(2051, points.size());
        assertTrue(points.containsKey(-2020));

        Point point = points.get(-2020);
        assertNotNull(point);
        assertEquals(point.getLatitude(), 46.610283, 0.0);
        assertEquals(point.getLongitude(), 16.444185, 0.0);
    }

    /**
     * Test parsing a 'way' node.
     *
     * @throws ParserConfigurationException on XML parser issues
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on document parsing issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testWay() throws ParserConfigurationException,
                                 SAXException,
                                 IOException,
                                 ParseException,
                                 XPathExpressionException {
        TreeMap<Integer, OsmNode> points = new TreeMap<Integer, OsmNode>();
        TreeMap<Integer, Way>     ways   = new TreeMap<Integer, Way>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();
        XPath          xpath     = XPathFactory.newInstance().newXPath();

        Document   d = db.parse(new FileInputStream("var/hungary.osm"));
        OAMReader reader = new OAMReader();
        reader.processNodes(d.getDocumentElement(), points);
        assertEquals(2051, points.size());


        // get the first 'way' element
        Node n = (Node) xpath.evaluate("//way", d, XPathConstants.NODE);
        reader.processWay((Element) n, points, ways);

        assertEquals(1, ways.size());
        assertTrue(ways.containsKey(-1));

        Way way = ways.get(-1);
        assertNotNull(way);
        assertEquals(3, way.getTags().size());
        assertTrue(way.getTags().containsKey("polygon_id"));
        assertEquals("1", way.getTags().get("polygon_id"));
    }

    /**
     * Test parsing an OAM / OSM document with ways.
     *
     * @throws ParserConfigurationException on XML parser issues
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on document parsing issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testOsm() throws ParserConfigurationException,
                                 SAXException,
                                 IOException,
                                 ParseException,
                                 XPathExpressionException {
        Oam                     oam    = new Oam();
        List<ParseException>    errors = new Vector<ParseException>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream("var/hungary.osm"));
        OAMReader reader = new OAMReader();
        reader.processOsm(d.getDocumentElement(), oam, errors);

        assertTrue(errors.isEmpty());
        assertEquals(1, oam.getWays().size());
        assertTrue(oam.getWays().containsKey(-1));

        Way way = oam.getWays().get(-1);
        assertNotNull(way);
        assertEquals(3, way.getTags().size());
        assertTrue(way.getTags().containsKey("polygon_id"));
        assertEquals("1", way.getTags().get("polygon_id"));
    }

    /**
     * Test reading an OAM document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    @Test
    public void testOam() throws ParserConfigurationException,
                                 SAXException,
                                 IOException,
                                 ParseException,
                                 TransformerException {

        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        // first, get an airspace definitions from a eAIP file
        Document   d = db.parse(
                        new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
        EAIPHungaryReader reader   = new EAIPHungaryReader();

        reader.processEAIP(d.getDocumentElement(), null, airspaces, null,
                           errors);

        assertEquals(4, errors.size());
        assertEquals(47, airspaces.size());

        // convert the airspaces into an Oam object
        Oam oam = new Oam();

        Converter.airspacesToOam(airspaces, oam, Action.CREATE, 1, 0, 0);

        // serialize the Oam object into a stream
        OAMWriter writer = new OAMWriter();
        d = db.newDocument();
        d = writer.processOam(d, oam);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(d);
        StringWriter strWriter = new StringWriter();
        StreamResult result = new StreamResult(strWriter);
        transformer.transform(source, result);

        // and now, parse the resulting XML file

        StringReader strReader = new StringReader(strWriter.toString());
        InputSource  strSource = new InputSource(strReader);
        d = db.parse(strSource);

        OAMReader oamReader = new OAMReader();
        List<Airspace> oamAirspaces = new Vector<Airspace>();
        List<Navaid>   oamNavaids   = new Vector<Navaid>();
        oamReader.processOam(d.getDocumentElement(), oamAirspaces, oamNavaids,
                             errors);

        assertEquals(4, errors.size());
        assertEquals(airspaces.size(), oamAirspaces.size());
        assertTrue(airspaces.containsAll(oamAirspaces));
        assertTrue(oamAirspaces.containsAll(airspaces));
        assertTrue(oamNavaids.isEmpty());
    }
}
