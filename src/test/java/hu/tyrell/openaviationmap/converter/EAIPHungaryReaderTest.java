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
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Test reading Hungarian eAIP documents.
 */
public class EAIPHungaryReaderTest {

    /**
     * Test an eAIP ENR-5.1 document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     */
    @Test
    public void testEAipEnr51() throws ParserConfigurationException,
                                       SAXException,
                                       IOException {
        Node eAipNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(
                        new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
        eAipNode = d.getDocumentElement();

        assertNotNull(eAipNode);

        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();


        reader.processEAIP(eAipNode, null, airspaces, errors);

        assertEquals(4, errors.size());
        assertEquals(47, airspaces.size());

        // check LHP1
        assertEquals("LHP1", airspaces.get(0).getDesignator());
        assertEquals("PAKS", airspaces.get(0).getName());
        assertEquals("P", airspaces.get(0).getType());
        assertEquals("H24", airspaces.get(0).getActiveTime());
        assertEquals("Nuclear Power Plant", airspaces.get(0).getRemarks());

        assertEquals(Boundary.Type.CIRCLE,
                     airspaces.get(0).getBoundary().getType());
        Circle circle = (Circle) airspaces.get(0).getBoundary();
        assertEquals(3000, circle.getRadius().getDistance(), 0.0);
        assertEquals(UOM.M, circle.getRadius().getUom());
        assertEquals(18.8528, circle.getCenter().getLongitude(),
                1.0 / 3600.0);
        assertEquals(46.57861, circle.getCenter().getLatitude(),
                1.0 / 3600.0);

        assertNotNull(airspaces.get(0).getUpperLimit());
        Elevation ul = airspaces.get(0).getUpperLimit();
        assertEquals(195, ul.getElevation(), 0.0);
        assertEquals(UOM.FL, ul.getUom());
        assertEquals(ElevationReference.MSL, ul.getReference());

        assertNotNull(airspaces.get(0).getLowerLimit());
        Elevation ll = airspaces.get(0).getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());


        // check LHR1
        assertEquals("LHR1", airspaces.get(2).getDesignator());
        assertEquals("BUDAPEST", airspaces.get(2).getName());
        assertEquals("R", airspaces.get(2).getType());
        assertEquals("H24", airspaces.get(2).getActiveTime());
        assertEquals("By special permission of the aeronautical authority",
                     airspaces.get(2).getRemarks());

        assertEquals(Boundary.Type.RING,
                airspaces.get(2).getBoundary().getType());
        Ring ring = (Ring) airspaces.get(2).getBoundary();
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
        assertEquals(18.974444, points.get(11).getLongitude(),
                1.0 / 3600.0);

        assertNotNull(airspaces.get(2).getUpperLimit());
        ul = airspaces.get(2).getUpperLimit();
        assertEquals(3500, ul.getElevation(), 0.0);
        assertEquals(UOM.FT, ul.getUom());
        assertEquals(ElevationReference.MSL, ul.getReference());

        assertNotNull(airspaces.get(2).getLowerLimit());
        ll = airspaces.get(2).getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());

        assertEquals("By special permission of the aeronautical authority",
                     airspaces.get(2).getRemarks());



        // check LHD55
        assertEquals("LHD55", airspaces.get(46).getDesignator());
        assertEquals("SZÜGY", airspaces.get(46).getName());
        assertEquals("D", airspaces.get(46).getType());

        assertEquals(Boundary.Type.CIRCLE,
                airspaces.get(46).getBoundary().getType());
        circle = (Circle) airspaces.get(46).getBoundary();
        assertEquals(2000, circle.getRadius().getDistance(), 0.0);
        assertEquals(UOM.M, circle.getRadius().getUom());
        assertEquals(19.33139, circle.getCenter().getLongitude(),
                1.0 / 3600.0);
        assertEquals(48.0675, circle.getCenter().getLatitude(),
                1.0 / 3600.0);

        assertNotNull(airspaces.get(46).getUpperLimit());
        ul = airspaces.get(46).getUpperLimit();
        assertEquals(2300, ul.getElevation(), 0.0);
        assertEquals(UOM.FT, ul.getUom());
        assertEquals(ElevationReference.MSL, ul.getReference());

        assertNotNull(airspaces.get(46).getLowerLimit());
        ll = airspaces.get(46).getLowerLimit();
        assertEquals(0, ll.getElevation(), 0.0);
        assertEquals(UOM.FT, ll.getUom());
        assertEquals(ElevationReference.SFC, ll.getReference());

        assertEquals("Firing field",
                     airspaces.get(46).getRemarks());


    }

}
