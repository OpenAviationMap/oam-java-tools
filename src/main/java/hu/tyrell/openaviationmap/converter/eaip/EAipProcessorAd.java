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
import hu.tyrell.openaviationmap.model.Boundary;
import hu.tyrell.openaviationmap.model.Distance;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Frequency;
import hu.tyrell.openaviationmap.model.Navaid;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Runway;
import hu.tyrell.openaviationmap.model.SurfaceType;
import hu.tyrell.openaviationmap.model.UOM;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * eAIP processor for Aerodrome (AD) segments of an eAIP.
 */
public class EAipProcessorAd extends EAipProcessor {
    /**
     *  Process section AD-2.2 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the AD-2.2 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd22(Aerodrome ad, Node node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the ARP
            String str = xpath.evaluate("//tbody/tr[1]/td[3]", node).trim();
            if (str != null && !str.isEmpty()) {
                int i = str.indexOf(' ');
                String latStr = str.substring(0, i).trim();
                int j = str.indexOf(' ', i + 1);
                String lonStr = str.substring(i, j).trim();

                Point arp = new Point();

                arp.setLatitude(processLat(ad.getIcao(), latStr));
                arp.setLongitude(processLon(ad.getIcao(), lonStr));

                ad.setArp(arp);
            }

            // get the elevation
            xpath.reset();
            str = xpath.evaluate("//tbody/tr[3]/td[3]", node).trim();
            if (str != null && !str.isEmpty()) {
                int i = str.indexOf('/');
                Elevation e = processElevation(ad.getIcao(),
                                               str.substring(0, i).trim());
                if (e.getReference() == null) {
                    e.setReference(ElevationReference.MSL);
                }
                ad.setElevation(e);
            }

            // get the remarks
            xpath.reset();
            str = xpath.evaluate("//tbody/tr[8]/td[3]", node).trim();
            if (str != null && !str.isEmpty() && !"Nil".equals(str)) {
                ad.setRemarks(str);
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     * Process a runway row definition in the eAIP section AD-2.12.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the tr element, a row describing a runway
     *  @return the runway as described by the eAIP row.
     *  @throws ParseException on input parsing errors.
     */
    Runway processRunwayNode(Aerodrome ad, Node node) throws ParseException {
        Runway runway = new Runway();

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String designator = "";

            String str = xpath.evaluate("td[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                runway.setDesignator(str);
                designator = str;
            }

            xpath.reset();
            str = xpath.evaluate("td[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                // find the degree sign
                int degSign = str.indexOf("\u00b0");
                runway.setBearing(Double.parseDouble(
                                                    str.substring(0, degSign)));
            }

            xpath.reset();
            str = xpath.evaluate("td[3]", node).trim();
            if (str != null && !str.isEmpty()) {
                int x = str.indexOf('x');

                Distance length = new Distance();
                length.setDistance(Double.parseDouble(str.substring(0, x)));
                length.setUom(UOM.M);

                Distance width = new Distance();
                width.setDistance(Double.parseDouble(str.substring(x + 1)));
                width.setUom(UOM.M);

                runway.setWidth(width);
                runway.setLength(length);
            }

            xpath.reset();
            str = xpath.evaluate("td[4]", node).trim();
            if (str != null && !str.isEmpty()) {
                if (str.endsWith("ASPH")) {
                    runway.setSurface(SurfaceType.ASPHALT);
                } else if (str.endsWith("GRASS")) {
                    runway.setSurface(SurfaceType.GRASS);
                }
            }

            xpath.reset();
            str = xpath.evaluate("td[5]/text()[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                int sp = str.indexOf(' ');
                String latStr = str.substring(0, sp).trim();
                String lonStr = str.substring(sp + 1).trim();

                Point threshold = new Point();
                threshold.setLatitude(processLat(designator, latStr));
                threshold.setLongitude(processLon(designator, lonStr));
                runway.setThreshold(threshold);
            }

            xpath.reset();
            str = xpath.evaluate("td[5]/text()[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                int sp = str.indexOf(' ');
                String latStr = str.substring(0, sp).trim();
                String lonStr = str.substring(sp + 1).trim();

                Point end = new Point();
                end.setLatitude(processLat(designator, latStr));
                end.setLongitude(processLon(designator, lonStr));
                runway.setEnd(end);
            }

            xpath.reset();
            str = xpath.evaluate("td[6]/text()[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                Elevation e = processElevation(designator, str);
                e.setReference(ElevationReference.MSL);
                runway.setElevation(e);
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }

        return runway;
    }

