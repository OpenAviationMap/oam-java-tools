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
 * eAIP processor for the AD-1.3 segment of an eAIP, which is an index.
 * of aerodromes.
 */
public class EAipProcessorAd13 extends EAipProcessor {
    /**
     *  Process a row in an AD-1.3 aerodrome index.
     *
     *  @param node the AD-1.3 aerodrome row
     *  @return aerodrome the aerodrome described by that row, with only
     *          minimal data filled in, like ICAO code and name.
     *  @throws ParseException on input parsing errors.
     */
    Aerodrome processAdNode(Node node) throws ParseException {

        Aerodrome ad = new Aerodrome();

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the name
            String str = xpath.evaluate("td[1]/text()[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                ad.setName(str);
            }

            // get the ICAO code
            str = xpath.evaluate("td[1]/text()[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                ad.setIcao(str);
            }

        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }

        return ad;
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

        XPath xpath = XPathFactory.newInstance().newXPath();

        // process the list of aerodromes
        try {
            NodeList nodes = (NodeList) xpath.evaluate(
                    "//AD-1.3/Sub-section[1]/table/tbody/tr[position() > 1]",
                    eAipNode,  XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                Aerodrome ad = processAdNode(nodes.item(i));
                aerodromes.add(ad);
            }

        } catch (XPathExpressionException e) {
            errors.add(new ParseException(e));
        } catch (ParseException e) {
            errors.add(e);
        }

    }

}
