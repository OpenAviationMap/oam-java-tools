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
package org.openaviationmap.converter.eaip;

import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Boundary;
import org.openaviationmap.model.Elevation;
import org.openaviationmap.model.Point;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

/**
 * eAIP processor for the ENR-5.5 segment of an eAIP.
 * This is the very same as the generic eAIP processor.
 */
public class EAipProcessorEnr55 extends EAipProcessor {
    /**
     *  Process an airspace definition from the aAIP.
     *
     *  @param airspaceNode the XML node that represents the airspace
     *         which is an &lt;x:tr&gt; node
     *  @param borderPoints a list of points repesenting the country border,
     *         which is used for airspaces that reference a country border.
     *         may be null.
     *  @return an airspace described by the node
     *  @throws ParseException on input parsing errors.
     */
    @Override
    Airspace processAirspace(Node        airspaceNode,
                             List<Point> borderPoints) throws ParseException {

        try {
            Airspace airspace = new Airspace();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name & designator
            String designator = xpath.evaluate("td[1]//strong/text()[1]",
                                               airspaceNode).trim();
            xpath.reset();
            String name = xpath.evaluate(
                    "substring-after(td[1]//strong/text()[2], '/')",
                    airspaceNode).trim();

            int ix = designator.indexOf("/");
            if (ix != -1) {
                name       = designator.substring(ix + 1).trim();
                designator = designator.substring(0, ix).trim();
            }
            String type = getAirspaceType(designator);

            airspace.setDesignator(designator);
            airspace.setName(name);
            airspace.setType(type);

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            String str = xpath.evaluate("td[1]//br/following-sibling::text() "
                            + "| td[1]//br/following-sibling::Inserted/text() "
                            + "| td[1]//br/following-sibling::*//text() ",
                            airspaceNode);
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(designator, str);
            } else {
                boundary = processPointList(designator, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate("td[position()=2]", airspaceNode);
            int i   = str.indexOf("/");
            Elevation upperLimit = processElevation(name,
                                                str.substring(0, i).trim());
            Elevation lowerLimit = processElevation(name,
                                                str.substring(i + 1).trim());

            airspace.setUpperLimit(upperLimit);
            airspace.setLowerLimit(lowerLimit);

            // get the operator
            xpath.reset();
            str = xpath.evaluate("td[position()=3]/text()[position()=1]",
                                 airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setOperator(str);
            }

            // get the active time, remarks & class
            xpath.reset();
            str = xpath.evaluate("td[4]/text()[1]", airspaceNode);
            String str1 = xpath.evaluate("td[4]/text()[2]", airspaceNode);
            String str2 = xpath.evaluate("td[4]/text()[3]", airspaceNode);
            String str3 = xpath.evaluate("td[4]/text()[4]", airspaceNode);
            if (str3 != null && !str3.isEmpty()) {
                if (str3.startsWith("Class ")) {
                    airspace.setAirspaceClass(
                                            str3.substring("Class ".length()));
                }
                airspace.setRemarks(str1 + " " + str2);
                airspace.setActiveTime(str);
            } else if (str2 != null && !str2.isEmpty()) {
                if (str2.startsWith("Class ")) {
                    airspace.setAirspaceClass(
                                            str2.substring("Class ".length()));
                }
                airspace.setRemarks(str1);
                airspace.setActiveTime(str);
            } else if (str1 != null && !str1.isEmpty()) {
                if (str1.startsWith("Class ")) {
                    airspace.setAirspaceClass(
                                            str1.substring("Class ".length()));
                }
                airspace.setRemarks(str);
            } else if (str != null && !str.isEmpty()
                    && str.startsWith("Class ")) {
                airspace.setAirspaceClass(str.substring("Class ".length()));
            }

            return airspace;
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }
}
