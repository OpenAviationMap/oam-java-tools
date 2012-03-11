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
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test cases for testing conversions of specific eAIP sections to OAM.
 */
public class EAipToOamTest {

    /**
     * Test converting an eAIP section to OAM.
     *
     * @param eAipDocumentName the eAIP document to process.
     * @param oamDocumentName the OAM document to verify against.
     * @param borderDocumentName the name of the OAM document describing the
     *        border line.
     * @param knowErrors the know number of parse errors
     * @param noAirspaces the expected number of airspaces
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    public void testEAipToOam(String eAipDocumentName,
                              String oamDocumentName,
                              String borderDocumentName,
                              int    knowErrors,
                              int    noAirspaces)
                                     throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        // load the border points
        List<Point>    borderPoints = null;

        if (borderDocumentName != null) {
            Document   d = db.parse(new FileInputStream(borderDocumentName));
            TreeMap<Integer, Way>   ways   = new TreeMap<Integer, Way>();
            OAMReader reader = new OAMReader();
            reader.processOsm(d.getDocumentElement(), ways);

            if (!ways.isEmpty()) {
                borderPoints = ways.values().iterator().next().getPointList();
            }
        }


        // first, get an airspace definitions from a eAIP file
        Document   d = db.parse(new FileInputStream(eAipDocumentName));
        EAIPHungaryReader reader   = new EAIPHungaryReader();

        reader.processEAIP(d.getDocumentElement(),
                           borderPoints,
                           airspaces,
                           errors);

        // we have one known error: the ADIZ
        assertEquals(knowErrors, errors.size());
        assertEquals(noAirspaces, airspaces.size());

        // serialize the airspaces into a stream
        OAMWriter writer = new OAMWriter();
        d = db.newDocument();

        d = writer.processAirspaces(d, airspaces, 0, 0, true, 1);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(d);
        StringWriter strWriter = new StringWriter();
        StreamResult result = new StreamResult(strWriter);
        transformer.transform(source, result);

        // and now, parse the resulting XML file
        // and compare the two airspace definitions
        StringReader strReader = new StringReader(strWriter.toString());
        InputSource  strSource = new InputSource(strReader);
        d = db.parse(strSource);

        OAMReader oamReader = new OAMReader();
        List<Airspace> oamAirspaces = new Vector<Airspace>();
        oamReader.processOam(d.getDocumentElement(), oamAirspaces);

        assertEquals(airspaces.size(), oamAirspaces.size());
        assertTrue(airspaces.containsAll(oamAirspaces));
        assertTrue(oamAirspaces.containsAll(airspaces));

        // parse a stored OAM file and compare the airspace definitions
        // with that one as well
        FileReader fReader = new FileReader(oamDocumentName);
        InputSource  fSource = new InputSource(fReader);
        d = db.parse(fSource);

        OAMReader fOamReader = new OAMReader();
        List<Airspace> fOamAirspaces = new Vector<Airspace>();
        fOamReader.processOam(d.getDocumentElement(), fOamAirspaces);

        assertEquals(airspaces.size(), fOamAirspaces.size());
        assertTrue(airspaces.containsAll(fOamAirspaces));
        assertTrue(fOamAirspaces.containsAll(airspaces));
    }

    /**
     * Test converting an eAIP section ENR-5.1 element to OAM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    @Test
    public void testEAipEnr51ToOam() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        testEAipToOam("var/LH-ENR-5.1-en-HU.xml",
                      "var/oam-hungary-5.1.xml",
                      "var/hungary.osm",
                      0, 47);
    }

    /**
     * Test converting an eAIP section ENR-5.2 element to OAM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    @Test
    public void testEAipEnr52ToOam() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        testEAipToOam("var/LH-ENR-5.2-en-HU.xml",
                      "var/oam-hungary-5.2.xml",
                      "var/hungary.osm",
                      1, 34);
    }

    /**
     * Test converting an eAIP section ENR-5.5 element to OAM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    @Test
    public void testEAipEnr55ToOam() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        testEAipToOam("var/LH-ENR-5.5-en-HU.xml",
                      "var/oam-hungary-5.5.xml",
                      "var/hungary.osm",
                      0, 15);
    }

    /**
     * Test converting an eAIP section ENR-5.6 element to OAM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on OAM parsing errors
     * @throws TransformerException on XML serialization errors
     */
    @Test
    public void testEAipEnr56ToOam() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException {

        testEAipToOam("var/LH-ENR-5.6-en-HU.xml",
                      "var/oam-hungary-5.6.xml",
                      "var/hungary.osm",
                      0, 37);
    }
}