    /**
     * Process the second part of a runway row definition in the
     * eAIP section AD-2.12.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the tr element, a row describing a runway
     *  @param runway the runway to put the new information into
     *  @throws ParseException on input parsing errors.
     */
    void processRunwayNode2(Aerodrome ad, Runway runway, Node node)
                                                        throws ParseException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            String str = xpath.evaluate("td[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                int i = str.indexOf('%');
                if (i != -1) {
                    str = str.substring(0, i);
                }
                runway.setSlope(Double.parseDouble(str));
            }

        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     * Process a runway distance row in the eAIP section AD-2.13.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the tr element, a row describing a runway
     *  @throws ParseException on input parsing errors.
     */
    void processRunwayDistancesNode(Aerodrome ad, Node node)
                                                    throws ParseException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String designator = "";

            String str = xpath.evaluate("td[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                designator = str;
            }

            // find the runway this row is about
            Runway rwy = null;
            for (Runway r : ad.getRunways()) {
                if (designator.equals(r.getDesignator())) {
                    rwy = r;
                    break;
                }
            }
            if (rwy == null) {
                throw new ParseException(ad.getIcao(),
                        "runway " + designator + " does not exist");
            }

            // TORA
            xpath.reset();
            str = xpath.evaluate("td[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                rwy.setTora(new Distance(Double.parseDouble(str), UOM.M));
            }

            // TODA
            xpath.reset();
            str = xpath.evaluate("td[3]", node).trim();
            if (str != null && !str.isEmpty()) {
                rwy.setToda(new Distance(Double.parseDouble(str), UOM.M));
            }

            // ASDA
            xpath.reset();
            str = xpath.evaluate("td[4]", node).trim();
            if (str != null && !str.isEmpty()) {
                rwy.setAsda(new Distance(Double.parseDouble(str), UOM.M));
            }

