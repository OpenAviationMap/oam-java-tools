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
package hu.tyrell.openaviationmap.converter.eaip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hu.tyrell.openaviationmap.converter.ParseException;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.UOM;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * A test case to test the eAIP processor.
 */
public class EAipProcessorTest {

    /**
     * Test a ring type airspace.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     * @throws ParseException on eAIP parsing errors
     */
    @Test
    public void testRingAirspace() throws ParserConfigurationException,
                                          SAXException,
                                          IOException,
                                          ParseException {
        Node airspaceNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream("var/lhr1_eAIP_raw.xml"));
        airspaceNode = d.getDocumentElement();

        assertNotNull(airspaceNode);

        EAipProcessor processor = new EAipProcessorEnr51();
        Airspace airspace = processor.processAirspace(airspaceNode, null);

        assertNotNull(airspace);
        assertEquals("LHR1", airspace.getDesignator());
        assertEquals("BUDAPEST", airspace.getName());
        assertEquals("R", airspace.getType());

        assertEquals(Boundary.Type.RING, airspace.getBoundary().getType());
        Ring ring = (Ring) airspace.getBoundary();
        assertEquals(12, ring.getPointList().size());
        List<Point> points = ring.getPointList();
        assertEquals(47.516389, points.get(0).getLatitude(), 1.0 / 3600.0);
        assertEquals(18.974444, points.get(0).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.515278, points.get(1).getLatitude(), 1.0 / 3600.0);
        assertEquals(19.021667, points.get(1).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.515   , points.get(2).getLatitude(), 1.0 / 3600.0);
        assertEquals(19.033056, points.get(2).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.514722, points.get(3).getLatitude(), 1.0 / 3600.0);
        assertEquals(19.043611, points.get(3).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.516389, points.get(11).getLatitude(), 1.0 / 3600.0);
        assertEquals(18.974444, points.get(11).getLongitude(), 1.0 / 3600.0);

        assertNotNull(airspace.getUpperLimit());
        Elevation ul = airspace.getUpperLimit();
        assertEquals(3500, ul.getElevation(), 0.0);
        assertEquals(UOM.FT, ul.getUom());
        assertEquals(ElevationReference.MSL, ul.getReference());

        assertNotNull(airspace.getLowerLimit());
        Elevation ll = airspace.getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());

        assertEquals("By special permission of the aeronautical authority",
                     airspace.getRemarks());

    }

    /**
     * Test a circle type airspace.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     * @throws ParseException on eAIP parsing errors
     */
    @Test
    public void testCircleAirspace() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException {
        Node airspaceNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream("var/lhp1_eAIP_raw.xml"));
        airspaceNode = d.getDocumentElement();

        assertNotNull(airspaceNode);

        EAipProcessor processor = new EAipProcessorEnr51();
        Airspace airspace = processor.processAirspace(airspaceNode, null);

        assertNotNull(airspace);
        assertEquals("LHP1", airspace.getDesignator());
        assertEquals("PAKS", airspace.getName());
        assertEquals("P", airspace.getType());

        assertEquals(Boundary.Type.CIRCLE,
                     airspace.getBoundary().getType());
        Circle circle = (Circle) airspace.getBoundary();
        assertEquals(3000, circle.getRadius().getDistance(), 0.0);
        assertEquals(UOM.M, circle.getRadius().getUom());
        assertEquals(18.8528, circle.getCenter().getLongitude(),
                1.0 / 3600.0);
        assertEquals(46.57861, circle.getCenter().getLatitude(),
                1.0 / 3600.0);

        assertNotNull(airspace.getUpperLimit());
        Elevation ul = airspace.getUpperLimit();
        assertEquals(195, ul.getElevation(), 0.0);
        assertEquals(UOM.FL, ul.getUom());
        assertEquals(ElevationReference.MSL, ul.getReference());

        assertNotNull(airspace.getLowerLimit());
        Elevation ll = airspace.getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());

        assertEquals("Nuclear Power Plant", airspace.getRemarks());
    }

    /**
     * Test an airspace that is a list of points and contains part of a circle
     * as well.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     * @throws ParseException on eAIP parsing errors
     */
    @Test
    public void testMixedAirspace() throws ParserConfigurationException,
                                           SAXException,
                                           IOException,
                                           ParseException {
        Node airspaceNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream("var/lhb18_eAIP_raw.xml"));
        airspaceNode = d.getDocumentElement();

        assertNotNull(airspaceNode);

        EAipProcessor processor = new EAipProcessorEnr56();
        Airspace airspace = processor.processAirspace(airspaceNode, null);

        assertNotNull(airspace);
        assertEquals("LHB18", airspace.getDesignator());
        assertEquals("FERTŐ", airspace.getName());
        assertEquals("B", airspace.getType());

        assertEquals(Boundary.Type.RING, airspace.getBoundary().getType());
        Ring ring = (Ring) airspace.getBoundary();
        assertEquals(23, ring.getPointList().size());
        List<Point> points = ring.getPointList();
        assertEquals(47.6419444444, points.get(0).getLatitude(), 1.0 / 3600.0);
        assertEquals(16.8661111111, points.get(0).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.6083333333, points.get(1).getLatitude(), 1.0 / 3600.0);
        assertEquals(16.7249999999, points.get(1).getLongitude(), 1.0 / 3600.0);
        assertEquals(47.6247319994, points.get(9).getLatitude(), 1.0 / 3600.0);
        assertEquals(16.6760527881, points.get(9).getLongitude(), 1.0 / 3600.0);

        assertNotNull(airspace.getUpperLimit());
        Elevation ul = airspace.getUpperLimit();
        assertEquals(1500, ul.getElevation(), 0.0);
        assertEquals(UOM.FT, ul.getUom());
        assertEquals(ElevationReference.SFC, ul.getReference());

        assertNotNull(airspace.getLowerLimit());
        Elevation ll = airspace.getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());

        assertEquals("Sensitive fauna", airspace.getRemarks());
        assertEquals("H24", airspace.getActiveTime());
    }

}
