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
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.CompoundBoundary;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * eAIP processor for the ENR-2.1 segment of an eAIP.
 */
public class EAipProcessorEnr21 extends EAipProcessor {
    /**
     *  Process an MTMA definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @return an airspace described by the node
     *  @throws ParseException on input parsing errors.
     */
    Airspace processMtma(Node        airspaceNode,
                         List<Point> borderPoints) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String name = xpath.evaluate("td[1]//strong/text()[1]",
                                         airspaceNode).trim();
            airspace.setName(name);

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            String str = xpath.evaluate("td[1]/text()[1]", airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[2]", airspaceNode);
            Elevation upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/text()[3]", airspaceNode);
            Elevation lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the operator
            xpath.reset();
            String operator = xpath.evaluate("td[2]/text()", airspaceNode);
            if (operator != null && !operator.isEmpty()) {
                airspace.setOperator(operator.trim());
            }

            // get the airspace type
            airspace.setType("MTMA");

            return airspace;

        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }

    /**
     *  Process an MCTR definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @param airspaces the created airspaces are put into this list
     *  @throws ParseException on input parsing errors.
     */
    void processMctr(Node               airspaceNode,
                     List<Point>        borderPoints,
                     List<Airspace>     airspaces) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String name = xpath.evaluate("td[1]//strong/text()[1]",
                                         airspaceNode).trim();
            airspace.setName(name);

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            String str = xpath.evaluate("td[1]/text()[1]", airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[2]", airspaceNode);
            Elevation upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/text()[3]", airspaceNode);
            Elevation lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the operator
            xpath.reset();
            String operator = xpath.evaluate("td[2]/text()", airspaceNode);
            if (operator != null && !operator.isEmpty()) {
                airspace.setOperator(operator.trim());
            }

            // get the airspace type
            airspace.setType("MCTR");

            // let's see if there is a second airspace being defined here
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[4]", airspaceNode);
            if (!"and".equals(str)) {
                airspace.setBoundary(boundary);
                airspaces.add(airspace);

                return;
            }

            // otherwise, construct a compound boundary, and read the other
            // airspace boundary as well

            ArrayList<Boundary> boundaryList = new ArrayList<Boundary>();
            boundaryList.add(boundary);

            // get the boundary
            boundary = null;
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[5]", airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[6]", airspaceNode);
            upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/text()[7]", airspaceNode);
            lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the airspace type
            airspace.setType("MCTR");

            boundaryList.add(boundary);
            CompoundBoundary cBoundary = new CompoundBoundary();
            cBoundary.setBoundaryList(boundaryList);
            airspace.setBoundary(cBoundary);

