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

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.ElementNameQualifier;
import org.custommonkey.xmlunit.ElementQualifier;
import org.custommonkey.xmlunit.MatchTracker;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Compares and describes any difference between XML documents, based on
 * XmlUnit's on Diff class.
 *
 * This class will treat documents identical the same as XmlUnit's Diff,
 * except it will also treat them identical if the gml:id attributes are
 * different.
 *
 * This class also allows for comparing node trees, as opposed to complete
 * documents.
 */
public class NodeDiffIdOk implements DifferenceListener, ComparisonController {

    /**
     * The control node.
     */
    private final Node controlNode;

    /**
     * The test node.
     */
    private final Node testNode;

    /**
     * Flag to signal if the two XML documents are similar.
     */
    private boolean similar = true;

    /**
     * Flag to signal if the two XML documents are identical.
     */
    private boolean identical = true;

    /**
     * Flag to signal if the documents have already been compared.
     */
    private boolean compared = false;

    /**
     * Flag to signal if the comparison should be halted.
     */
    private boolean haltComparison = false;

    /**
     * Difference messages.
     */
    private final StringBuffer messages;

    /**
     * The difference engine used for comparison.
     */
    private final DifferenceEngine differenceEngine;

    /**
     * The external difference listener, if any.
     */
    private DifferenceListener  differenceListenerDelegate;

    /**
     * The external element qualifier, if any.
     */
    private ElementQualifier elementQualifierDelegate;

    /**
     * The external match tracker, if any.
     */
    private MatchTracker matchTrackerDelegate;

    /**
     * Construct a Diff that compares the XML in two Documents.
     *
     * @param controlNode the control node.
     * @param testNode the test node.
     */
    public NodeDiffIdOk(Node controlNode, Node testNode) {
        this(controlNode, testNode, (DifferenceEngine) null);
    }

    /**
     * Construct a Diff that compares the XML in two Documents using a specific
     * DifferenceEngine.
     *
     * @param controlNode the control node.
     * @param testNode the test node.
     * @param comparator an external comparator, may be null.
     */
    public NodeDiffIdOk(Node             controlNode,
                 Node             testNode,
                 DifferenceEngine comparator) {
        this(controlNode, testNode, comparator, new ElementNameQualifier());
    }

    /**
     * Construct a Diff that compares the XML in two Documents using a specific
     * DifferenceEngine and ElementQualifier.
     *
     * @param controlNode the control node.
     * @param testNode the test node.
     * @param comparator an external comparator, may be null.
     * @param elementQualifier an external element qualifier, may be null.
     */
    public NodeDiffIdOk(Node               controlNode,
                         Node                testNode,
                         DifferenceEngine    comparator,
                         ElementQualifier    elementQualifier) {
        this.controlNode = controlNode;
        this.testNode    = testNode;
        this.elementQualifierDelegate = elementQualifier;
        this.differenceEngine = comparator;
        this.messages = new StringBuffer();
    }

    /**
     * Construct a Diff from a prototypical instance.
     * Used by extension subclasses
     *
     * @param prototype a prototypical instance
     */
    protected NodeDiffIdOk(NodeDiffIdOk prototype) {
        this(prototype.controlNode,
              prototype.testNode,
              prototype.differenceEngine,
              prototype.elementQualifierDelegate);

        this.differenceListenerDelegate = prototype.differenceListenerDelegate;
    }

    /**
     * Top of the recursive comparison execution tree.
     */
    protected final void compare() {
        if (compared) {
            return;
        }
        getDifferenceEngine().compare(controlNode, testNode, this,
                                      elementQualifierDelegate);
        compared = true;
    }

    /**
     * Return the result of a comparison. Two documents are considered
     * to be "similar" if they contain the same elements and attributes
     * regardless of order.
     *
     * @return true if the two XML node structures are similar.
     */
    public boolean similar() {
        compare();
        return similar;
    }

    /**
     * Return the result of a comparison. Two documents are considered
     * to be "identical" if they contain the same elements and attributes
     * in the same order.
     *
     * @return true if the two XML node structures are identical.
     */
    public boolean identical() {
        compare();
        return identical;
    }

    /**
     * Append a meaningful message to the buffer of messages.
     *
     * @param appendTo the messages buffer
     * @param difference the difference to append info from
     */
    private void appendDifference(StringBuffer appendTo,
                                    Difference difference) {
        appendTo.append(' ').append(difference).append('\n');
    }

