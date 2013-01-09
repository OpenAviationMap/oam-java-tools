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
package hu.tyrell.openaviationmap.rendering;

import static org.junit.Assert.assertTrue;
import hu.tyrell.openaviationmap.converter.NodeDiffIdOk;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test cases for the ScaleSLD class.
 */
public class ScaleSLDTest {

    /**
     * Helper function to read an XML file into an XML document node.
     *
     * @param input the file to read
     * @return the XML document node corresponding to the content of the input
     * @throws ParserConfigurationException on XML parser config errors
     * @throws SAXException on XML parsing errors
     * @throws IOException on I/O errors
     */
    private Node readXml(Reader input) throws ParserConfigurationException,
                                              SAXException,
                                              IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db  = dbf.newDocumentBuilder();
        InputSource            strSource = new InputSource(input);

        return db.parse(strSource);
    }

    /**
     * Test identity - when no changes are made during scaling.
     *
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation errors
     * @throws RenderException on SLD scaling, rendering issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testIdentity() throws ParserConfigurationException,
                                      SAXException,
                                      IOException,
                                      TransformerException,
                                      XPathExpressionException,
                                      RenderException {
        FileReader      input  = new FileReader("var/proba.sld");
        List<Integer>   scales = new ArrayList<Integer>(0);
        StringWriter    output = new StringWriter();

        ScaleSLD.scaleSld(input, scales, output);

        // now check that the input and output are the same
        Node d  = readXml(new FileReader("var/proba.sld"));
        Node dd = readXml(new StringReader(output.toString()));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        NodeDiffIdOk diff = new NodeDiffIdOk(dd, d);
        assertTrue(diff.similar());
    }

    /**
     * Test when the input document is not an SLD document.
     *
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation errors
     * @throws RenderException on SLD scaling, rendering issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testNotSld() throws ParserConfigurationException,
                                    SAXException,
                                    IOException,
                                    TransformerException,
                                    XPathExpressionException,
                                    RenderException {

        FileReader      input  = new FileReader("var/empty.aixm51");
        List<Integer>   scales = new ArrayList<Integer>(0);
        StringWriter    output = new StringWriter();
        boolean         caught = false;

        try {
            ScaleSLD.scaleSld(input, scales, output);
        } catch (RenderException e) {
            // this is what we expected
            caught = true;
        }

        assertTrue("expected RenderException not caught", caught);
    }

    /**
     * Test when there is one scale value applied. This will create two
     * rule elements for each rule element, split by the single scaling
     * that is provided.
     *
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation errors
     * @throws RenderException on SLD scaling, rendering issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testOneScale() throws ParserConfigurationException,
                                      SAXException,
                                      IOException,
                                      TransformerException,
                                      XPathExpressionException,
                                      RenderException {

        FileReader      input  = new FileReader("var/proba.sld");
        List<Integer>   scales = new ArrayList<Integer>(1);
        StringWriter    output = new StringWriter();

        scales.add(500000);

        ScaleSLD.scaleSld(input, scales, output);

        // now check that the input and output are the same
        Node d  = readXml(new FileReader("var/proba-500000.sld"));
        Node dd = readXml(new StringReader(output.toString()));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        NodeDiffIdOk diff = new NodeDiffIdOk(dd, d);
        assertTrue(diff.similar());
    }

    /**
     * Test when there are two scale values applied.
     *
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation errors
     * @throws RenderException on SLD scaling, rendering issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testTwoScales() throws ParserConfigurationException,
                                       SAXException,
                                       IOException,
                                       TransformerException,
                                       XPathExpressionException,
                                       RenderException {

        FileReader      input  = new FileReader("var/proba.sld");
        List<Integer>   scales = new ArrayList<Integer>(2);
        StringWriter    output = new StringWriter();

        scales.add(250000);
        scales.add(500000);

        ScaleSLD.scaleSld(input, scales, output);

        // now check that the input and output are the same
        Node d  = readXml(new FileReader("var/proba-250000_500000.sld"));
        Node dd = readXml(new StringReader(output.toString()));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        NodeDiffIdOk diff = new NodeDiffIdOk(dd, d);
        assertTrue(diff.similar());
    }

    /**
     * Test when there are four scale values applied.
     *
     * @throws IOException on I/O errors
     * @throws SAXException on XML parsing errors
     * @throws ParserConfigurationException on XML parser configuration errors
     * @throws TransformerException on XML transformation errors
     * @throws RenderException on SLD scaling, rendering issues
     * @throws XPathExpressionException on XPath errors
     */
    @Test
    public void testFourScales() throws ParserConfigurationException,
                                        SAXException,
                                        IOException,
                                        TransformerException,
                                        XPathExpressionException,
                                        RenderException {

        FileReader      input  = new FileReader("var/proba.sld");
        List<Integer>   scales = new ArrayList<Integer>(2);
        StringWriter    output = new StringWriter();

        scales.add(125000);
        scales.add(250000);
        scales.add(500000);
        scales.add(1000000);

        ScaleSLD.scaleSld(input, scales, output);

        // now check that the input and output are the same
        Node d  = readXml(new FileReader(
                                "var/proba-125000_250000_500000_1000000.sld"));
        Node dd = readXml(new StringReader(output.toString()));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        NodeDiffIdOk diff = new NodeDiffIdOk(dd, d);
        assertTrue(diff.similar());
    }
}
