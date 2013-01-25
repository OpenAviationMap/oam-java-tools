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
package hu.tyrell.openaviationmap.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.oam.Action;

import java.io.FileInputStream;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test cases to test the conversion process.
 */
public class ConverterTest {

    /**
     * Test converting eAIP airspaces which refer to national border boundaries.
     *
     * @throws Exception on conversion errors
     */
    @Test
    public void testNationalBorderAirspace() throws Exception {
        String                  inputFile      = "var/LH-ENR-5.6-en-HU.xml";
        String                  inputFormat    = "eAIP.Hungary";
        String                  outputFile     = "var/test-oam-hungary-5.6.xml";
        String                  outputFormat   = "OAM";
        Action                  action         = Action.CREATE;
        String                  borderFile     = "var/hungary.osm";
        String                  adFile         = "var/LH-AD-1.3-en-HU.xml";
        int                     version        = 1;
        List<ParseException>    errors         = new Vector<ParseException>();

        Converter.convert(inputFile,
                          inputFormat,
                          outputFile,
                          outputFormat,
                          action,
                          borderFile,
                          adFile,
                          null,
                          null,
                          version,
                          errors);

        assertTrue(errors.isEmpty());

        // so far so good - now read the generated file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        Document   d = db.parse(new FileInputStream(outputFile));
        OAMReader  reader = new OAMReader();
        List<Airspace>      airspaces  = new Vector<Airspace>();
        List<Navaid>        navaids    = new Vector<Navaid>();
        List<Aerodrome>     aerodromes = new Vector<Aerodrome>();
        reader.processOam(d.getDocumentElement(),
                          airspaces,
                          navaids,
                          aerodromes,
                          errors);

        assertTrue(errors.isEmpty());
        assertEquals(37, airspaces.size());

        // look for & check LHB18
        Airspace airspace = null;
        for (Airspace as : airspaces) {
            if ("LHB18".equals(as.getDesignator())) {
                airspace = as;
                break;
            }
        }
        assertNotNull(airspace);
        assertEquals("B", airspace.getType());
        assertEquals(Boundary.Type.RING, airspace.getBoundary().getType());
        Ring r = (Ring) airspace.getBoundary();
        assertEquals(25, r.getPointList().size());

        // look for & check LHB03
        airspace = null;
        for (Airspace as : airspaces) {
            if ("LHB03".equals(as.getDesignator())) {
                airspace = as;
                break;
            }
        }
        assertNotNull(airspace);
        assertEquals("B", airspace.getType());
        assertEquals(Boundary.Type.RING, airspace.getBoundary().getType());
        r = (Ring) airspace.getBoundary();
        assertEquals(50, r.getPointList().size());
    }

}
