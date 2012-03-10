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
import static org.junit.Assert.fail;
import hu.tyrell.openaviationmap.model.Airspace;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test writing Open Aviation Map files.
 */
public class OAMWriterTest {

    /**
     * Test writing a single airspace.
     */
    @Test
    public void testSingleAirspace() {
        Airspace airspace = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder        db  = dbf.newDocumentBuilder();

            // first, get an airspace definition from a eAIP file
            Document   d = db.parse(new FileInputStream("var/lhr1_eAIP.xml"));
            EAIPHungaryReader reader   = new EAIPHungaryReader();

            airspace = reader.processAirspace(d.getDocumentElement());
            assertNotNull(airspace);

            // now,  convert this airspace into a OAM document
            d = db.newDocument();
            OAMWriter writer = new OAMWriter();

            ArrayList<Airspace> airspaces = new ArrayList<Airspace>();
            airspaces.add(airspace);

            d = writer.processAirspaces(d, airspaces, 0, 0, false, 1);

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

        } catch (Exception e) {
            fail(e.toString());
        }

    }

    /**
     * Test writing a number of airpsaces.
     */
    @Test
    public void testAirspaces() {
        List<Airspace> airspaces = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder        db  = dbf.newDocumentBuilder();

            // first, get an airspace definition from a eAIP file
            Document   d = db.parse(
                            new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
            EAIPHungaryReader reader   = new EAIPHungaryReader();

            airspaces = reader.processEAIP(d.getDocumentElement());
            assertEquals(47, airspaces.size());

            // now,  convert this airspace into a OAM document
            d = db.newDocument();
            OAMWriter writer = new OAMWriter();

            d = writer.processAirspaces(d, airspaces, 0, 0, false, 1);

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

        } catch (Exception e) {
            fail(e.toString());
        }

    }
}
