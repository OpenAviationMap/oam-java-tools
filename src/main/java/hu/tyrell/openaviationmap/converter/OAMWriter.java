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

import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.UOM;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A class that writes aviation data in the Open Aviation Map format.
 */
public class OAMWriter {
    /**
     * Create a DOM node based on a Point.
     *
     * @param document the DOM document to create the document node for
     * @param point to point to create the node from
     * @param nodeId the unique id of the node
     * @param create generate this element in OSM create mode
     * @param version the version of the node
     * @return a DOM node representing the point
     */
    private Node processPoint(Document  document,
                              Point     point,
                              int       nodeId,
                              boolean   create,
                              int       version) {
        Element node = document.createElement("node");

        node.setAttribute("id",
                          Integer.toString(create ? -nodeId : nodeId));
        node.setAttribute("version", Integer.toString(version));
        if (create) {
            node.setAttribute("action", "create");
        }
        node.setAttribute("lat", Double.toString(point.getLatitude()));
        node.setAttribute("lon", Double.toString(point.getLongitude()));

        return node;
    }

    /**
     * Process a Circle boundary object, by turning it into a series of node
     * elements.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param circle the circle to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param create generate this element in create mode
     * @param version the version of the node
     * @return the highest used node id
     */
    private int processCircle(Document            document,
                              DocumentFragment    fragment,
                              Circle              circle,
                              int                 nodeIdIx,
                              boolean             create,
                              int                 version) {

        int nodeIx = nodeIdIx + 1;

        double radiusInNm  = circle.getRadius().inUom(UOM.NM).getDistance();
        double radiusInDeg = radiusInNm / 60.0;
        double radiusLat   = radiusInDeg;
        double radiusLon   = radiusInDeg / Math.cos(
                              Math.toRadians(circle.getCenter().getLatitude()));

        // FIXME: calculate number of points on some required precision metric
        int totalPoints = 32;
        double tpHalf = totalPoints / 2.0;
        for (int i = 0; i < totalPoints; ++i) {
            double theta = Math.PI * i / tpHalf;
            double x = circle.getCenter().getLongitude()
                    + (radiusLon * Math.cos(theta));
            double y = circle.getCenter().getLatitude()
                    + (radiusLat * Math.sin(theta));

            Point point = new Point();
            point.setLongitude(x);
            point.setLatitude(y);

            fragment.appendChild(processPoint(document,
                                              point,
                                              nodeIx++,
                                              create,
                                              version));
        }

        return nodeIx;
    }

    /**
     * Process a Ring boundary object, by turning it into a series of node
     * elements.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param ring the ring to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param create generate this element in create mode
     * @param version the version of the node
     * @return the highest used node id
     */
    private int processRing(Document            document,
                            DocumentFragment    fragment,
                            Ring                ring,
                            int                 nodeIdIx,
                            boolean             create,
                            int                 version) {

        int nodeIx = nodeIdIx + 1;

        // omit the last node, as it will be a duplicate of the first one
        for (int i = 0; i < ring.getPointList().size() - 1; ++i) {
            Point point = ring.getPointList().get(i);

            fragment.appendChild(processPoint(document,
                                              point, nodeIx++,
                                              create, version));
        }

        return nodeIx;
    }

    /**
     * Process a Boundary object, by turning it into a series of node
     * elements.
     *
     * @param document the document that will contain the nodes
     * @param fragment the document fragment to put the nodes into
     * @param boundary the boundary to process
     * @param nodeIdIx the last valid node id used, only ids above
     *        this one will be used for node ids
     * @param create generate this element in create mode
     * @param version the version of the node
     * @return the highest used node id
     */
    private int processBoundary(Document            document,
                                DocumentFragment    fragment,
                                Boundary            boundary,
                                int                 nodeIdIx,
                                boolean             create,
                                int                 version) {

        switch (boundary.getType()) {
        case RING:
            return processRing(document,
                               fragment,
                               (Ring) boundary,
                               nodeIdIx,
                               create,
                               version);

        case CIRCLE:
            return processCircle(document, fragment,
                                 (Circle) boundary, nodeIdIx,
                                 create, version);

        default:
            return nodeIdIx;
        }
    }

