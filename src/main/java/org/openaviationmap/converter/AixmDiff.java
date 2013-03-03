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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to compare AIXM documents.
 */
public final class AixmDiff {
    /**
     * Private default constructor.
     */
    private AixmDiff() {
    }

    /**
     * Compare a baseline AIXM message to another, 'input' message, and output
     * the differences between them.
     *
     * @param baseDocument the baseline to compare against
     * @param inputDocument the input to compare the baseline to
     * @param idPath an XPath statement that identifies the part of each AIXM
     *            feature that serves as the unique identification of that
     *            feature
     * @param nsContext the namespace context to use for the supplied XPath
     *            statement, to resolve XML namespaces
     * @param newDocument an AIXM message containing content that is not in the
     *            baseline, but is present in the input message
     * @param deletedDocument an AXIM message containing content that is in the
     *            baseline, but not in the input
     * @param updatedDocument an AIXM message containing content that is present
     *            both in the baseline and in the input, but has changed
     * @param unchangedDocument an AIXM message containing content that is both
     *            present in the baseline and the input, and is in fact the
     *            same.
     */
    static void compareMessages(Document           baseDocument,
                                 Document           inputDocument,
                                 String             idPath,
                                 NamespaceContext   nsContext,
                                 Document           newDocument,
                                 Document           deletedDocument,
                                 Document           updatedDocument,
                                 Document           unchangedDocument) {

        ConverterUtil.canonizeNS(baseDocument, nsContext);
        normalizeIds(baseDocument, nsContext);

        ConverterUtil.canonizeNS(inputDocument, nsContext);
        normalizeIds(inputDocument, nsContext);

        Map<String, Node> baseMap = new HashMap<String, Node>();
        Map<String, Node> inputMap = new HashMap<String, Node>();

        // create a map of key-node pairs, based on the id XPath
        // specified, both for base & input
        messageToMap(baseDocument, idPath, nsContext, baseMap);
        messageToMap(inputDocument, idPath, nsContext, inputMap);

        Node newMessageNode = getMessageNode(newDocument);
        Node deletedMessageNode = getMessageNode(deletedDocument);
        Node updatedMessageNode = getMessageNode(updatedDocument);
        Node unchangedMessageNode = getMessageNode(unchangedDocument);

        TreeSet<String> sortedKeySet = new TreeSet<String>(baseMap.keySet());

        // now compare the input against base
        for (String key : sortedKeySet) {
            Node baseFeature = baseMap.get(key);

            if (inputMap.containsKey(key)) {
                Node feature = inputMap.get(key);

                // compare the two feature nodes
                NodeDiffIdOk diff = new NodeDiffIdOk(baseFeature, feature);
                if (diff.similar()) {
                    Node clone = unchangedDocument.importNode(feature, true);
                    unchangedMessageNode.appendChild(clone);
                } else {
                    Node clone = updatedDocument.importNode(feature, true);
                    updatedMessageNode.appendChild(clone);
                }
            } else {
                Node clone = deletedDocument.importNode(baseFeature, true);
                deletedMessageNode.appendChild(clone);
            }
        }

        sortedKeySet = new TreeSet<String>(inputMap.keySet());

        for (String key : sortedKeySet) {
            Node feature = inputMap.get(key);

            if (!baseMap.containsKey(key)) {
                Node clone = newDocument.importNode(feature, true);
                newMessageNode.appendChild(clone);
            }
        }

        copyBoundedBy(baseDocument, newDocument, nsContext);
        copyBoundedBy(baseDocument, deletedDocument, nsContext);
        copyBoundedBy(baseDocument, updatedDocument, nsContext);
        copyBoundedBy(baseDocument, unchangedDocument, nsContext);

        newDocument.normalizeDocument();
        deletedDocument.normalizeDocument();
        updatedDocument.normalizeDocument();
        unchangedDocument.normalizeDocument();

        ConverterUtil.canonizeNS(newDocument, nsContext);
        ConverterUtil.canonizeNS(deletedDocument, nsContext);
        ConverterUtil.canonizeNS(updatedDocument, nsContext);
        ConverterUtil.canonizeNS(unchangedDocument, nsContext);

        normalizeIds(newDocument, nsContext);
        normalizeIds(deletedDocument, nsContext);
        normalizeIds(updatedDocument, nsContext);
        normalizeIds(unchangedDocument, nsContext);
    }

    /**
     * Convert an AIXM message document into a map of AIXM features, keyed by a
     * specific part of each feature.
     *
     * @param message the AIXM message to convert
     * @param idPath the XPath statement identifying the 'key' in each message
     * @param nsContext the namespace context to use for the supplied XPath
     *            statement, to resolve XML namespaces
     * @param featureMap a map that will containing all features that have an
     *            'id' as specified by idPath, keyed by this id.
     */
    static void messageToMap(Document           message,
                              String             idPath,
                              NamespaceContext   nsContext,
                              Map<String, Node>  featureMap) {

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);

        Element root = message.getDocumentElement();

        if (!"AIXMBasicMessage".equals(root.getLocalName())) {
            return;
        }

        NodeList members;

