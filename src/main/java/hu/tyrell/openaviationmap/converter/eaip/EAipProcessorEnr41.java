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
package hu.tyrell.openaviationmap.converter.eaip;

import hu.tyrell.openaviationmap.converter.ParseException;
import hu.tyrell.openaviationmap.model.Airspace;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Frequency;
import hu.tyrell.openaviationmap.model.MagneticVariation;
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
 * eAIP processor for the ENR-4.1 segment of an eAIP.
 */
public class EAipProcessorEnr41 extends EAipProcessor {
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

            // get the type
            String str = xpath.evaluate("Navaid-type/text()[1]", navaidNode)
                    .trim();
            if ("DVOR/DME".equals(str)) {
                navaid.setType(Navaid.Type.VORDME);
            } else if ("NDB".equals(str)) {
                navaid.setType(Navaid.Type.NDB);
            } else {
                throw new ParseException(id, "unknown navaid type " + str);
            }

            // get the name
            xpath.reset();
            str = xpath.evaluate("Navaid-name", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setName(str);
            }

            // get the ident
            xpath.reset();
            str = xpath.evaluate("Navaid-ident", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setIdent(str);
            }

            // get the declination
            xpath.reset();
            str = xpath.evaluate("Navaid-declination", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                // remove the trailing degree sign
                if (str.endsWith("\u00b0")) {
                    str = str.substring(0, str.length() - 1);
                }
                navaid.setDeclination(Double.parseDouble(str));
            }

            // get the variation
            xpath.reset();
            str = xpath.evaluate("Navaid-magnetic-variation", navaidNode)
                    .trim();
            if (str != null && !str.isEmpty()) {
                navaid.setVariation(processVariation(str));
            }

            // get the frequency
            xpath.reset();
            str = xpath.evaluate("Navaid-frequency/text()[1]", navaidNode)
                    .trim();
            if (str != null && !str.isEmpty()) {
                navaid.setFrequency(Frequency.fromString(str));
            }

            // get the DME channel, if any
            xpath.reset();
            str = xpath.evaluate("Navaid-frequency/text()[2]", navaidNode)
                    .trim();
            if (str != null && !str.isEmpty()) {
                navaid.setDmeChannel(str);
            }

            // get the active time
            xpath.reset();
            str = xpath.evaluate("Navaid-hours", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setActivetime(str);
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

            // get the elevation
            xpath.reset();
            str = xpath.evaluate("Navaid-elevation", navaidNode).trim();
            if (str != null && !str.isEmpty()) {
                Elevation elevation = processElevation(str);
                if (elevation.getReference() == null) {
                    elevation.setReference(ElevationReference.SFC);
                }
                navaid.setElevation(elevation);
            }

            // get the coverage
            xpath.reset();
            str = xpath.evaluate("Navaid-remarks/text()[1]", navaidNode).trim();
            if (str != null && !str.isEmpty() && str.startsWith("Coverage")) {
                String s = str.substring(str.indexOf(':') + 1,
                                         str.indexOf('/')).trim();
                navaid.setCoverage(processDistance(s));
            }

            // get the remarks
            xpath.reset();
            NodeList nodes = (NodeList) xpath.evaluate("Navaid-remarks/text()",
                                            navaidNode,
                                            XPathConstants.NODESET);
            StringBuffer strb = new StringBuffer();
            for (int i = 0; i < nodes.getLength(); ++i) {
                strb.append(nodes.item(i).getNodeValue());
                strb.append(" ");
            }
            str = strb.toString().trim();
            if (!str.isEmpty()) {
                navaid.setRemarks(str);
            }

            return navaid;

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(navaidNode, e);
        }
    }

    /**
     * Process a magnetic variation string found in the eAIP.
     *
     * @param str the magnetic variation string, like "+4.1° / 2009"
     * @return the magnetic variation described by str
     */
    private MagneticVariation processVariation(String str) {
        MagneticVariation variation = new MagneticVariation();

        // cut up to degree & year
        int i = str.indexOf('/');

        // process the degree of variation
        String s = str.substring(0, i).trim();
        // remove the trailing degree sign
        if (s.endsWith("\u00b0")) {
            s = s.substring(0, s.length() - 1);
        }
        variation.setVariation(Double.parseDouble(s));

        // process the year
        s = str.substring(i + 1).trim();
        variation.setYear(Integer.parseInt(s));
        return variation;
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
     *  @param navaids the navaids that are contained in the eAIP
     *  @param errors all parsing errors will be written to this list
     */
    @Override
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<Navaid>            navaids,
                            List<ParseException>    errors) {

        NodeList nodes = null;

        // get the name & designator
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            nodes = (NodeList) xpath.evaluate("//Navaid-table/Navaid",
                                              eAipNode,
                                              XPathConstants.NODESET);
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