    /**
     * Create a DOM node that represents a list of airspaces, and can be written
     * into an OAM file.
     *
     * @param document the DOM document to create the document fragment for
     * @param airspaces the airspaces to convert
     * @param nodeIdIx the minimal index of unique node ids. it is assumed
     *        that any id above this index can be given to OAM nodes
     * @param wayIdIx the minimal index of unique way ids. it is assumed
     *        that any id above this index can be given to OAM ways
     * @param create if true, generate all elements in OSM create mode,
     *        that is, negative ids and action="create"
     * @param version the version number to put for all ids
     * @return the document that represents the supplied airspaces
     */
    public Document processAirspaces(Document       document,
                                     List<Airspace> airspaces,
                                     int            nodeIdIx,
                                     int            wayIdIx,
                                     boolean        create,
                                     int            version) {

        DocumentFragment nodeFragment = document.createDocumentFragment();
        DocumentFragment wayFragment = document.createDocumentFragment();

        int nIdIx = nodeIdIx;
        int wIdIx = wayIdIx;

        for (Airspace airspace : airspaces) {

            // create a node for each point in the airspace ring boundary
            int minIx  = nIdIx + 1;
            int nodeIx = processBoundary(document, nodeFragment,
                                         airspace.getBoundary(), nIdIx,
                                         create, version);

            // create a 'way' element for the airspace itself
            ++wIdIx;
            Element way = document.createElement("way");
            way.setAttribute("id",
                             Integer.toString(create ? -wIdIx : wIdIx));
            way.setAttribute("version", Integer.toString(version));
            if (create) {
                way.setAttribute("action", "create");
            }

            // insert the node references
            for (int i = minIx; i < nodeIx; ++i) {
                Element nd = document.createElement("nd");
                nd.setAttribute("ref",
                                Integer.toString(create ? -i : i));
                way.appendChild(nd);
            }
            // insert the first node reference again to close the path
            Element nd = document.createElement("nd");
            nd.setAttribute("ref",
                            Integer.toString(create ? -minIx : minIx));
            way.appendChild(nd);


            nIdIx = nodeIx;

            // insert the airspace metadata
            Element tag = document.createElement("tag");
            tag.setAttribute("k", "airspace");
            tag.setAttribute("v", "yes");
            way.appendChild(tag);

            if (airspace.getDesignator() != null
             && !airspace.getDesignator().isEmpty()) {

                tag = document.createElement("tag");
                tag.setAttribute("k", "icao");
                tag.setAttribute("v", airspace.getDesignator());
                way.appendChild(tag);
            }

            if (airspace.getName() != null && !airspace.getName().isEmpty()) {
                tag = document.createElement("tag");
                tag.setAttribute("k", "name");
                tag.setAttribute("v", airspace.getName());
                way.appendChild(tag);
            }

            if (airspace.getRemarks() != null
             && !airspace.getRemarks().isEmpty()) {
                tag = document.createElement("tag");
                tag.setAttribute("k", "longname");
                tag.setAttribute("v", airspace.getRemarks());
                way.appendChild(tag);
            }

            if (airspace.getType() != null) {
                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:type");
                tag.setAttribute("v", airspace.getType());
                way.appendChild(tag);
            }

            if (airspace.getAirspaceClass() != null) {
                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:class");
                tag.setAttribute("v", airspace.getAirspaceClass());
                way.appendChild(tag);
            }

            addElevationLimits(document, airspace, way);

            if (airspace.getBoundary().getType() == Boundary.Type.CIRCLE) {
                Circle c = (Circle) airspace.getBoundary();

                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:center:lat");
                tag.setAttribute("v",
                        Double.toString(c.getCenter().getLatitude()));
                way.appendChild(tag);

                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:center:lon");
                tag.setAttribute("v",
                        Double.toString(c.getCenter().getLongitude()));
                way.appendChild(tag);

                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:radius");
                tag.setAttribute("v",
                        Double.toString(c.getRadius().getDistance()));
                way.appendChild(tag);

                tag = document.createElement("tag");
                tag.setAttribute("k", "airspace:radius:unit");
                tag.setAttribute("v", c.getRadius().getUom().toString());
                way.appendChild(tag);
            }

            wayFragment.appendChild(way);
        }

        Element root = document.createElement("osm");
        root.setAttribute("version", "0.6");
        document.appendChild(root);
        root.appendChild(nodeFragment);
        root.appendChild(wayFragment);

        return document;
    }

    /**
     * Add OAM tags related to elevation limits.
     *
     * @param document the document to add the tags to
     * @param airspace the airspace the limits are about
     * @param way the OAM way to add the limits to.
     */
    private void addElevationLimits(Document document,
                                    Airspace airspace,
                                    Element  way) {
        Element tag;

        if (airspace.getLowerLimit() != null) {
            Elevation elevation = airspace.getLowerLimit();

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:lower");
            tag.setAttribute("v",
                    Integer.toString((int) elevation.getElevation()));
            way.appendChild(tag);

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:lower:unit");
            tag.setAttribute("v",
                    elevation.getUom().toString().toLowerCase());
            way.appendChild(tag);

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:lower:class");
            switch (elevation.getReference()) {
            default:
            case MSL:
                tag.setAttribute("v", "amsl");
                break;
            case SFC:
                tag.setAttribute("v", "agl");
                break;
            }
            way.appendChild(tag);
        }

        if (airspace.getUpperLimit() != null) {
            Elevation elevation = airspace.getUpperLimit();

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:upper");
            tag.setAttribute("v",
                    Integer.toString((int) elevation.getElevation()));
            way.appendChild(tag);

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:upper:unit");
            tag.setAttribute("v",
                    elevation.getUom().toString().toLowerCase());
            way.appendChild(tag);

            tag = document.createElement("tag");
            tag.setAttribute("k", "height:upper:class");
            switch (elevation.getReference()) {
            default:
            case MSL:
                tag.setAttribute("v", "amsl");
                break;
            case SFC:
                tag.setAttribute("v", "agl");
                break;
            }
            way.appendChild(tag);
        }

    }

}
