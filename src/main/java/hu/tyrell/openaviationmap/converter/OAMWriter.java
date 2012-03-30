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

import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Way;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * A class that writes aviation data in the Open Aviation Map format.
 */
public class OAMWriter {
    /**
     * The date format used to write timestamps.
     */
    private static DateFormat df =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Process a node.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param node the node to process
     */
    private void processNode(Document            document,
                             DocumentFragment    fragment,
                             OsmNode             node) {

        Element nodeElement = document.createElement("node");

        nodeElement.setAttribute("id", Integer.toString(node.getId()));
        nodeElement.setAttribute("version",
                                 Integer.toString(node.getVersion()));
        if (node.getAction() != null) {
            switch (node.getAction()) {
            default:
            case NONE:
                break;

            case CREATE:
                nodeElement.setAttribute("action", "create");
                break;

            case MODIFY:
                nodeElement.setAttribute("action", "modify");
                break;

            case DELETE:
                nodeElement.setAttribute("action", "delete");
                break;
            }
        }

        nodeElement.setAttribute("lat",
                                 Double.toString(node.getLatitude()));
        nodeElement.setAttribute("lon",
                                  Double.toString(node.getLongitude()));

        if (node.getTimestamp() != null) {
            nodeElement.setAttribute("timestamp",
                                     df.format(node.getTimestamp()));
        }

        if (node.getUid() != null) {
            nodeElement.setAttribute("uid", Integer.toString(node.getUid()));
        }

        if (node.getUser() != null) {
            nodeElement.setAttribute("user", node.getUser());
        }

        if (node.isVisible() != null) {
            nodeElement.setAttribute("visible", node.isVisible()
                                              ? "true" : "false");
        }

        if (node.getChangeset() != null) {
            nodeElement.setAttribute("changeset",
                                     Integer.toString(node.getChangeset()));
        }

        // insert the node tags
        for (String tag : node.getTags().keySet()) {
            Element tagElement = document.createElement("tag");
            tagElement.setAttribute("k", tag);
            tagElement.setAttribute("v", node.getTags().get(tag));
            nodeElement.appendChild(tagElement);
        }

        fragment.appendChild(nodeElement);
    }

    /**
     * Process a way.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param way the way to process
     */
    private void processWay(Document            document,
                            DocumentFragment    fragment,
                            Way                 way) {

        Element wayElement = document.createElement("way");

        wayElement.setAttribute("id", Integer.toString(way.getId()));
        wayElement.setAttribute("version",
                                Integer.toString(way.getVersion()));

        if (way.getAction() != null) {
            switch (way.getAction()) {
            default:
            case NONE:
                break;

            case CREATE:
                wayElement.setAttribute("action", "create");
                break;

            case MODIFY:
                wayElement.setAttribute("action", "modify");
                break;

            case DELETE:
                wayElement.setAttribute("action", "delete");
                break;
            }
        }

        if (way.getTimestamp() != null) {
            wayElement.setAttribute("timestamp",
                                     df.format(way.getTimestamp()));
        }

        if (way.getUid() != null) {
            wayElement.setAttribute("uid", Integer.toString(way.getUid()));
        }

        if (way.getUser() != null) {
            wayElement.setAttribute("user", way.getUser());
        }

        if (way.isVisible() != null) {
            wayElement.setAttribute("visible", way.isVisible()
                                              ? "true" : "false");
        }

        if (way.getChangeset() != null) {
            wayElement.setAttribute("changeset",
                                     Integer.toString(way.getChangeset()));
        }

        for (Integer ref : way.getNodeList()) {
            Element nd = document.createElement("nd");
            nd.setAttribute("ref", Integer.toString(ref));
            wayElement.appendChild(nd);
        }

        // insert the way tags
        for (String tag : way.getTags().keySet()) {
            Element tagElement = document.createElement("tag");
            tagElement.setAttribute("k", tag);
            tagElement.setAttribute("v", way.getTags().get(tag));
            wayElement.appendChild(tagElement);

        }

        fragment.appendChild(wayElement);
    }

    /**
     * Create a DOM node that represents a list of ways, and can be written
     * into an OAM file.
     *
     * @param document the DOM document to create the document fragment for
     * @param oam the Oam object to create the XML for
     * @return the document that represents the supplied ways
     */
    public Document processOam(Document               document,
                               Oam                    oam) {

        DocumentFragment nodeFragment = document.createDocumentFragment();
        DocumentFragment wayFragment  = document.createDocumentFragment();

        // add all nodes into the node fragment
        for (OsmNode node : oam.getNodes().values()) {
            processNode(document, nodeFragment, node);
        }

        // add all ways into the way fragment
        for (Way way: oam.getWays().values()) {
            processWay(document, wayFragment, way);
        }

        // put it all together into a document
        Element root = document.createElement("osm");
        root.setAttribute("version", "0.6");
        document.appendChild(root);
        root.appendChild(nodeFragment);
        root.appendChild(wayFragment);

        return document;
    }

    /**
     * Write an OAM object.
     *
     * @param oam the OAM to write.
     * @param writer the writer to write to.
     * @throws ParserConfigurationException on XML parser configuration issues
     * @throws TransformerException on XML transformation issues
     */
    public static void write(Oam oam, Writer writer)
                                throws ParserConfigurationException,
                                       TransformerException {

        DocumentBuilderFactory dbf  = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db   = dbf.newDocumentBuilder();
        Document               d    = db.newDocument();
        OAMWriter         oamWriter = new OAMWriter();

        d = oamWriter.processOam(d, oam);

        // write the XML document into a file
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
    }
}
