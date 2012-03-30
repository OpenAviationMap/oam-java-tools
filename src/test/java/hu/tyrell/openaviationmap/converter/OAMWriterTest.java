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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.oam.Action;
import hu.tyrell.openaviationmap.model.oam.Oam;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test writing Open Aviation Map files.
 */
public class OAMWriterTest {

    /**
     * Test writing a single airspace.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on eAIP parsing errors
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testSingleAirspace() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        // first, get an airspace definition from a eAIP file
        Document   d = db.parse(new FileInputStream("var/lhr1_eAIP.xml"));
        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();

        reader.processEAIP(d.getDocumentElement(), null, airspaces, null,
                           errors);

        assertTrue(errors.isEmpty());
        assertNotNull(airspaces);
        assertFalse(airspaces.isEmpty());

        // convert the airspaces into an Oam object
        Oam oam = new Oam();

        Converter.airspacesToOam(airspaces, oam, Action.NONE, 1, 0, 0);

        // now,  convert this Oam object into XML
        d = db.newDocument();
        OAMWriter writer = new OAMWriter();

        d = writer.processOam(d, oam);

        Node root = d.getFirstChild();

        // some asserts to check on the results
        XPath          xpath     = XPathFactory.newInstance().newXPath();

        // get the name & designator
        NodeList nodes = (NodeList) xpath.evaluate("//node",
                                                   root,
                                                   XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 11);
        Node attr = nodes.item(2).getAttributes().getNamedItem("id");
        assertEquals("3", attr.getNodeValue());
        attr = nodes.item(2).getAttributes().getNamedItem("version");
        assertEquals("1", attr.getNodeValue());
        attr = nodes.item(2).getAttributes().getNamedItem("lat");
        assertEquals("47.515", attr.getNodeValue());
        attr = nodes.item(2).getAttributes().getNamedItem("lon");
        assertEquals("19.033055555555556", attr.getNodeValue());

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way", root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 1);
        attr = nodes.item(0).getAttributes().getNamedItem("id");
        assertEquals("1", attr.getNodeValue());

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way/nd", root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 12);

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way/tag[@k='icao']", root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 1);
        attr = nodes.item(0).getAttributes().getNamedItem("v");
        assertEquals("LHR1", attr.getNodeValue());
    }

    /**
     * Test writing a number of airpsaces.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on eAIP parsing errors
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testAirspaces()  throws ParserConfigurationException,
                                        SAXException,
                                        IOException,
                                        ParseException,
                                        XPathExpressionException {
        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        // first, get an airspace definition from a eAIP file
        Document   d = db.parse(
                        new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
        EAIPHungaryReader reader   = new EAIPHungaryReader();

        reader.processEAIP(d.getDocumentElement(), null, airspaces, null,
                           errors);

        assertEquals(4, errors.size());
        assertEquals(47, airspaces.size());

        // convert the airspaces into an Oam object
        Oam oam = new Oam();

        Converter.airspacesToOam(airspaces, oam, Action.NONE, 1, 0, 0);

        // now,  convert the Oam object into XML
        d = db.newDocument();
        OAMWriter writer = new OAMWriter();

        d = writer.processOam(d, oam);

        Node root = d.getFirstChild();

        // some asserts to check on the results
        XPath          xpath     = XPathFactory.newInstance().newXPath();

        // get the name & designator
        NodeList nodes = (NodeList) xpath.evaluate("//node",
                                                   root,
                                                   XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 929);

        nodes = (NodeList) xpath.evaluate("//node[@id='69']",
                                          root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 1);
        Node attr = nodes.item(0).getAttributes().getNamedItem("id");
        assertEquals("69", attr.getNodeValue());
        attr = nodes.item(0).getAttributes().getNamedItem("version");
        assertEquals("1", attr.getNodeValue());
        attr = nodes.item(0).getAttributes().getNamedItem("lat");
        assertEquals("47.515", attr.getNodeValue());
        attr = nodes.item(0).getAttributes().getNamedItem("lon");
        assertEquals("19.033055555555556", attr.getNodeValue());

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way", root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 47);
        attr = nodes.item(0).getAttributes().getNamedItem("id");
        assertEquals("1", attr.getNodeValue());

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way[@id='3']/nd", root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 12);

        xpath.reset();
        nodes = (NodeList) xpath.evaluate("//way[@id='3']/tag[@k='icao']",
                                          root,
                                          XPathConstants.NODESET);
        assertEquals(nodes.getLength(), 1);
        attr = nodes.item(0).getAttributes().getNamedItem("v");
        assertEquals("LHR1", attr.getNodeValue());
    }
}
