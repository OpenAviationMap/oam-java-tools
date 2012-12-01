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
import hu.tyrell.openaviationmap.converter.eaip.EAipProcessorAd13;
import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.oam.Oam;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import aero.aixm.schema._5_1.message.AIXMBasicMessageType;

/**
 * Test cases for testing conversions of specific eAIP sections to AIXM.
 */
public class EAipToAixmTest {

    /**
     * Test converting an eAIP section to AIXM.
     *
     * @param eAipDocumentName the eAIP document to process.
     * @param aixmDocumentName the AIXM document to verify against.
     * @param borderDocumentName the name of the AIXM document describing the
     *        border line.
     * @param ad13DocumentName the name of an aerodrome list eAIP document
     * @param knownErrors the know number of parse errors
     * @param noAirspaces the expected number of airspaces
     * @param noNavaids the expected number of navaids
     * @param noAerodromes the expected number of aerodromes
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    public void testEAipToAixm(String eAipDocumentName,
                              String  aixmDocumentName,
                              String  borderDocumentName,
                              String  ad13DocumentName,
                              int     knownErrors,
                              int     noAirspaces,
                              int     noNavaids,
                              int     noAerodromes)
                                     throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        List<Airspace>       airspaces  = new Vector<Airspace>();
        List<Navaid>         navaids    = new Vector<Navaid>();
        List<Aerodrome>      aerodromes = new Vector<Aerodrome>();
        List<ParseException> errors     = new Vector<ParseException>();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        // load the border points
        List<Point>    borderPoints = null;

        if (borderDocumentName != null) {
            Document   d   = db.parse(new FileInputStream(borderDocumentName));
            Oam        oam    = new Oam();
            OAMReader  reader = new OAMReader();
            reader.processOsm(d.getDocumentElement(), oam, errors);

            if (!oam.getWays().isEmpty()) {
                // convert the OsmNodes to Points
                List<Integer> refList =
                        oam.getWays().values().iterator().next().getNodeList();

                borderPoints = new Vector<Point>(refList.size());

                for (Integer r : refList) {
                    borderPoints.add(oam.getNodes().get(r).asPoint());
                }
            }
        }

        if (ad13DocumentName != null) {
            Document d = db.parse(new FileInputStream(ad13DocumentName));

            if ("e:AD-1.3".equals(d.getDocumentElement().getTagName())) {
                EAipProcessorAd13 p = new EAipProcessorAd13();
                p.processEAIP(d.getDocumentElement(),
                              borderPoints,
                              airspaces,
                              navaids,
                              aerodromes,
                              errors);
            }

        }

        // first, get an airspace definitions from a eAIP file
        Document   d = db.parse(new FileInputStream(eAipDocumentName));
        EAIPHungaryReader reader   = new EAIPHungaryReader();

        reader.processEAIP(d.getDocumentElement(),
                           borderPoints,
                           airspaces,
                           navaids,
                           aerodromes,
                           errors);

        assertEquals(knownErrors, errors.size());
        assertEquals(noAirspaces, airspaces.size());
        assertEquals(noNavaids, navaids.size());
        assertEquals(noAerodromes, aerodromes.size());

        // convert the airspaces into an Aixm object
        GregorianCalendar vStart =
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        vStart.set(2012, 3, 8, 0, 0, 0);

        JAXBElement<AIXMBasicMessageType> m =
                                AixmConverter.convertToAixm(airspaces,
                                                            navaids,
                                                            aerodromes,
                                                            vStart,
                                                            null,
                                                            "BASELINE",
                                                            1L,
                                                            0L);

        // serialize the Aixm into a stream
        JAXBContext  ctx = JAXBContext.newInstance(
                                              "aero.aixm.schema._5_1.message");
        Marshaller   marsh = ctx.createMarshaller();
        marsh.setProperty("jaxb.formatted.output", true);
        StringWriter strWriter = new StringWriter();
        StreamResult result = new StreamResult(strWriter);
        marsh.marshal(m, result);

        // and now, parse the resulting XML file
        // and compare the two airspace definitions
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        db = dbf.newDocumentBuilder();


        StringReader strReader = new StringReader(strWriter.toString());
        InputSource  strSource = new InputSource(strReader);
        d = db.parse(strSource);


        // TODO: create an AXIM document parser, parse the serialized
        //       result, and compare with the original


        // read a stored AIXM file and compare the airspace definitions
        // with that one as well
        FileReader fReader = new FileReader(aixmDocumentName);
        InputSource  fSource = new InputSource(fReader);
        Document dd = db.parse(fSource);


        // TODO: create an AXIM document parser, parse the loaded
        //       result, and compare with the original


        // for now, just compare the loaded XML document with the generated
        // one
        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        d.normalizeDocument();
        ConverterUtil.canonizeNS(d, nsCtx);

        dd.normalizeDocument();
        ConverterUtil.canonizeNS(dd, nsCtx);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        NodeDiffIdOk diff = new NodeDiffIdOk(dd, d);
        assertTrue(diff.similar());
    }

    /**
     * Test converting an eAIP section ENR-5.1 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr51ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-5.1-en-HU.xml",
                       "var/hungary-5.1.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       4, 47, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-5.2 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr52ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-5.2-en-HU.xml",
                       "var/hungary-5.2.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       0, 34, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-5.5 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr55ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-5.5-en-HU.xml",
                       "var/hungary-5.5.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       0, 15, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-5.6 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr56ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-5.6-en-HU.xml",
                       "var/hungary-5.6.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       0, 37, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-2.1 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr21ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-2.1-en-HU.xml",
                       "var/hungary-2.1.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       0, 18, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-2.2 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr22ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-2.2-en-HU.xml",
                       "var/hungary-2.2.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       0, 3, 0, 0);
    }

    /**
     * Test converting an eAIP section ENR-4.1 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr41ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-4.1-en-HU.xml",
                       "var/hungary-4.1.aixm51",
                       "var/hungary.osm",
                       "var/LH-AD-1.3-en-HU.xml",
                       1, 0, 18, 0);
    }

    /**
     * Test converting an eAIP section ENR-4.4 element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipEnr44ToAixm() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException,
                                            TransformerException,
                                            JAXBException {

        testEAipToAixm("var/LH-ENR-4.4-en-HU.xml",
                      "var/hungary-4.4.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 81, 0);
    }

    /**
     * Test converting an eAIP section AD-LHBC element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhbcToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHBC-en-HU.xml",
                      "var/hungary-lhbc.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHBP element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhbpToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHBP-en-HU.xml",
                      "var/hungary-lhbp.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHDC element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhdcToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHDC-en-HU.xml",
                      "var/hungary-lhdc.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHFM element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhfmToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHFM-en-HU.xml",
                      "var/hungary-lhfm.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHNY element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhnyToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHNY-en-HU.xml",
                      "var/hungary-lhny.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHPP element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhppToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHPP-en-HU.xml",
                      "var/hungary-lhpp.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHPR element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhprToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHPR-en-HU.xml",
                      "var/hungary-lhpr.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHSM element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhsmToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHSM-en-HU.xml",
                      "var/hungary-lhsm.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }

    /**
     * Test converting an eAIP section AD-LHUD element to AIXM.
     *
     * @throws ParserConfigurationException on XML parser configuration errors.
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParseException on AIXM parsing errors
     * @throws TransformerException on XML serialization errors
     * @throws JAXBException on JAXB errors
     */
    @Test
    public void testEAipAdLhudToAixm() throws ParserConfigurationException,
                                             SAXException,
                                             IOException,
                                             ParseException,
                                             TransformerException,
                                             JAXBException {

        testEAipToAixm("var/LH-AD-LHUD-en-HU.xml",
                      "var/hungary-lhud.aixm51",
                      "var/hungary.osm",
                      "var/LH-AD-1.3-en-HU.xml",
                      0, 0, 0, 1);
    }
}
