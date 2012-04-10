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

import hu.tyrell.openaviationmap.model.oam.Member;
import hu.tyrell.openaviationmap.model.oam.Oam;
import hu.tyrell.openaviationmap.model.oam.OsmBaseNode;
import hu.tyrell.openaviationmap.model.oam.OsmNode;
import hu.tyrell.openaviationmap.model.oam.Relation;
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
     * Process a generic OSM node.
     *
     * @param node the node to process
     * @param nodeElement the XML node to put the info into.
     */
    private void processOsmBaseNode(OsmBaseNode         node,
                                    Element             nodeElement) {

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
            Element tagElement = nodeElement.getOwnerDocument()
                                                    .createElement("tag");
            tagElement.setAttribute("k", tag);
            String tagValue = node.getTags().get(tag);
            if (tagValue.length() > 255) {
                tagValue = tagValue.substring(0, 255);
            }
            tagElement.setAttribute("v", tagValue);
            nodeElement.appendChild(tagElement);
        }
    }

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

        processOsmBaseNode(node, nodeElement);

        nodeElement.setAttribute("lat", Double.toString(node.getLatitude()));
        nodeElement.setAttribute("lon", Double.toString(node.getLongitude()));

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

        processOsmBaseNode(way, wayElement);

        for (Integer ref : way.getNodeList()) {
            Element nd = document.createElement("nd");
            nd.setAttribute("ref", Integer.toString(ref));
            wayElement.appendChild(nd);
        }

        fragment.appendChild(wayElement);
    }

    /**
     * Process a relation.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param rel the relation to process
     */
    private void processRelation(Document            document,
                                 DocumentFragment    fragment,
                                 Relation            rel) {

        Element relElement = document.createElement("relation");

        processOsmBaseNode(rel, relElement);

        for (Member member : rel.getMembers()) {
            Element m = document.createElement("member");

            switch (member.getType()) {
            case NODE:
                m.setAttribute("type", "node");
                break;

            case WAY:
                m.setAttribute("type", "way");
                break;

            default:
            }

            m.setAttribute("ref", Integer.toString(member.getRef()));
            m.setAttribute("role", member.getRole());

            relElement.appendChild(m);
        }

        fragment.appendChild(relElement);
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
        DocumentFragment relFragment  = document.createDocumentFragment();

        // add all nodes into the node fragment
        for (OsmNode node : oam.getNodes().values()) {
            processNode(document, nodeFragment, node);
        }

        // add all ways into the way fragment
        for (Way way : oam.getWays().values()) {
            processWay(document, wayFragment, way);
        }

        // add all relations into the rel fragment
        for (Relation rel : oam.getRelations().values()) {
            processRelation(document, relFragment, rel);
        }

        // put it all together into a document
        Element root = document.createElement("osm");
        root.setAttribute("version", "0.6");
        document.appendChild(root);
        root.appendChild(nodeFragment);
        root.appendChild(wayFragment);
        root.appendChild(relFragment);

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
