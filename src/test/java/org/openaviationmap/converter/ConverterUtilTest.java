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
package org.openaviationmap.converter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to test the ConverterUtil class.
 */
public class ConverterUtilTest {
    /**
     * Test XML namespace canonization.
     *
     * @throws ParserConfigurationException on XML parser configurations
     * @throws IOException on I/O errprs
     * @throws SAXException on SAX errors
     */
    @Test
    public void testCanonizeNS() throws ParserConfigurationException,
                                          SAXException,
                                          IOException {
        String inputStr = "<root><e1 xmlns:foo='http://foo/'>"
                        + "<foo:e2 foo:ize='hose'>bar</foo:e2>"
                        + "</e1></root>";

        NamespaceContextImpl nsCtx = new NamespaceContextImpl();
        nsCtx.startPrefixMapping("bar", "http://foo/");


        DocumentBuilderFactory dbf   = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder        db    = dbf.newDocumentBuilder();
        Document               input = db.parse(new InputSource(
                                                  new StringReader(inputStr)));

        ConverterUtil.canonizeNS(input, nsCtx);

        String checkStr = "<root xmlns:bar='http://foo/'><e1>"
                        + "<bar:e2 bar:ize='hose'>bar</bar:e2>"
                        + "</e1></root>";

        Document check = db.parse(new InputSource(new StringReader(checkStr)));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(input, check);
        assertTrue(diff.identical());
    }

    /**
     * Test XML namespace canonization with a default namespace.
     *
     * @throws ParserConfigurationException on XML parser configurations
     * @throws IOException on I/O errprs
     * @throws SAXException on SAX errors
     */
    @Test
    public void testCanonizeDefaultNS() throws ParserConfigurationException,
                                                 SAXException,
                                                 IOException {
        String inputStr = "<root xmlns='http://bar/'>"
                        + "<e1 xmlns:foo='http://foo/'>"
                        + "<foo:e2 foo:ize='hose'>bar</foo:e2>"
                        + "</e1></root>";

        NamespaceContextImpl nsCtx = new NamespaceContextImpl();
        nsCtx.startPrefixMapping("foo", "http://foo/");
        nsCtx.startPrefixMapping("bar", "http://bar/");


        DocumentBuilderFactory dbf   = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder        db    = dbf.newDocumentBuilder();
        Document               input = db.parse(new InputSource(
                                                  new StringReader(inputStr)));

        ConverterUtil.canonizeNS(input, nsCtx);

        String checkStr =
                "<bar:root xmlns:bar='http://bar/' xmlns:foo='http://foo/'>"
              + "<bar:e1>"
              + "<foo:e2 foo:ize='hose'>bar</foo:e2>"
              + "</bar:e1></bar:root>";

        Document check = db.parse(new InputSource(new StringReader(checkStr)));

        XMLUnit.setXSLTVersion("2.0");
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(input, check);
        assertTrue(diff.identical());
    }
}
