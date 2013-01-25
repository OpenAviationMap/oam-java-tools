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
package hu.tyrell.openaviationmap.converter.eaip;

import hu.tyrell.openaviationmap.converter.ParseException;
import hu.tyrell.openaviationmap.model.Aerodrome;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * eAIP processor for the ENR-4.4 segment of an eAIP.
 */
public class EAipProcessorEnr44 extends EAipProcessor {
    /**
     *  Process a navaid definition from the aAIP.
     *
     *  @param navaidNode the XML node that represents the navaid
     *  @return a navaid described by the node
     *  @throws ParseException on input parsing errors.
     */
    Navaid processNavaid(Node        navaidNode) throws ParseException {

        try {
            Navaid navaid = new Navaid();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the id
            String id = navaidNode.getAttributes().getNamedItem("id")
                         .getNodeValue();
            navaid.setId(id);

            // set the type
            navaid.setType(Navaid.Type.DESIGNATED);

            // get the ident
            xpath.reset();
            String str = xpath.evaluate("Designated-point-ident", navaidNode)
                                                                        .trim();
            if (str != null && !str.isEmpty()) {
                navaid.setIdent(str);
                // set the same for the name
                navaid.setName(str);
            }

            // get the latitude
            xpath.reset();
            str = xpath.evaluate("Latitude", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setLatitude(processLat(id, str));
            }

            // get the longitude
            xpath.reset();
            str = xpath.evaluate("Longitude", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setLongitude(processLon(id, str));
            }


            return navaid;

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(navaidNode, e);
        }
    }

    /**
     *  Process an eAIP file.
     *
     *  @param eAipNode the document node of an eAIP file
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @param airspaces all airspaces extracted from the supplied eAIP file
     *         will be inserted into this list.
     *  @param navaids the navaids that are contained in the eAIP file
     *         will be inserted into this list.
     *  @param aerodromes the aerodromes that are contained contained in the
     *         eAIP file will be put into this list
     *  @param errors all parsing errors will be written to this list
     */
    @Override
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<Navaid>            navaids,
                            List<Aerodrome>         aerodromes,
                            List<ParseException>    errors) {

        NodeList nodes = null;

        // get the designated point nodes
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            nodes = (NodeList) xpath.evaluate(
                    "//Designated-point-table/Designated-point",
                    eAipNode, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(e));
        }

        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.getLength(); ++i) {
            try {
                Navaid navaid = processNavaid(nodes.item(i));
                navaids.add(navaid);
            } catch (ParseException e) {
                errors.add(e);
                continue;
            }
        }
    }

}
