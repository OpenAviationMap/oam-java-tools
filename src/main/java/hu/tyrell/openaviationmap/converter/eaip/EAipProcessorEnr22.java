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
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.Point;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

/**
 * eAIP processor for the ENR-2.2 segment of an eAIP.
 */
public class EAipProcessorEnr22 extends EAipProcessor {
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

            // get the vertical limits & class
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[2]", airspaceNode);
            int i   = str.indexOf("\n");
            int j   = str.lastIndexOf("\n");
            Elevation upperLimit = processElevation(str.substring(0, i).trim());
            Elevation lowerLimit = processElevation(
                                                str.substring(i + 1, j).trim());

            airspace.setUpperLimit(upperLimit);
            airspace.setLowerLimit(lowerLimit);
            airspace.setAirspaceClass(str.substring(j + 1).trim());

            // get the operator
            xpath.reset();
            str = xpath.evaluate("td[2]/text()", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setOperator(str);
            }

            // get the frequencies
            xpath.reset();
            str = xpath.evaluate("td[4]/text()", airspaceNode).trim();
            if (str != null && !str.isEmpty()) {
                airspace.setCommFrequency(str);
            }

            // get the remarks
            xpath.reset();
            str = xpath.evaluate("td[5]/text()", airspaceNode);
            if (str != null && !str.isEmpty()) {
                airspace.setRemarks(str);
            }

            return airspace;

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(airspaceNode, e);
        }
    }

}