    /**
     * DifferenceListener implementation.
     * If the
     * {@link NodeDiffIdOk#overrideDifferenceListener overrideDifferenceListener}
     * method has been called then the interpretation of the difference
     * will be delegated.
     *
     * @param difference the newly encountered difference
     * @return a DifferenceListener.RETURN_... constant indicating how the
     *    difference was interpreted.
     * Always RETURN_ACCEPT_DIFFERENCE if the call is not delegated.
     */
    @Override
    public int differenceFound(Difference difference) {
        int returnValue = RETURN_ACCEPT_DIFFERENCE;
        if (differenceListenerDelegate != null) {
            returnValue = differenceListenerDelegate.differenceFound(
                                                                    difference);
        }

        Node cNode = difference.getControlNodeDetail().getNode();
        Node tNode = difference.getTestNodeDetail().getNode();

        if (AixmConverter.GML_NS_URI.equals(cNode.getNamespaceURI())
         && ("id".equals(cNode.getLocalName())
          || "identifier".equals(cNode.getLocalName()))
         && AixmConverter.GML_NS_URI.equals(tNode.getNamespaceURI())
         && ("id".equals(tNode.getLocalName())
          || "identifier".equals(tNode.getLocalName()))) {

            returnValue = RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;

        } else if (cNode.getNodeType() == Node.ATTRIBUTE_NODE
                 && tNode.getNodeType() == Node.ATTRIBUTE_NODE) {

            Element cOwner = ((Attr) cNode).getOwnerElement();
            Element tOwner = ((Attr) tNode).getOwnerElement();

            if (AixmConverter.GML_NS_URI.equals(cOwner.getNamespaceURI())
             && "identifier".equals(cOwner.getLocalName())
             && AixmConverter.GML_NS_URI.equals(tOwner.getNamespaceURI())
             && "identifier".equals(tOwner.getLocalName())) {

                returnValue = RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            } else if (
                  AixmConverter.XLINK_NS_URI.equals(cNode.getNamespaceURI())
                  && "href".equals(cNode.getLocalName())
                  && AixmConverter.XLINK_NS_URI.equals(tNode.getNamespaceURI())
                  && "href".equals(tNode.getLocalName())) {

                // go after XLinks, and compare the linked content for
                // similarity
                returnValue = compareLinks(cNode.getOwnerDocument(),
                                           cNode.getNodeValue(),
                                           tNode.getOwnerDocument(),
                                           tNode.getNodeValue(),
                                           AixmConverter.getNsCtx());
            }

        } else if (cNode.getNodeType() == Node.TEXT_NODE
                 && tNode.getNodeType() == Node.TEXT_NODE) {

            Node cParent = cNode.getParentNode();
            Node tParent = tNode.getParentNode();

            if (AixmConverter.GML_NS_URI.equals(cParent.getNamespaceURI())
             && "identifier".equals(cParent.getLocalName())
             && AixmConverter.GML_NS_URI.equals(tParent.getNamespaceURI())
             && "identifier".equals(tParent.getLocalName())) {

                returnValue = RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }
        }

        switch (returnValue) {
        case RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL:
            return returnValue;
        case RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR:
            identical = false;
            haltComparison = false;
            break;
        case RETURN_ACCEPT_DIFFERENCE:
            identical = false;
            if (difference.isRecoverable()) {
                haltComparison = false;
            } else {
                similar = false;
                haltComparison = true;
            }
            break;
        case RETURN_UPGRADE_DIFFERENCE_NODES_DIFFERENT:
            identical      = false;
            similar        = false;
            haltComparison = true;
            break;
        default:
            throw new IllegalArgumentException(returnValue
                    + " is not a defined DifferenceListener.RETURN_... value");
        }
        if (haltComparison) {
            messages.append("\n[different]");
        } else {
            messages.append("\n[not identical]");
        }
        appendDifference(messages, difference);

        return returnValue;
    }

