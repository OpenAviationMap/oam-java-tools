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

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * Generic utilities.
 */
public final class ConverterUtil {
    /**
     * Private constructor.
     */
    private ConverterUtil() {
    }

    /**
     * Make all XML namespace declarations canonical. This means that all
     * XML namespace declarations are moved up to the root element, and
     * the prefixes are given the same name.
     *
     * @param document the document to canonize.
     * @param nsCtx preferred namespace prefixes to use during canonizaiton.
     */
    public static void
    canonizeNS(Document document, NamespaceContext nsCtx) {
        DocumentTraversal t  = (DocumentTraversal) document;

        // turn the default namespace explicit, if it is contained in the nsCtx
        NodeIterator it = t.createNodeIterator(document,
                                               NodeFilter.SHOW_ELEMENT,
                                               null,
                                               true);

        for (Node n = it.nextNode(); n != null; n = it.nextNode()) {
            NamedNodeMap attrs = n.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                Node a = attrs.item(i);
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(a.getLocalName())) {
                    String prefix = nsCtx.getPrefix(a.getNodeValue());
                    if (prefix != null) {
                        // remove the default namespace attribute
                        ((Element) n).removeAttributeNS(
                                          XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                          XMLConstants.XMLNS_ATTRIBUTE);
                        --i;

                        // set an explicit prefix for this node, and all child
                        // nodes
                        ((Element) n).setAttributeNS(
                                XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix,
                                a.getNodeValue());

                        n.setPrefix(prefix);

                        NodeIterator nit = t.createNodeIterator(n,
                                                      NodeFilter.SHOW_ELEMENT,
                                                      null, true);
                        for (Node nn = nit.nextNode();
                             nn != null;
                             nn = nit.nextNode()) {

                            if (nn.getPrefix() == null) {
                                nn.setPrefix(prefix);
                            }
                        }
                    }
                }
            }
        }

        // collect the namespaces used in the document
        Map<String, String> nsMap     = new HashMap<String, String>();
        Map<String, String> prefixMap = new HashMap<String, String>();

        it = t.createNodeIterator(document, NodeFilter.SHOW_ELEMENT,
                                  null, true);

        for (Node n = it.nextNode(); n != null; n = it.nextNode()) {
            NamedNodeMap attrs = n.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                Node a = attrs.item(i);
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(a.getPrefix())
                 && !nsMap.containsKey(a.getNodeValue())) {

                    nsMap.put(a.getNodeValue(), a.getLocalName());
                }

                String prefix = nsCtx.getPrefix(a.getNodeValue());
                if (prefix != null) {
                    prefixMap.put(a.getLocalName(), prefix);
                } else {
                    prefix = nsMap.get(a.getNamespaceURI());
                    if (prefix != null && !prefix.equals(a.getLocalName())) {
                        prefixMap.put(a.getLocalName(), prefix);
                    }
                }
            }
        }

        // update the deduced namespace prefix map based on the supplied context
        for (String uri : nsMap.keySet()) {
            String prefix = nsCtx.getPrefix(uri);
            if (prefix != null) {
                nsMap.put(uri, prefix);
            }
        }

        // remove all namespace declaration attributes
        // and update the namespace prefixes
        it = t.createNodeIterator(document, NodeFilter.SHOW_ELEMENT,
                                  null, true);

        for (Node n = it.nextNode(); n != null; n = it.nextNode()) {
            if (n.getPrefix() != null
             && prefixMap.containsKey(n.getPrefix())) {
                n.setPrefix(prefixMap.get(n.getPrefix()));
            }

            NamedNodeMap attrs = n.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                Node a = attrs.item(i);
                if (XMLConstants.XMLNS_ATTRIBUTE.equals(a.getPrefix())) {
                    ((Element) n).removeAttributeNS(
                                            XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                                            a.getLocalName());
                    --i;
                } else if (a.getPrefix() != null
                         && prefixMap.containsKey(a.getPrefix())) {
                    a.setPrefix(prefixMap.get(a.getPrefix()));
                }
            }
        }

        // put all namespace declarations into the root element
        Element root = document.getDocumentElement();
        for (String uri : nsMap.keySet()) {
            root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                         XMLConstants.XMLNS_ATTRIBUTE + ":" + nsMap.get(uri),
                         uri);
        }
    }

    /**
     * Print a DOM document to stdout.
     *
     * @param document the document to print
     */
    public static void printDocument(Document document) {
        serializeDocument(document, System.out);
    }

    /**
     * Serialize a DOM document to an output stream.
     *
     * @param document the document to print
     * @param os the output stream to serialize to.
     */
    public static void
    serializeDocument(Document document, OutputStream os) {
        try {
            // write the XML document into a file
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove all white space text nodes from an DOM tree.
     *
     * @param document the DOM tree to remove all XML white space text nodes
     *         from.
     */
    public static void removeWhitespace(Document document) {
        Set<Node>         toRemove = new HashSet<Node>();
        DocumentTraversal t  = (DocumentTraversal) document;
        NodeIterator      it = t.createNodeIterator(document,
                                                    NodeFilter.SHOW_TEXT,
                                                    null,
                                                    true);

        for (Node n = it.nextNode(); n != null; n = it.nextNode()) {
            if (n.getNodeValue().trim().isEmpty()) {
                toRemove.add(n);
            }
        }

        for (Node n : toRemove) {
            n.getParentNode().removeChild(n);
        }
    }
}
