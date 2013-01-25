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

import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to test AIXM diffs.
 */
public class AixmDiffTest {

    /**
     * XPath statement that leads to the aixm:designator element within
     * a hasMember element.
     */
    private static final String XPATH_DESIGNATOR =
                "*/aixm:timeSlice/aixm:AirspaceTimeSlice/aixm:designator";

    /**
     * Normalize the input document, so that features are in proper order,
     * namespaces are normalized, white space is removed.
     *
     * @param document the document to normalize
     * @param nsCtx the namespace context to use
     */
    private void normalizeInput(Document document, NamespaceContext nsCtx) {
        document.normalizeDocument();
        ConverterUtil.removeWhitespace(document);
        AixmDiff.reorderMessage(document, XPATH_DESIGNATOR, nsCtx);
        ConverterUtil.canonizeNS(document, nsCtx);
    }

    /**
     * Test identity - when the baseline and the new file is exactly the same.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws JAXBException on JAXB errors
     * @throws TransformerException in XML transformation issues
     */
    @Test
    public void testIdentity() throws ParserConfigurationException,
                                        SAXException,
                                        IOException,
                                        ParseException,
                                        JAXBException,
                                        TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        FileReader   fReader      = new FileReader("var/hungary-5.1.aixm51");
        InputSource  fSource      = new InputSource(fReader);
        Document     baseDocument = db.parse(fSource);
        normalizeInput(baseDocument, nsCtx);

        fReader = new FileReader("var/hungary-5.1.aixm51");
        fSource = new InputSource(fReader);
        Document     inputDocument = db.parse(fSource);
        normalizeInput(inputDocument, nsCtx);

        Document updatedDocument   = db.newDocument();
        Document deletedDocument   = db.newDocument();
        Document newDocument       = db.newDocument();
        Document unchangedDocument = db.newDocument();

        AixmDiff.compareMessages(baseDocument,
                                 inputDocument,
                                 XPATH_DESIGNATOR,
                                 nsCtx,
                                 newDocument,
                                 deletedDocument,
                                 updatedDocument,
                                 unchangedDocument);