            // LDA
            xpath.reset();
            str = xpath.evaluate("td[5]", node).trim();
            if (str != null && !str.isEmpty()) {
                rwy.setLda(new Distance(Double.parseDouble(str), UOM.M));
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     * Process an ATS row in the eAIP section AD-2.18.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the tr element, a row describing a runway
     *  @throws ParseException on input parsing errors.
     */
    void processAtsNode(Aerodrome ad, Node node) throws ParseException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            Frequency f;
            String str = xpath.evaluate("td[3]", node).trim();
            if (str != null && !str.isEmpty()) {
                int i = str.indexOf('\n');
                // process only the first frequency available
                if (i != -1) {
                    str = str.substring(0, i);
                }
                f = Frequency.fromString(str);
            } else {
                throw new ParseException(ad.getIcao(),
                                         "ATS frequency missing");
            }

            xpath.reset();
            str = xpath.evaluate("td[1]/text()", node).trim();
            if (str == null || str.isEmpty()) {
                str = xpath.evaluate("td[1]/Abbreviation/@Ref", node).trim();
            }
            if (str != null && !str.isEmpty()) {
                if (str.endsWith("AFIS")) {
                    ad.setAfis(f);
                }
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     *  Process section AD-2.12 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the AD-2.12 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd212(Aerodrome ad, Node node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xpath.evaluate(
                            "table/tbody/tr"
                          + "[following-sibling::tr[contains(., 'Slope')] "
                                                 + "and not(descendant::th)]",
                          node, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                Runway rwy = processRunwayNode(ad, nodes.item(i));

                ad.getRunways().add(rwy);
            }

            nodes = (NodeList) xpath.evaluate(
                            "table/tbody/tr"
                          + "[preceding-sibling::tr[contains(., 'Slope')]]"
                          + "[position() > 1]",
                          node, XPathConstants.NODESET);

            if (nodes.getLength() != ad.getRunways().size()) {
                throw new ParseException(ad.getIcao(),
                        "AD-2.12 runway definition incorrect second part"
                      + " (" + ad.getRunways().size() + " vs. "
                             + nodes.getLength() + ")");
            }

            for (int i = 0; i < nodes.getLength(); ++i) {
                processRunwayNode2(ad, ad.getRunways().get(i), nodes.item(i));
            }


        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     *  Process section AD-2.13 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the AD-2.13 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd213(Aerodrome ad, Node node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xpath.evaluate("table/tbody/tr",
                                            node, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                processRunwayDistancesNode(ad, nodes.item(i));
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     *  Process section AD-2.17 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param borderPoints points of the national border, in case an airspace
     *         definition refers to a border
     *  @param airspaces a list of airspaces. the newly processed airspace
     *         will be put into this map.
     *  @param node the AD-2.17 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd217(Aerodrome         ad,
                      List<Point>       borderPoints,
                      List<Airspace>    airspaces,
                      Node              node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            Airspace airspace = new Airspace();
            String   name     = null;

            String str = xpath.evaluate(
                            "table/tbody/tr[1]/td[3]/text()[1]", node).trim();
            if (str != null && !str.isEmpty()) {

                String type = xpath.evaluate(
                            "table/tbody/tr[1]/td[3]/Abbreviation/@Ref", node)
                            .trim();
                if (type != null && !type.isEmpty()) {
                    if (type.startsWith("ABBR-")) {
                        type = type.substring(5).trim();

                        name = str + " " + type;
                    }
                } else {
                    int i = str.lastIndexOf(' ');
                    if (i != -1) {
                        type = str.substring(i + 1);
                    }
                    name = str;
                }

                airspace.setName(name);
                if (type != null && !type.isEmpty()) {
                    airspace.setType(type);
                }
            }

            // get the boundary
            Boundary boundary = null;
            xpath.reset();
            // the airspace definition might simply be in the first row
            str = xpath.evaluate(
                            "table/tbody/tr[1]/td[3]/text()[2]", node).trim();
            // or, it may be scattered in a number of rows
            if (str == null || str.isEmpty()) {
                NodeList nl = (NodeList) xpath.evaluate(
                   "table/tbody/tr[position() > 1 "
                 + "and following-sibling::tr[contains(., 'Vertical limits')]]"
                 + "/td/text()",
                    node,
                    XPathConstants.NODESET);

                str = "";
                for (int i = 0; i < nl.getLength(); ++i) {
                    str += nl.item(i).getNodeValue() + " ";
                    if ((i % 2) == 1 && i != nl.getLength() - 1) {
                        str += "- ";
                    }
                }
            }
            if (str.startsWith(CIRCLE_PREFIX)) {
                boundary = processCircle(name, str);
            } else {
                boundary = processPointList(name, str, borderPoints);
            }

            airspace.setBoundary(boundary);

            // get the vertical limits
            xpath.reset();
            str = xpath.evaluate(
                  "table/tbody/tr/td "
                + "[preceding-sibling::td[contains(., 'Vertical limits')]]"
                + "/text()",
                   node).trim();
            int i   = str.indexOf("/");
            Elevation upperLimit = processElevation(name,
                                                str.substring(0, i).trim());
            airspace.setUpperLimit(upperLimit);

            if (i + 1 < str.length()) {
                Elevation lowerLimit = processElevation(name,
                                                str.substring(i + 1).trim());
                airspace.setLowerLimit(lowerLimit);
            } else {
                xpath.reset();
                str = xpath.evaluate(
                       "table/tbody/tr/td "
                     + "[preceding-sibling::td[contains(., 'Vertical limits')]]"
                     + "/Abbreviation/@Ref",
                        node).trim();
                if ("ABBR-GND".equals(str)) {
                    airspace.setLowerLimit(
                              new Elevation(0, UOM.FT, ElevationReference.SFC));
                }
            }

            xpath.reset();
            str = xpath.evaluate(
                       "table/tbody/tr/td "
                     + "[preceding-sibling::td["
                                + "contains(., 'Airspace classification')]]"
                     + "/text()",
                     node).trim();
            if (str != null && !str.isEmpty()) {
                airspace.setAirspaceClass(str);
            }

            ad.setAirspace(airspace);

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     *  Process section AD-2.18 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param node the AD-2.18 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd218(Aerodrome ad, Node node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xpath.evaluate("table/tbody/tr",
                                            node, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                processAtsNode(ad, nodes.item(i));
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     * Process a radio navigation aid row in the eAIP section AD-2.19.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param navaids a list of navaids. the newly processed navaids will
     *         be put into this list.
     *  @param node the tr element, a row describing a runway
     *  @throws ParseException on input parsing errors.
     */
    void processRNavNode(Aerodrome      ad,
                         List<Navaid>   navaids,
                         Node           node) throws ParseException {
        try {
            Navaid navaid = new Navaid();

            XPath xpath = XPathFactory.newInstance().newXPath();

            // get the type
            String str = xpath.evaluate("td[1]/text()[1]", node).trim();
            if ("DVOR/DME".equals(str)) {
                navaid.setType(Navaid.Type.VORDME);
            } else if ("DME".equals(str)) {
                navaid.setType(Navaid.Type.DME);
            } else if ("NDB".equals(str) || "L".equals(str)) {
                navaid.setType(Navaid.Type.NDB);
            } else {
                throw new ParseException(ad.getIcao(),
                                         "unknown navaid type " + str);
            }

            // get the declination
            xpath.reset();
            str = xpath.evaluate("td[1]/text()[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                // remove parenthesis
                if (str.startsWith("(")) {
                    str = str.substring(1, str.length());
                }
                if (str.endsWith(")")) {
                    str = str.substring(0, str.length() - 1);
                }
                // remove the trailing degree sign
                if (str.endsWith("\u00b0")) {
                    str = str.substring(0, str.length() - 1);
                }
                navaid.setDeclination(Double.parseDouble(str));
            }

            // get the ident, and set the name accordingly
            xpath.reset();
            str = xpath.evaluate("td[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setIdent(str);
                navaid.setName(str);
            }

            // get the frequency
            xpath.reset();
            str = xpath.evaluate("td[3]/text()[1]", node).trim();
            if (str != null && !str.isEmpty()) {
                try {
                    navaid.setFrequency(Frequency.fromString(str));
                } catch (Exception e) {
                    // this might be a DME channel string actually
                    if (str.endsWith("X") || str.endsWith("Y")) {
                        navaid.setDmeChannel(str);
                    }
                }
            }

            // get the DME channel
            xpath.reset();
            str = xpath.evaluate("td[3]/text()[2]", node).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setDmeChannel(str);
            }

            // get the active time
            xpath.reset();
            str = xpath.evaluate("td[4]", node).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setActivetime(str);
            }

            // get the latitude & longitude
            xpath.reset();
            str = xpath.evaluate("td[5]", node).trim();
            if (str != null && !str.isEmpty()) {
                String[] s = str.split("[\n \t\r]");
                if (s.length != 2) {
                    throw new ParseException(navaid.getIdent(),
                                        "incorrect navaid location string");
                }

                navaid.setLatitude(processLat(navaid.getIdent(), s[0].trim()));
                navaid.setLongitude(processLon(navaid.getIdent(), s[1].trim()));
            }

            // get the elevation
            xpath.reset();
            str = xpath.evaluate("td[6]", node).trim();
            if (str != null && !str.isEmpty()) {
                Elevation elevation = processElevation(navaid.getIdent(), str);
                if (elevation.getReference() == null) {
                    elevation.setReference(ElevationReference.MSL);
                }
                navaid.setElevation(elevation);
            }

            // get the remarks
            xpath.reset();
            str = xpath.evaluate("td[7]", node).trim();
            if (str != null && !str.isEmpty()) {
                navaid.setRemarks(str);
            }

            // extract the coverage from the remarks, if there
            if (str.contains("Coverage")) {
                String[] s = str.split(":[ \t\r\n]");
                for (int i = 0; i < s.length; ++i) {
                    if ("Coverage".equals(s[i]) && i + 1 < s.length) {
                        navaid.setCoverage(Distance.fromString(s[i + 1]));
                        break;
                    }
                }
            }

            ad.getNavaids().add(navaid);

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
        }
    }

    /**
     *  Process section AD-2.19 of an AD definition.
     *
     *  @param ad the aerodrome to collect the information into
     *  @param navaids a list of navaids. the newly generated navaids will
     *         be put into this list.
     *  @param node the AD-2.19 node of an AD eAIP document
     *  @throws ParseException on input parsing errors.
     */
    void processAd219(Aerodrome     ad,
                      List<Navaid>  navaids,
                      Node          node) throws ParseException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            NodeList nodes = (NodeList) xpath.evaluate("table/tbody/tr",
                                            node, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); ++i) {
                processRNavNode(ad, navaids, nodes.item(i));
            }

        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException(ad.getIcao(), e);
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

        // deduce the designator of the airport from the root element id
        // attribute
        String icao = null;
        String idStr = eAipNode.getAttributes().getNamedItem("id")
                                                            .getNodeValue();
        if (idStr.startsWith("AD-2.")) {
            icao = idStr.substring(5).trim();
        }

        Aerodrome ad = null;
        for (Aerodrome a : aerodromes) {
            if (icao.equals(a.getIcao())) {
                ad = a;
                break;
            }
        }
        if (ad == null) {
            ad = new Aerodrome();
            ad.setIcao(icao);
            aerodromes.add(ad);
        }

        XPath xpath = XPathFactory.newInstance().newXPath();

        // process section AD-2.2, generic info about the aerodrome
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.2", eAipNode,
                                              XPathConstants.NODE);
            processAd22(ad, node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

        // process section AD-2.12, runways
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.12", eAipNode,
                                              XPathConstants.NODE);
            processAd212(ad, node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

        // process section AD-2.13, declared runway distances like TORA, etc.
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.13", eAipNode,
                                              XPathConstants.NODE);
            processAd213(ad, node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

        // process section AD-2.17, airspace
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.17", eAipNode,
                                              XPathConstants.NODE);
            processAd217(ad,
                         borderPoints,
                         airspaces,
                         node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

        // process section AD-2.18, ATS
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.18", eAipNode,
                                              XPathConstants.NODE);
            processAd218(ad, node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

        // process section AD-2.19, radio navigation / landing facilities
        try {
            Node node = (Node) xpath.evaluate("//Aerodrome/AD-2.19", eAipNode,
                                              XPathConstants.NODE);
            processAd219(ad, navaids, node);
        } catch (XPathExpressionException e) {
            errors.add(new ParseException(icao, e));
        } catch (ParseException e) {
            errors.add(e);
        }

    }

}
