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
 * eAIP processor for the ENR-5.2 segment of an eAIP.
 * Everything is done the same as the generic eAIP processor.
 */
public class EAipProcessorEnr52 extends EAipProcessor {

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

            if (designator.contains("ADIZ")) {
                // TODO: handle the ADIZ definition as well
                throw new ParseException(designator,
                                         "ADIZ not supported at the moment");
            }

            airspace.setDesignator(designator);
            airspace.setName(name);
            airspace.setType(type);

            // get the boundary
            xpath.reset();
            String str = xpath.evaluate("td[1]//br/following-sibling::text() "
                            + "| td[1]//br/following-sibling::Inserted/text() "
                            + "| td[1]//br/following-sibling::*//text() ",
                            airspaceNode);
            /*
            // sometimes the boundary description is encolsed in an
            // <e:Inserted> element
            str = xpath.evaluate("td/Inserted/text()", airspaceNode);
            if (str == null || str.isEmpty()) {
                // but usually its just the text node in the <x:td> element
                str = xpath.evaluate("td/text()", airspaceNode);
            }
            */
            Boundary boundary = null;
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

            // get the remarks
            xpath.reset();
            str = xpath.evaluate("td[position()=3]/text()[position()=2]",
                                 airspaceNode);
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