        // now check the results
        fReader      = new FileReader("var/empty.aixm51");
        fSource      = new InputSource(fReader);
        Document     emptyDocument = db.parse(fSource);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);

        Diff diff = new Diff(emptyDocument, newDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, deletedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, updatedDocument);
        assertTrue(diff.identical());

        diff = new Diff(inputDocument, unchangedDocument);
        assertTrue(diff.identical());

        diff = new Diff(baseDocument, unchangedDocument);
        assertTrue(diff.identical());
    }

    /**
     * Test when the baseline is empty and the input has lots of stuff.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws JAXBException on JAXB errors
     * @throws TransformerException in XML transformation issues
     */
    @Test
    public void testBaseEmpty() throws ParserConfigurationException,
                                         SAXException,
                                         IOException,
                                         ParseException,
                                         JAXBException,
                                         TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        FileReader   fReader      = new FileReader("var/empty.aixm51");
        InputSource  fSource      = new InputSource(fReader);
        Document     baseDocument = db.parse(fSource);
        normalizeInput(baseDocument, nsCtx);

        fReader = new FileReader("var/hungary-5.1.aixm51");
        fSource = new InputSource(fReader);
        Document     inputDocument = db.parse(fSource);
        normalizeInput(inputDocument, nsCtx);

        Document updatedDocument   = db.newDocument();
        Document deletedDocument   = db.newDocument();
        Document newDocument       = db.newDocument();
        Document unchangedDocument = db.newDocument();

        AixmDiff.compareMessages(baseDocument,
                                 inputDocument,
                                 XPATH_DESIGNATOR,
                                 nsCtx,
                                 newDocument,
                                 deletedDocument,
                                 updatedDocument,
                                 unchangedDocument);

        // now check the results
        fReader      = new FileReader("var/empty.aixm51");
        fSource      = new InputSource(fReader);
        Document     emptyDocument = db.parse(fSource);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);

        Diff diff = new Diff(inputDocument, newDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, deletedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, updatedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, unchangedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, baseDocument);
        assertTrue(diff.identical());
    }

    /**
     * Test when the baseline has all the data, and the input is empty.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws JAXBException on JAXB errors
     * @throws TransformerException in XML transformation issues
     */
    @Test
    public void testInputEmpty() throws ParserConfigurationException,
                                          SAXException,
                                          IOException,
                                          ParseException,
                                          JAXBException,
                                          TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        FileReader   fReader      = new FileReader("var/hungary-5.1.aixm51");
        InputSource  fSource      = new InputSource(fReader);
        Document     baseDocument = db.parse(fSource);
        normalizeInput(baseDocument, nsCtx);

        fReader = new FileReader("var/empty.aixm51");
        fSource = new InputSource(fReader);
        Document     inputDocument = db.parse(fSource);
        normalizeInput(inputDocument, nsCtx);

        Document updatedDocument   = db.newDocument();
        Document deletedDocument   = db.newDocument();
        Document newDocument       = db.newDocument();
        Document unchangedDocument = db.newDocument();

        AixmDiff.compareMessages(baseDocument,
                                 inputDocument,
                                 XPATH_DESIGNATOR,
                                 nsCtx,
                                 newDocument,
                                 deletedDocument,
                                 updatedDocument,
                                 unchangedDocument);

        // now check the results
        fReader      = new FileReader("var/empty.aixm51");
        fSource      = new InputSource(fReader);
        Document     emptyDocument = db.parse(fSource);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);

        Diff diff = new Diff(emptyDocument, newDocument);
        assertTrue(diff.identical());

        diff = new Diff(baseDocument, deletedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, updatedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, unchangedDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, baseDocument);
        assertTrue(!diff.identical());
    }

    /**
     * Test when the input is a subset of the base document, but there is
     * no change in the input document.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws JAXBException on JAXB errors
     * @throws TransformerException in XML transformation issues
     */
    @Test
    public void testNoChange() throws ParserConfigurationException,
                                        SAXException,
                                        IOException,
                                        ParseException,
                                        JAXBException,
                                        TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        FileReader   fReader      = new FileReader("var/hungary-5.1.aixm51");
        InputSource  fSource      = new InputSource(fReader);
        Document     baseDocument = db.parse(fSource);
        normalizeInput(baseDocument, nsCtx);

        fReader = new FileReader("var/hungary-5.1-partial.aixm51");
        fSource = new InputSource(fReader);
        Document     inputDocument = db.parse(fSource);
        normalizeInput(inputDocument, nsCtx);

        Document updatedDocument   = db.newDocument();
        Document deletedDocument   = db.newDocument();
        Document newDocument       = db.newDocument();
        Document unchangedDocument = db.newDocument();

        AixmDiff.compareMessages(baseDocument,
                                 inputDocument,
                                 XPATH_DESIGNATOR,
                                 nsCtx,
                                 newDocument,
                                 deletedDocument,
                                 updatedDocument,
                                 unchangedDocument);

        // now check the results
        fReader      = new FileReader("var/empty.aixm51");
        fSource      = new InputSource(fReader);
        Document     emptyDocument = db.parse(fSource);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);

        Diff diff = new Diff(emptyDocument, newDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, deletedDocument);
        assertTrue(!diff.identical());
        assertEquals(45, numFeatures(deletedDocument, nsCtx));

        diff = new Diff(emptyDocument, updatedDocument);
        assertTrue(diff.identical());

        diff = new Diff(inputDocument, unchangedDocument);
        assertTrue(diff.identical());
    }

    /**
     * Test when the input contains both changed & unchanged data.
     *
     * @throws ParseException on parsing documents
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws JAXBException on JAXB errors
     * @throws TransformerException in XML transformation issues
     */
    @Test
    public void testComplex() throws ParserConfigurationException,
                                       SAXException,
                                       IOException,
                                       ParseException,
                                       JAXBException,
                                       TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder        db  = dbf.newDocumentBuilder();

        NamespaceContext nsCtx = AixmConverter.getNsCtx();

        FileReader   fReader      = new FileReader("var/hungary-5.1.aixm51");
        InputSource  fSource      = new InputSource(fReader);
        Document     baseDocument = db.parse(fSource);
        normalizeInput(baseDocument, nsCtx);

        fReader = new FileReader("var/hungary-5.1-changed.aixm51");
        fSource = new InputSource(fReader);
        Document     inputDocument = db.parse(fSource);
        normalizeInput(inputDocument, nsCtx);

        Document updatedDocument   = db.newDocument();
        Document deletedDocument   = db.newDocument();
        Document newDocument       = db.newDocument();
        Document unchangedDocument = db.newDocument();

        AixmDiff.compareMessages(baseDocument,
                                 inputDocument,
                                 XPATH_DESIGNATOR,
                                 nsCtx,
                                 newDocument,
                                 deletedDocument,
                                 updatedDocument,
                                 unchangedDocument);

        // now check the results
        fReader      = new FileReader("var/empty.aixm51");
        fSource      = new InputSource(fReader);
        Document     emptyDocument = db.parse(fSource);

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);

        Diff diff = new Diff(emptyDocument, newDocument);
        assertTrue(diff.identical());

        diff = new Diff(emptyDocument, deletedDocument);
        assertTrue(!diff.identical());
        assertEquals(45, numFeatures(deletedDocument, nsCtx));

        diff = new Diff(emptyDocument, updatedDocument);
        assertTrue(!diff.identical());
        assertEquals(1, numFeatures(updatedDocument, nsCtx));

        diff = new Diff(emptyDocument, unchangedDocument);
        assertTrue(!diff.identical());
        assertEquals(1, numFeatures(unchangedDocument, nsCtx));
    }

    /**
     * Tell the number of AIXM features in an AIXM document.
     *
     * @param document the AIXM document.
     * @param nsContext the namespace context to use
     * @return the number of AIXM features in the document.
     */
    private int numFeatures(Document        document,
                             NamespaceContext nsContext) {
        // get the name & designator
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(nsContext);

            NodeList nodes = (NodeList) xpath.evaluate(
                          "/message:AIXMBasicMessage/message:hasMember",
                          document,
                          XPathConstants.NODESET);

            return nodes.getLength();

        } catch (XPathExpressionException e) {
            return 0;
        }
    }
}