            airspaces.add(airspace);

        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }

    /**
     *  Process an TMA definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @return an airspace described by the node
     *  @throws ParseException on input parsing errors.
     */
    Airspace processTma(Node        airspaceNode,
                        List<Point> borderPoints) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String name = xpath.evaluate("td[1]//strong/text()[1]",
                                         airspaceNode).trim();
            airspace.setName(name);

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            String str = xpath.evaluate("td[1]/text()[1]", airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[2]", airspaceNode);
            Elevation upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/text()[3]", airspaceNode);
            Elevation lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the airspace type
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[4]", airspaceNode);
            airspace.setAirspaceClass(str);

            airspace.setType("TMA");

            return airspace;

        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }

    /**
     * Process the Budapest TMA airspace nodes.
     *
     * @param nodes the nodes describing the Budapest TMA airspace.
     * @param borderPoints a list of points repesenting the country border,
     *        which is used for airspaces that reference a country border.
     *        may be null.
     * @param airspaces the airspaces recognized are put into this list
     * @throws ParseException on parsing issues
     */
    void processBudapestTma(NodeList        nodes,
                            List<Point>     borderPoints,
                            List<Airspace>  airspaces)
                                                throws ParseException {
        Node node = null;

        try {
            // the first table row contains the frequences for the TMA
            XPath xpath = XPathFactory.newInstance().newXPath();

            node = nodes.item(0);
            String frequencies = xpath.evaluate("td[4]",
                                 node).trim().replaceAll("\\s+", " ");

            xpath.reset();
            String operator = xpath.evaluate("td[2]",
                              node).trim().replaceAll("\\s+", " ");

            // process all the TMA parts one by one
            for (int i = 2; i < nodes.getLength(); ++i) {
                node = nodes.item(i);

                Airspace airspace = processTma(node, borderPoints);
                airspace.setCommFrequency(frequencies);
                if (operator != null && !operator.isEmpty()) {
                    airspace.setOperator(operator);
                }

                airspaces.add(airspace);
            }

        } catch (Exception e) {
            throw new ParseException(node, e);
        }
    }

    /**
     *  Process an FIR definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @param airspaces created airspaces will be put into this list
     *  @throws ParseException on input parsing errors.
     */
    void processBudapestFir(Node            airspaceNode,
                            List<Point>     borderPoints,
                            List<Airspace>  airspaces) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String name = xpath.evaluate("td[1]//strong/text()",
                                         airspaceNode).trim();
            airspace.setName(name);

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            String str = xpath.evaluate("td[1]/text()[1]", airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[1]/br[2]/following-sibling::text()[1]",
                                 airspaceNode);
            Elevation upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/br[3]/following-sibling::text()[1]",
                                 airspaceNode);
            Elevation lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the unit providing service
            xpath.reset();
            str = xpath.evaluate("td[2]/text()", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setOperator(str.trim());
            }

            // get the active time
            xpath.reset();
            str = xpath.evaluate("td[3]/text()[3]", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setActiveTime(str.trim());
            }

            // get remarks
            xpath.reset();
            str = xpath.evaluate("td[5]/text()[1]", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setRemarks(str);
            }

            airspace.setType("FIR");

            airspaces.add(airspace);

        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }

    /**
     *  Process a CTA definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @param airspaces created airspaces will be put into this list
     *  @throws ParseException on input parsing errors.
     */
    void processBudapestCta(Node            airspaceNode,
                            List<Point>     borderPoints,
                            List<Airspace>  airspaces) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String name = xpath.evaluate("td[1]//strong/text()[1]",
                                         airspaceNode).trim();
            airspace.setName(name);

            // get the boundary
            // we're cheating here, as the definition is textual at best
            // let's just copy the border ponts
            Ring boundary = new Ring();
            boundary.setPointList(new Vector<Point>(borderPoints));
            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            String str = xpath.evaluate("td[1]/text()[2]", airspaceNode);
            Elevation upperLimit = processElevation(name, str.trim());
            airspace.setUpperLimit(upperLimit);

            xpath.reset();
            str = xpath.evaluate("td[1]/text()[3]", airspaceNode);
            Elevation lowerLimit = processElevation(name, str.trim());
            airspace.setLowerLimit(lowerLimit);

            // get the airspace class
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[4]", airspaceNode);
            airspace.setAirspaceClass(str);

            // get the unit providing service
            xpath.reset();
            str = xpath.evaluate("td[2]/text()", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setOperator(str.trim());
            }

            // get the active time
            xpath.reset();
            str = xpath.evaluate("td[3]/text()[3]", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setActiveTime(str);
            }

            // get the frequencies
            xpath.reset();
            str = xpath.evaluate("td[4]", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setCommFrequency(str.trim()
                                          .replaceAll("MHZ", "MHZ ")
                                          .replaceAll("CH", "CH ")
                                          .replaceAll("UHF", "UHF ")
                                          .replaceAll("\\s+", " "));
            }

            // get remarks
            xpath.reset();
            str = xpath.evaluate("td[5]/text()[1]", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setRemarks(str);
            }

            airspace.setType("CTA");

            airspaces.add(airspace);

        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
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

        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            // process the BUDAPEST FIR definition
            nodes = (NodeList) xpath.evaluate(
                    "//table[contains(.,'BUDAPEST FIR')]/tbody/tr",
                    eAipNode,
                    XPathConstants.NODESET);

            if (nodes != null && nodes.getLength() > 0) {
                processBudapestFir(nodes.item(0), borderPoints, airspaces);
            }

            // process the BUDAPEST CTA definition
            nodes = (NodeList) xpath.evaluate(
                    "//table[contains(.,'BUDAPEST CTA')]/tbody/tr",
                    eAipNode,
                    XPathConstants.NODESET);

            if (nodes != null && nodes.getLength() > 0) {
                processBudapestCta(nodes.item(0), borderPoints, airspaces);
            }

            // process the Budapest TMA sectors
            nodes = (NodeList) xpath.evaluate(
                          "//table[contains(.,'BUDAPEST TMA')]/tbody/tr",
                          eAipNode,
                          XPathConstants.NODESET);

            if (nodes != null) {
                processBudapestTma(nodes, borderPoints, airspaces);
            }

            // process the MTMA sectors
            nodes = (NodeList) xpath.evaluate(
                          "//table[contains(.,'MTMA')]/tbody/tr",
                          eAipNode,
                          XPathConstants.NODESET);

            if (nodes != null) {
                // the first two tr elements are not full MTMA defs
                // but the generic Kecskemet MTMA description
                for (int i = 2; i < nodes.getLength(); ++i) {
                    Node node = nodes.item(i);
                    Airspace airspace = processMtma(node, borderPoints);
                    airspaces.add(airspace);
                }
            }

            // process the MCTRs
            nodes = (NodeList) xpath.evaluate(
                          "//table[contains(.,'MCTR')]/tbody/tr",
                          eAipNode,
                          XPathConstants.NODESET);

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Node node = nodes.item(i);
                    processMctr(node, borderPoints, airspaces);
                }
            }

        } catch (XPathExpressionException e) {
            errors.add(new ParseException(e));
        } catch (ParseException e) {
            errors.add(e);
        }
    }

}