        try {
            members = (NodeList) xpath.evaluate(
                    "/message:AIXMBasicMessage/message:hasMember", root,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return;
        }

        for (int i = 0; i < members.getLength(); ++i) {
            Node feature = members.item(i);

            String key;

            try {
                xpath.reset();
                key = xpath.evaluate(idPath, feature).trim();
            } catch (XPathExpressionException e) {
                continue;
            }

            if (!key.isEmpty()) {
                featureMap.put(key, feature);
            }
        }
    }

    /**
     * Get, or if necessary, create an AIXMMessage root node for a document.
     *
     * @param document the document to create the node for.
     * @return the AIXMMessage node of the document.
     */
    static Node getMessageNode(Document document) {
        Node root = document.getDocumentElement();
        if (root != null) {
            throw new RuntimeException("TODO: handle non-empty documents");
        }

        Element message = document.createElementNS(
                AixmConverter.AIXM_MESSAGE_NS_URI,
                AixmConverter.AIXM_MESSAGE_NS_PREFIX + ":AIXMBasicMessage");
        message.setAttributeNS(AixmConverter.GML_NS_URI,
                AixmConverter.GML_NS_PREFIX + ":id", "foo");

        document.appendChild(message);

        return message;
    }

    /**
     * Replace gml:id elements with the XPath values of the elements themselves.
     *
     * @param document the document to process
     * @param nsCtx the namespace context to use to determine namespace
     *            prefixes. if null, a 'standard' namespace context is used
     */
    public static void
    normalizeIds(Document document, NamespaceContext nsCtx) {
        NamespaceContext ctx = nsCtx;
        String gmlPrefix = nsCtx.getPrefix(AixmConverter.GML_NS_URI);
        if (gmlPrefix == null) {
            ctx = AixmConverter.getNsCtx();
            gmlPrefix = ctx.getPrefix(AixmConverter.GML_NS_URI);
        }
        String gmlIdAttrName = gmlPrefix + ":id";

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(ctx);

        Node root = document.getDocumentElement();
        NodeList ids;

        try {
            ids = (NodeList) xpath.evaluate("//@" + gmlIdAttrName, root,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return;
        }

        for (int i = 0; i < ids.getLength(); ++i) {
            Attr id = (Attr) ids.item(i);

            id.setNodeValue(xpathForNode(id.getOwnerElement()));
        }
    }

    /**
     * Create an XPath statement for a particular DOM node.
     *
     * @param node the DOM node to create the statement for
     * @return an XPath statement pointing at the node.
     */
    public static String xpathForNode(Node node) {
        StringBuffer strb = new StringBuffer();

        Node docElement = node.getOwnerDocument();
        Node n = node;
        do {
            // create a string: "/prefix:nodename[ix]"
            StringBuffer b = new StringBuffer();
            b.append("/");
            b.append(n.getNodeName());

            // now get the index
            int ix = 1;
            for (Node sibling = n.getPreviousSibling();
                 sibling != null;
                 sibling = sibling.getPreviousSibling()) {

                if (sibling.getNodeType() == Node.ELEMENT_NODE
                 && n.getNodeName().equals(sibling.getNodeName())) {

                    ++ix;
                }
            }
            b.append("[").append(ix).append("]");

            // prepend the generated string to the full statement
            strb.insert(0, b);

            n = n.getParentNode();
        } while (n != docElement);

        return strb.toString();
    }

    /**
     * Copy the gml:boundedBy element from an AIXM message document to another
     * one.
     *
     * @param baseDocument the document to copy from
     * @param targetDocument the document to copy to
     * @param nsCtx the namespace context to use for both documents
     */
    static void copyBoundedBy(Document          baseDocument,
                               Document          targetDocument,
                               NamespaceContext  nsCtx) {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(nsCtx);

            Node boundedBy = (Node) xpath.evaluate(
                    "/message:AIXMBasicMessage/gml:boundedBy",
                    baseDocument.getDocumentElement(), XPathConstants.NODE);

            if (boundedBy == null) {
                return;
            }

            Node clone = targetDocument.importNode(boundedBy, true);
            targetDocument.getDocumentElement().insertBefore(clone,
                    targetDocument.getDocumentElement().getFirstChild());
        } catch (XPathException e) {
            // just don't copy the node
            return;
        }
    }

    /**
     * Re-order features in an AIXM message document, based on the ordering of a
     * specific child element of each feature.
     *
     * @param document the AIXM message to reorder
     * @param idPath an XPath statement that identifies the part of each AIXM
     *            feature that serves as the unique identification of that
     *            feature. the ordering of this part will be used to re-order
     *            the document
     * @param nsContext the namespace context to use for the supplied XPath
     *            statement, to resolve XML namespaces
     */
    static void reorderMessage(Document            document,
                                String              idPath,
                                NamespaceContext    nsContext) {

        Map<String, Node> idMap = new HashMap<String, Node>();

        // create a map of key-node pairs, based on the id XPath
        // specified, both for base & input
        messageToMap(document, idPath, nsContext, idMap);

        TreeSet<String> sortedKeySet = new TreeSet<String>(idMap.keySet());
        DocumentFragment fragment = document.createDocumentFragment();
        Element message = document.getDocumentElement();

        // now compare the input against base
        for (String key : sortedKeySet) {
            Node feature = idMap.get(key);

            fragment.appendChild(feature);
        }

        message.appendChild(fragment);
    }
}