    /**
     * Compare two links (XLink hrefs), and tell if the linked elements
     * are similar in content or not.
     *
     * @param cDocument the control document
     * @param cHref the link id in the control document
     * @param tDocument the test document
     * @param tHref the link id in the test document
     * @param nsContext the namespace context to use when working on the
     *         documents
     * @return a return value as per the {@link #differenceFound(Difference)}
     *          function, showing the result.
     */
    private int compareLinks(Document          cDocument,
                              String            cHref,
                              Document          tDocument,
                              String            tHref,
                              NamespaceContext  nsContext) {

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);

        String cId;
        String tId;

        if (cHref.startsWith("#") && tHref.startsWith("#")) {
            cId = cHref.substring(1);
            tId = tHref.substring(1);
        } else {
            return RETURN_ACCEPT_DIFFERENCE;
        }

        try {
            Node cN = (Node) xpath.evaluate(
                    "/message:AIXMBasicMessage/message:hasMember/*[@gml:id='"
                            + cId + "']/..",
                    cDocument,
                    XPathConstants.NODE);

            Node tN = (Node) xpath.evaluate(
                    "/message:AIXMBasicMessage/message:hasMember/*[@gml:id='"
                            + tId + "']/..",
                    tDocument,
                    XPathConstants.NODE);

            NodeDiffIdOk diff = new NodeDiffIdOk(cN, tN);
            if (diff.identical()) {
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            } else if (diff.similar()) {
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            }

        } catch (XPathException e) {
            return RETURN_ACCEPT_DIFFERENCE;
        }

        return RETURN_ACCEPT_DIFFERENCE;
    }

    /**
     * DifferenceListener implementation.
     * If the
     * {@link NodeDiffIdOk#overrideDifferenceListener  overrideDifferenceListener}
     * method has been called then the call will be delegated
     * otherwise a message is printed to <code>System.err</code>.
     *
     * @param control the control node
     * @param test the test node
     */
    @Override
    public void skippedComparison(Node control, Node test) {
        if (differenceListenerDelegate != null) {
            differenceListenerDelegate.skippedComparison(control, test);
        } else {
            System.err.println("DifferenceListener.skippedComparison: "
                               + "unhandled control node type=" + control
                               + ", unhandled test node type=" + test);
        }
    }

    /**
     * ComparisonController implementation.
     *
     * @param afterDifference the difference encountered
     * @return true if the difference is not recoverable and
     * the comparison should be halted, or false if the difference
     * is recoverable and the comparison can continue
     */
    @Override
    public boolean haltComparison(Difference afterDifference) {
        return haltComparison;
    }

    /**
     * Append the message from the result of this Diff instance to a specified
     *  StringBuffer.
     *
     * @param toAppendTo the buffer to append to.
     * @return specified StringBuffer with message appended
     */
    public StringBuffer appendMessage(StringBuffer toAppendTo) {
        compare();
        if (messages.length() == 0) {
            messages.append("[identical]");
        }
        // fix for JDK1.4 backwards incompatibility
        return toAppendTo.append(messages.toString());
    }

    /**
     * Get the result of this Diff instance as a String.
     *
     * @return result of this Diff
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getName());
        appendMessage(buf);
        return buf.toString();
    }

    /**
     * Override the <code>DifferenceListener</code> used to determine how
     * to handle differences that are found.
     * @param delegate the DifferenceListener instance to delegate handling to.
     */
    public void overrideDifferenceListener(DifferenceListener delegate) {
        this.differenceListenerDelegate = delegate;
    }

    /**
     * Override the <code>ElementQualifier</code> used to determine which
     * control and test nodes are comparable for this difference comparison.
     * @param delegate the ElementQualifier instance to delegate to.
     */
    public void overrideElementQualifier(ElementQualifier delegate) {
        this.elementQualifierDelegate = delegate;
    }

    /**
     * Override the <code>MatchTracker</code> used to track
     * successfully matched nodes.
     * @param delegate the MatchTracker instance to delegate handling to.
     */
    public void overrideMatchTracker(MatchTracker delegate) {
        this.matchTrackerDelegate = delegate;
        if (differenceEngine != null) {
            differenceEngine.setMatchTracker(delegate);
        }
    }

    /**
     * Lazily initializes the difference engine if it hasn't been set
     * via a constructor.
     *
     * @return the difference engine used.
     */
    private DifferenceEngine getDifferenceEngine() {
        return differenceEngine == null
            ? new DifferenceEngine(this, matchTrackerDelegate)
            : differenceEngine;
    }

}
