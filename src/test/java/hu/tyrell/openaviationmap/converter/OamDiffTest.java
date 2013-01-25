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
import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.model.oam.Oam;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test cases for the OamDiff class.
 */
public class OamDiffTest {

    /**
     * The base OAM used in all tests.
     */
    private static Oam hungaryOam;

    /**
     * Parse the base OAM only once.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @BeforeClass
    public static void loadBaseOam() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        hungaryOam = OAMReader.loadOam("var/oam-hungary.xml", errors);
        if (!errors.isEmpty()) {
            throw new ParseException("errors parsing base OAM");
        }
    }

    /**
     * Test identity - when the baseline and the new file is exactly the same.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testIdentity() throws ParserConfigurationException,
                                      SAXException,
                                      IOException,
                                      ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-5.1.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-5.1.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(unchangedOam, "icao"));
        assertTrue(inputOam.compare(unchangedOam, "icao"));
    }

    /**
     * Test input empty - when the input is empty, and the baseline has objects.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testInputEmpty() throws ParserConfigurationException,
                                        SAXException,
                                        IOException,
                                        ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-5.1.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(inputOam.getNodes().isEmpty());
        assertTrue(inputOam.getWays().isEmpty());
        assertTrue(inputOam.getRelations().isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(deletedOam, "icao"));

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test base empty - when the base is empty, and the input has objects.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException in XML transformation errors
     */
    @Test
    public void testBaseEmpty() throws ParserConfigurationException,
                                       SAXException,
                                       IOException,
                                       ParseException,
                                       TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(baseOam.getNodes().isEmpty());
        assertTrue(baseOam.getWays().isEmpty());
        assertTrue(baseOam.getRelations().isEmpty());

        Oam inputOam = OAMReader.loadOam("var/oam-hungary-5.1.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(inputOam.compare(newOam, "icao"));

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test when the baseline is bigger than the input, but there is no
     * change.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation issues
     */
    @Test
    public void testNoChange() throws ParserConfigurationException,
                                      SAXException,
                                      IOException,
                                      ParseException,
                                      TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = hungaryOam;
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-5.1-partial.xml",
                                         errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertEquals(1522, deletedOam.getNodes().size());
        assertEquals(143, deletedOam.getWays().size());
        assertEquals(9, deletedOam.getRelations().size());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertEquals(732, unchangedOam.getNodes().size());
        assertEquals(33, unchangedOam.getWays().size());
        assertEquals(0, unchangedOam.getRelations().size());
    }

