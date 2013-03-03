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
package org.openaviationmap.converter.aixm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.openaviationmap.converter.AixmConverter;
import org.openaviationmap.converter.EAIPHungaryReader;
import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Airspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import aero.aixm.schema._5_1.message.AIXMBasicMessageType;

/**
 * AIXM conveter tests.
 */
public class AixmConveterTest {
    /**
     * Test converting a single airspace.
     *
     * @throws ParserConfigurationException on XML parser errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parser errors
     * @throws JAXBException on JAXB issues
     */
    @Test
    public void testSingleAirspace() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            JAXBException {
        // load some data
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document d = db.parse(new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
        Node airspaceNode = d.getDocumentElement();

        assertNotNull(airspaceNode);

        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();


        reader.processEAIP(airspaceNode, null, airspaces, null, null, errors);

        assertEquals(4, errors.size());
        assertEquals(47, airspaces.size());


        // extract a single airspace for conversion
        assertEquals("LHR1", airspaces.get(2).getDesignator());
        Airspace ap = airspaces.get(2);

        List<Airspace> aps = new Vector<Airspace>();
        aps.add(ap);

        GregorianCalendar   start =
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        start.set(2012, 4, 1, 0, 0, 0);

        aero.aixm.schema._5_1.message.ObjectFactory messageFactory =
                        new aero.aixm.schema._5_1.message.ObjectFactory();
        AIXMBasicMessageType message =
                                messageFactory.createAIXMBasicMessageType();

        AixmConverter.airspacesToAixm(aps,
                                      start,
                                      null,
                                      "BASELINE",
                                      1L, 0L,
                                      message.getHasMember());
        assertEquals(1, message.getHasMember().size());


        // package the airspace into an AIXM message
        JAXBElement<AIXMBasicMessageType> m =
                                messageFactory.createAIXMBasicMessage(message);
        m.getValue().setId("uniqueid");

        // marshal the data into XML using the JAXB marshaller
        JAXBContext  ctx = JAXBContext.newInstance(
                                            "aero.aixm.schema._5_1.message");
        Marshaller   marsh = ctx.createMarshaller();
        marsh.setProperty("jaxb.formatted.output", true);
        marsh.marshal(m, new File("/tmp/ize.aixm51"));
    }

    /**
     * Test converting multiple airspaces.
     *
     * @throws ParserConfigurationException on XML parser errors
     * @throws IOException on I/O errors
     * @throws SAXException on XML parser errors
     * @throws JAXBException on JAXB issues
     */
    @Test
    public void testMultipleAirspaces() throws ParserConfigurationException,
                                               SAXException,
                                               IOException,
                                               JAXBException {
        // load some data
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document d = db.parse(new FileInputStream("var/LH-ENR-5.1-en-HU.xml"));
        Node airspaceNode = d.getDocumentElement();

        assertNotNull(airspaceNode);

        EAIPHungaryReader    reader    = new EAIPHungaryReader();
        List<Airspace>       airspaces = new Vector<Airspace>();
        List<ParseException> errors    = new Vector<ParseException>();


        reader.processEAIP(airspaceNode, null, airspaces, null, null, errors);

        assertEquals(4, errors.size());
        assertEquals(47, airspaces.size());


        // extract a single airspace for conversion
        assertEquals("LHR1", airspaces.get(2).getDesignator());

        GregorianCalendar   start =
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        start.set(2012, 4, 1, 0, 0, 0);

        aero.aixm.schema._5_1.message.ObjectFactory messageFactory =
                            new aero.aixm.schema._5_1.message.ObjectFactory();
        AIXMBasicMessageType message =
                                messageFactory.createAIXMBasicMessageType();


        AixmConverter.airspacesToAixm(airspaces,
                                      start,
                                      null,
                                      "BASELINE",
                                      1L, 0L,
                                      message.getHasMember());
        assertEquals(47, message.getHasMember().size());


        // package the airspace into an AIXM message
        JAXBElement<AIXMBasicMessageType> m =
                                messageFactory.createAIXMBasicMessage(message);
        m.getValue().setId("uniqueid");

        // marshal the data into XML using the JAXB marshaller
        JAXBContext  ctx = JAXBContext.newInstance(
                                            "aero.aixm.schema._5_1.message");
        Marshaller   marsh = ctx.createMarshaller();
        marsh.setProperty("jaxb.formatted.output", true);
        marsh.marshal(m, new File("/tmp/izee.aixm51"));
    }
}
