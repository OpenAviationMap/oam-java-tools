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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
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
import hu.tyrell.openaviationmap.model.oam.Oam;

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


        reader.processEAIP(eAipNode, null, airspaces, null, null, errors);

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

    /**
     * Test an eAIP ENR-4.1 document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     */
    @Test
    public void testEAipEnr41() throws ParserConfigurationException,
                                       SAXException,
                                       IOException {
        Node eAipNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(
                        new FileInputStream("var/LH-ENR-4.1-en-HU.xml"));
        eAipNode = d.getDocumentElement();

        assertNotNull(eAipNode);

        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Navaid>         navaids   = new Vector<Navaid>();
        List<ParseException> errors    = new Vector<ParseException>();


        reader.processEAIP(eAipNode, null, null, navaids, null, errors);

        assertEquals(1, errors.size());
        assertEquals(18, navaids.size());

        // check the BKS VOR/DME
        Navaid navaid = navaids.get(0);
        assertEquals("BKS-DVORDME", navaid.getId());
        assertEquals(Navaid.Type.VORDME, navaid.getType());
        assertEquals("BÉKÉS", navaid.getName());
        assertEquals("BKS", navaid.getIdent());
        assertEquals(4.2, navaid.getDeclination(), 0.01);
        assertNull(navaid.getVariation());
        assertEquals(Frequency.fromString("115.8MHz"), navaid.getFrequency());
        assertEquals("105X", navaid.getDmeChannel());
        assertEquals("H24", navaid.getActivetime());
        assertEquals(46.8, navaid.getLatitude(), 0.00001);
        assertEquals(21.07388, navaid.getLongitude(), 0.00001);
        assertEquals(new Elevation(95, UOM.M, ElevationReference.MSL),
                     navaid.getElevation());
        assertEquals(new Distance(100, UOM.NM), navaid.getCoverage());
        assertEquals("Coverage: 100 NM/185 km DME COORD: 464759.9N 0210426.0E",
                navaid.getRemarks());

        // check the BKS NDB
        navaid = navaids.get(1);
        assertEquals("BKS-NDB", navaid.getId());
        assertEquals(Navaid.Type.NDB, navaid.getType());
        assertEquals("BÉKÉS", navaid.getName());
        assertEquals("BKS", navaid.getIdent());
        assertEquals(0, navaid.getDeclination(), 0.01);
        assertEquals(new MagneticVariation(4.1, 2009),
                navaid.getVariation());
        assertEquals(Frequency.fromString("374kHz"), navaid.getFrequency());
        assertNull(navaid.getDmeChannel());
        assertEquals("H24", navaid.getActivetime());
        assertEquals(46.79499, navaid.getLatitude(), 0.00001);
        assertEquals(21.07805, navaid.getLongitude(), 0.00001);
        assertNull(navaid.getElevation());
        assertEquals(new Distance(60, UOM.NM), navaid.getCoverage());
        assertEquals("Coverage: 60NM/110km", navaid.getRemarks());
    }

    /**
     * Test an eAIP ENR-4.4 document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     */
    @Test
    public void testEAipEnr44() throws ParserConfigurationException,
                                       SAXException,
                                       IOException {
        Node eAipNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(
                        new FileInputStream("var/LH-ENR-4.4-en-HU.xml"));
        eAipNode = d.getDocumentElement();

        assertNotNull(eAipNode);

        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Navaid>         navaids   = new Vector<Navaid>();
        List<ParseException> errors    = new Vector<ParseException>();


        reader.processEAIP(eAipNode, null, null, navaids, null, errors);

        assertEquals(0, errors.size());
        assertEquals(81, navaids.size());

        // check the ABETI
        Navaid navaid = navaids.get(0);
        assertEquals("ABETI", navaid.getId());
        assertEquals(Navaid.Type.DESIGNATED, navaid.getType());
        assertEquals("ABETI", navaid.getName());
        assertEquals("ABETI", navaid.getIdent());
        assertEquals(47.67777, navaid.getLatitude(), 0.00001);
        assertEquals(17.01277, navaid.getLongitude(), 0.00001);

        // check the DIMLO
        navaid = navaids.get(20);
        assertEquals("DIMLO", navaid.getId());
        assertEquals(Navaid.Type.DESIGNATED, navaid.getType());
        assertEquals("DIMLO", navaid.getName());
        assertEquals("DIMLO", navaid.getIdent());
        assertEquals(46.68361, navaid.getLatitude(), 0.00001);
        assertEquals(16.42277, navaid.getLongitude(), 0.00001);
    }

    /**
     * Test an eAIP AD-1.3 document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     */
    @Test
    public void testEAipAd13() throws ParserConfigurationException,
                                    SAXException,
                                    IOException {
        Node eAipNode = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(
                        new FileInputStream("var/LH-AD-1.3-en-HU.xml"));
        eAipNode = d.getDocumentElement();

        assertNotNull(eAipNode);

        EAIPHungaryReader    reader     = new EAIPHungaryReader();
        List<Aerodrome>      aerodromes = new Vector<Aerodrome>();
        List<ParseException> errors     = new Vector<ParseException>();


        reader.processEAIP(eAipNode, null, null, null, aerodromes, errors);

        assertEquals(0, errors.size());
        assertEquals(9, aerodromes.size());

        Aerodrome ad = aerodromes.get(0);
        assertEquals("LHBC", ad.getIcao());
        assertEquals("BÉKÉSCSABA", ad.getName());
        ad = aerodromes.get(6);
        assertEquals("LHPP", ad.getIcao());
        assertEquals("PÉCS-POGÁNY", ad.getName());
    }

    /**
     * Test an eAIP AD document.
     *
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing issues
     */
    @Test
    public void testEAipAd() throws ParserConfigurationException,
                                    SAXException,
                                    IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        List<ParseException> errors     = new Vector<ParseException>();

        // load the border points
        List<Point>    borderPoints = null;

        Document   d         = db.parse(new FileInputStream("var/hungary.osm"));
        Oam        oam       = new Oam();
        OAMReader  oamReader = new OAMReader();
        oamReader.processOsm(d.getDocumentElement(), oam, errors);
        assertTrue(errors.isEmpty());

        if (!oam.getWays().isEmpty()) {
            // convert the OsmNodes to Points
            List<Integer> refList =
                    oam.getWays().values().iterator().next().getNodeList();

            borderPoints = new Vector<Point>(refList.size());

            for (Integer r : refList) {
                borderPoints.add(oam.getNodes().get(r).asPoint());
            }
        }


        // process the airport definition file
        Node eAipNode = null;

        d = db.parse(new FileInputStream("var/LH-AD-LHBC-en-HU.xml"));
        eAipNode = d.getDocumentElement();

        assertNotNull(eAipNode);

        EAIPHungaryReader    reader     = new EAIPHungaryReader();
        List<Aerodrome>      aerodromes = new Vector<Aerodrome>();


        reader.processEAIP(eAipNode, borderPoints, null, null,
                           aerodromes, errors);

        assertEquals(0, errors.size());
        assertEquals(1, aerodromes.size());

        // check the processed aerodrome
        Aerodrome ad = aerodromes.get(0);
        assertEquals("LHBC", ad.getIcao());
        assertNotNull(ad.getArp());
        assertEquals(Frequency.fromString("123.25MHz"), ad.getAfis());
        assertEquals(46.68333, ad.getArp().getLatitude(), 0.00001);
        assertEquals(21.16249, ad.getArp().getLongitude(), 0.00001);
        assertTrue(ad.getElevation().equals(
                new Elevation(87, UOM.M, ElevationReference.MSL)));
        assertEquals("Prior permission required.", ad.getRemarks());

        // check the related TIZ
        assertNotNull(ad.getAirspaces());
        assertEquals(1, ad.getAirspaces().size());
        Airspace ap = ad.getAirspaces().get(0);
        assertEquals("Békéscsaba TIZ", ap.getName());
        assertEquals("TIZ", ap.getType());
        assertEquals("F", ap.getAirspaceClass());
        assertEquals(Boundary.Type.RING, ap.getBoundary().getType());
        assertEquals(29, ((Ring) ap.getBoundary()).getPointList().size());
        assertTrue(new Elevation(4000, UOM.FT, ElevationReference.MSL)
                   .equals(ap.getUpperLimit()));
        assertTrue(new Elevation(0, UOM.FT, ElevationReference.SFC)
                   .equals(ap.getLowerLimit()));

        // check some of the runways
        assertEquals(4, ad.getRunways().size());
        Runway rwy = ad.getRunways().get(0);
        assertEquals("17L", rwy.getDesignator());
        assertEquals(174.5, rwy.getBearing(), 0.001);
        assertTrue(rwy.getLength().equals(new Distance(1300, UOM.M)));
        assertTrue(rwy.getWidth().equals(new Distance(30, UOM.M)));
        assertEquals(SurfaceType.ASPHALT, rwy.getSurface());
        assertEquals(46.6891944, rwy.getThreshold().getLatitude(), 0.0000001);
        assertEquals(21.1617083, rwy.getThreshold().getLongitude(), 0.0000001);
        assertEquals(46.6775527, rwy.getEnd().getLatitude(), 0.0000001);
        assertEquals(21.1633277, rwy.getEnd().getLongitude(), 0.0000001);
        assertTrue(rwy.getElevation().equals(
                         new Elevation(86, UOM.M, ElevationReference.MSL)));
        assertTrue(rwy.getTora().equals(new Distance(1300, UOM.M)));
        assertTrue(rwy.getToda().equals(new Distance(1300, UOM.M)));
        assertTrue(rwy.getAsda().equals(new Distance(1300, UOM.M)));
        assertTrue(rwy.getLda().equals(new Distance(1300, UOM.M)));
        assertEquals(0.08, rwy.getSlope(), 0.001);

        rwy = ad.getRunways().get(3);
        assertEquals("35L", rwy.getDesignator());
        assertEquals(354.5, rwy.getBearing(), 0.001);
        assertTrue(rwy.getLength().equals(new Distance(790, UOM.M)));
        assertTrue(rwy.getWidth().equals(new Distance(40, UOM.M)));
        assertEquals(SurfaceType.GRASS, rwy.getSurface());
        assertEquals(46.6758722, rwy.getThreshold().getLatitude(), 0.0000001);
        assertEquals(21.1583750, rwy.getThreshold().getLongitude(), 0.0000001);
        assertEquals(46.6829444, rwy.getEnd().getLatitude(), 0.0000001);
        assertEquals(21.1573888, rwy.getEnd().getLongitude(), 0.0000001);
        assertTrue(rwy.getElevation().equals(
                         new Elevation(86, UOM.M, ElevationReference.MSL)));
        assertTrue(rwy.getTora().equals(new Distance(790, UOM.M)));
        assertTrue(rwy.getToda().equals(new Distance(790, UOM.M)));
        assertTrue(rwy.getAsda().equals(new Distance(790, UOM.M)));
        assertTrue(rwy.getLda().equals(new Distance(790, UOM.M)));
        assertEquals(0.06, rwy.getSlope(), 0.001);

        // check the related navaids
        assertEquals(2, ad.getNavaids().size());
        Navaid navaid = ad.getNavaids().get(0);
        assertEquals(Navaid.Type.NDB, navaid.getType());
        assertEquals(Frequency.fromString("400kHz"), navaid.getFrequency());
        assertEquals("H24", navaid.getActivetime());
        assertEquals(46.6648888, navaid.getLatitude(), 0.0000001);
        assertEquals(21.1650833, navaid.getLongitude(), 0.0000001);
        assertEquals("LI 35R", navaid.getRemarks());

        navaid = ad.getNavaids().get(1);
        assertEquals(Navaid.Type.VORDME, navaid.getType());
        assertEquals(Frequency.fromString("115.8MHz"), navaid.getFrequency());
        assertEquals("105X", navaid.getDmeChannel());
        assertEquals("H24", navaid.getActivetime());
        assertEquals(46.7999722, navaid.getLatitude(), 0.0000001);
        assertEquals(21.0738888, navaid.getLongitude(), 0.0000001);
        assertTrue(navaid.getElevation().equals(
                   new Elevation(95, UOM.M, ElevationReference.MSL)));
        assertEquals("DME COORD:464759.9N 0210426.0E", navaid.getRemarks());
}
}