    /**
     * Test a scenario where there are new, deleted, changed and unchanged ways
     * as well.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation issues
     */
    @Test
    public void testComplex() throws ParserConfigurationException,
                                     SAXException,
                                     IOException,
                                     ParseException,
                                     TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = hungaryOam;
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-5.1-changed.xml",
                                         errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertEquals(1, newOam.getWays().size());
        assertEquals(144, deletedOam.getWays().size());
        assertEquals(2, updatedOam.getWays().size());
        assertEquals(30, unchangedOam.getWays().size());
    }

    /**
     * Test identity - when the baseline and the new file is exactly the same,
     * for files that have node elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testIdentityNode() throws ParserConfigurationException,
                                          SAXException,
                                          IOException,
                                          ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-4.1.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-4.1.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "id",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(unchangedOam, "id"));
        assertTrue(inputOam.compare(unchangedOam, "id"));
    }

    /**
     * Test input empty - when the input is empty, and the baseline has objects,
     * for files that have node elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testInputEmptyNode() throws ParserConfigurationException,
                                            SAXException,
                                            IOException,
                                            ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-4.1.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(inputOam.getNodes().isEmpty());
        assertTrue(inputOam.getWays().isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "id",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(deletedOam, "id"));

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test base empty - when the base is empty, and the input has objects,
     * for files that have node elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException in XML transformation errors
     */
    @Test
    public void testBaseEmptyNode() throws ParserConfigurationException,
                                           SAXException,
                                           IOException,
                                           ParseException,
                                           TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(baseOam.getNodes().isEmpty());
        assertTrue(baseOam.getWays().isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-4.1.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "id",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(inputOam.compare(newOam, "id"));

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test when the baseline is bigger than the input, but there is no
     * change, for files that have node elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation issues
     */
    @Test
    public void testNoChangeNode() throws ParserConfigurationException,
                                          SAXException,
                                          IOException,
                                          ParseException,
                                          TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = hungaryOam;
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-4.1-partial.xml",
                                         errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "id",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(deletedOam.getWays().isEmpty());
        assertEquals(94, deletedOam.getNodes().size());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getWays().isEmpty());
        assertEquals(5, unchangedOam.getNodes().size());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test a scenario where there are new, deleted, changed and unchanged ways
     * as well, for files that have node elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation issues
     */
    @Test
    public void testComplexNode() throws ParserConfigurationException,
                                         SAXException,
                                         IOException,
                                         ParseException,
                                         TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = hungaryOam;
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-4.1-changed.xml",
                                         errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "id",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertEquals(1, newOam.getNodes().size());
        assertEquals(94, deletedOam.getNodes().size());
        assertEquals(2, updatedOam.getNodes().size());
        assertEquals(3, unchangedOam.getNodes().size());
    }

    /**
     * Test identity - when the baseline and the new file is exactly the same,
     * for files that have relation elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testIdentityRelation() throws ParserConfigurationException,
                                              SAXException,
                                              IOException,
                                               ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-lhdc.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-lhdc.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(unchangedOam, "icao"));
        assertTrue(inputOam.compare(unchangedOam, "icao"));
    }

    /**
     * Test input empty - when the input is empty, and the baseline has objects,
     * for files that have relation elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     */
    @Test
    public void testInputEmptyRelation() throws ParserConfigurationException,
                                                SAXException,
                                                IOException,
                                                ParseException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-hungary-lhdc.xml", errors);
        assertTrue(errors.isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(inputOam.getNodes().isEmpty());
        assertTrue(inputOam.getWays().isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertTrue(baseOam.compare(deletedOam, "icao"));

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test base empty - when the base is empty, and the input has objects,
     * for files that have relation elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException in XML transformation errors
     */
    @Test
    public void testBaseEmptyRelation() throws ParserConfigurationException,
                                               SAXException,
                                               IOException,
                                               ParseException,
                                               TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = OAMReader.loadOam("var/oam-empty.xml", errors);
        assertTrue(errors.isEmpty());
        assertTrue(baseOam.getNodes().isEmpty());
        assertTrue(baseOam.getWays().isEmpty());
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-lhdc.xml", errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(inputOam.compare(newOam, "icao"));

        assertTrue(deletedOam.getNodes().isEmpty());
        assertTrue(deletedOam.getWays().isEmpty());
        assertTrue(deletedOam.getRelations().isEmpty());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertTrue(unchangedOam.getNodes().isEmpty());
        assertTrue(unchangedOam.getWays().isEmpty());
        assertTrue(unchangedOam.getRelations().isEmpty());
    }

    /**
     * Test when the baseline is bigger than the input, but there is no
     * change, for files that have relation elements in them.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation issues
     */
    @Test
    public void testNoChangeRelation() throws ParserConfigurationException,
                                              SAXException,
                                              IOException,
                                              ParseException,
                                              TransformerException {
        List<ParseException>   errors = new Vector<ParseException>();

        Oam baseOam  = hungaryOam;
        Oam inputOam = OAMReader.loadOam("var/oam-hungary-lhdc.xml",
                                         errors);
        assertTrue(errors.isEmpty());

        Oam updatedOam   = new Oam();
        Oam deletedOam   = new Oam();
        Oam newOam       = new Oam();
        Oam unchangedOam = new Oam();

        OamDiff.compareOams(baseOam,
                            inputOam,
                            "icao",
                            newOam,
                            deletedOam,
                            updatedOam,
                            unchangedOam);

        assertTrue(newOam.getNodes().isEmpty());
        assertTrue(newOam.getWays().isEmpty());
        assertTrue(newOam.getRelations().isEmpty());

        assertEquals(2214, deletedOam.getNodes().size());
        assertEquals(168, deletedOam.getWays().size());
        assertEquals(8, deletedOam.getRelations().size());

        assertTrue(updatedOam.getNodes().isEmpty());
        assertTrue(updatedOam.getWays().isEmpty());
        assertTrue(updatedOam.getRelations().isEmpty());

        assertEquals(8, unchangedOam.getWays().size());
        assertEquals(40, unchangedOam.getNodes().size());
        assertEquals(1, unchangedOam.getRelations().size());
    }

}
