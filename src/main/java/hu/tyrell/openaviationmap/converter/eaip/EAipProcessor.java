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
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Distance;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Point;
import hu.tyrell.openaviationmap.model.Ring;
import hu.tyrell.openaviationmap.model.UOM;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for processing eAIP documents.
 */
public class EAipProcessor {
    /**
     * The prefix string for a circle description.
     */
    private static final String CIRCLE_PREFIX = "A circle radius";

    /**
     * The infix string for a circle description, between radius an center
     * point.
     */
    private static final String CIRCLE_INFIX = "centered on";

    /**
     * The infix string for a circle description, between radius an center
     * point. note: it's not a typo, this is how it is in the document
     */
    private static final String CIRCLE_INFIX_SIC = "centred on";

    /**
     * Convert a latitude string into a latitude value.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param latStr the latitude string
     * @return the latitude value
     * @throws ParseException on parsing errors
     */
    private double processLat(String designator, String latStr)
                                                        throws ParseException {
        if (latStr == null) {
            throw new ParseException(designator, "lat string is null");
        }
        if (latStr.length() < 7) {
            throw new ParseException(designator,
                                     "lat string too short: '" + latStr + "'");
        }
        double degrees = Double.parseDouble(latStr.substring(0, 2));
        double minutes = Double.parseDouble(latStr.substring(2, 4));
        double seconds = Double.parseDouble(latStr.substring(4, 6));

        double value = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return latStr.charAt(6) == 'S' ? -value : value;
    }

    /**
     * Convert a longitude string into a longitude value.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param lonStr the longitude string
     * @return the longitude value
     * @throws ParseException on parsing errors
     */
    private double processLon(String designator, String lonStr)
                                                        throws ParseException {
        if (lonStr == null) {
            throw new ParseException(designator, "lon string is null");
        }
        if (lonStr.length() < 8) {
            throw new ParseException(designator,
                                     "lon string too short: '" + lonStr + "'");
        }
        double degrees = Double.parseDouble(lonStr.substring(0, 3));
        double minutes = Double.parseDouble(lonStr.substring(3, 5));
        double seconds = Double.parseDouble(lonStr.substring(5, 7));

        double value = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return lonStr.charAt(7) == 'W' ? -value : value;
    }

    /**
     * Convert an eAIP point string description into a Point object.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param pointDesc a textual point description
     * @return the corresponding Point object.
     * @throws ParseException on parsing errors
     */
    private Point processPoint(String designator, String pointDesc)
                                                        throws ParseException {
        String pd = pointDesc.trim();
        int space = pd.indexOf(" ");
        String latStr = pd.substring(0, space).trim();
        String lonStr = pd.substring(space + 1).trim();

        Point point = new Point();
        point.setLatitude(processLat(designator, latStr));
        point.setLongitude(processLon(designator, lonStr));

        return point;
    }

    /**
     * Extract a section of a border polygon, which is closest to the points
     * specified. Insert the extracted points into the supplied point list.
     *
     * @param borderSectionStart the start of the border section to extract
     * @param borderSectionEnd the end of the border section to extract
     * @param borderPoints the points of the border to extract from
     * @param pointList append the extracted points to this list
     */
    private void appendBorderSection(Point       borderSectionStart,
                                     Point       borderSectionEnd,
                                     List<Point> borderPoints,
                                     List<Point> pointList) {
        // find the point on the border that is closest to the section start
        double dist    = Double.MAX_VALUE;
        int    startIx = 0;
        for (int ix = 0; ix < borderPoints.size(); ++ix) {
            Point  p = borderPoints.get(ix);
            double d = borderSectionStart.distance(p);
            if (d < dist) {
                dist    = d;
                startIx = ix;
            }
        }

        // find the point on the border that is closest to the section end
        dist         = Double.MAX_VALUE;
        int    endIx = 0;
        for (int ix = 0; ix < borderPoints.size(); ++ix) {
            Point  p = borderPoints.get(ix);
            double d = borderSectionEnd.distance(p);
            if (d < dist) {
                dist    = d;
                endIx   = ix;
            }
        }

        // copy the points from startIx to endIx into pointList
        if (startIx <= endIx) {
            for (int i = startIx; i < endIx; ++i) {
                pointList.add(borderPoints.get(i));
            }
        } else {
            for (int i = startIx; i > endIx; --i) {
                pointList.add(borderPoints.get(i));
            }
        }
    }

    /**
     * Parse an eAIP point list into a Boundary.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param boundaryDesc the textual boundary description
     * @param borderPoints a list of points repesenting the country border,
     *        which is used for airspaces that reference a country border.
     *        may be null.
     * @return the airspace boundary
     * @throws ParseException on parsing errors
     */
    private Ring processPointList(String      designator,
                                  String      boundaryDesc,
                                  List<Point> borderPoints)
                                                      throws ParseException {

        Vector<Point> pointList          = new Vector<Point>();
        Point         borderSectionStart = null;

        StringTokenizer tokenizer = new StringTokenizer(boundaryDesc, "-");
        while (tokenizer.hasMoreTokens()) {
            String str = tokenizer.nextToken();

            Point p = processPoint(designator, str);

            if (borderSectionStart != null) {
                appendBorderSection(borderSectionStart,
                                    p,
                                    borderPoints,
                                    pointList);

                borderSectionStart = null;
            }

            int borderIx = str.indexOf("along border");
            if (borderIx != -1) {
                if (borderPoints == null) {
                    System.out.println("WARINING: airspace " + designator
                           + " contains the following country border: "
                           + str.substring(borderIx + "along border".length()));
                }

                borderSectionStart = p;
            }

            pointList.add(p);
        }

        Ring boundary = new Ring();
        boundary.setPointList(pointList);

        return boundary;
    }

    /**
     * Process a textual elevation description.
     *
     * @param elevDesc the textual elevation description
     * @return the elevation described by elevDesc
     */
    private Elevation processElevation(String elevDesc) {
        String ed = elevDesc.trim();

        Elevation elevation = new Elevation();

        if ("GND".equals(ed)) {
            elevation.setElevation(0);
            elevation.setReference(ElevationReference.SFC);
            elevation.setUom(UOM.FT);
        } else if (ed.startsWith("FL")) {
            elevation.setElevation(
                    Double.parseDouble(ed.substring(2).trim()));
            elevation.setReference(ElevationReference.MSL);
            elevation.setUom(UOM.FL);
        } else {
            // get the elevation
            int i = ed.indexOf(" ");
            elevation.setElevation(
                    Double.parseDouble(ed.substring(0, i)));

            // get the unit of measurement
            int j = ed.indexOf(" ", i + 1);
            String uom = ed.substring(i, j).trim();
            if ("FT".equals(uom)) {
                elevation.setUom(UOM.FT);
            }

            // get the reference
            String reference = ed.substring(j).trim();
            if ("ALT".equals(reference)) {
                elevation.setReference(ElevationReference.MSL);
            } else if ("AGL".equals(reference)) {
                elevation.setReference(ElevationReference.SFC);
            }
        }

        return elevation;
    }

    /**
     * Process a distance description.
     *
     * @param distDesc a textual distance description
     * @return the distance described by the description
     */
    private Distance processDistance(String distDesc) {
        String dd = distDesc.trim();

        Distance distance = new Distance();

        if (dd.endsWith("KM")) {
            distance.setDistance(Double.parseDouble(
                    dd.substring(0, dd.length() - 2)) * 1000.0);
            distance.setUom(UOM.M);
        }

        return distance;
    }

    /**
     * Process a circle boundary description.
     *
     * @param designator the airspace designator string
     * @param circleDesc the textual circle description
     * @return the circle boundary described by the airspace
     * @throws ParseException on parsing errors
     */
    Circle processCircle(String designator,
                         String circleDesc) throws ParseException {
        String cd = circleDesc.trim();

        Circle circle = new Circle();

        int i = cd.indexOf(CIRCLE_PREFIX);
        int j = cd.indexOf(CIRCLE_INFIX);
        int infixLen = CIRCLE_INFIX.length();
        if (i < 0) {
            throw new ParseException(designator,
                                     "Circle description missing prefix");
        }
        if (j < 0) {
            j = cd.indexOf(CIRCLE_INFIX_SIC);
            if (j < 0) {
                throw new ParseException(designator,
                                         "Circle description missing infix");
            }
            infixLen = CIRCLE_INFIX_SIC.length();
        }
        String radiusStr = cd.substring(i + CIRCLE_PREFIX.length(), j).trim();
        circle.setRadius(processDistance(radiusStr));

        String centerStr = cd.substring(j + infixLen + 1).trim();
        circle.setCenter(processPoint(designator, centerStr));

        return circle;
    }

    /**
     * Return an airspace type from an airspace designation. This is done
     * by getting the substring after the country code in the designation
     * (e.g. "LH"), but before any airspace numeric id. For example, from
     * "LHTRA23A" this would return "TRA".
     *
     *  @param designator the airspace designator string
     *  @return the airspace type.
     *  @throws ParseException on input parsing errors.
     */
    private String getAirspaceType(String designator) throws ParseException {
        if (designator.length() < 3) {
            throw new ParseException(designator,
                              "designator too short to deduce airspace type");
        }

        int i = 2;

        for (; i < designator.length(); ++i) {
            if (Character.isDigit(Character.valueOf(designator.charAt(i)))) {
                break;
            }
        }

        return designator.substring(2, i);
    }

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
            Elevation upperLimit = processElevation(str.substring(0, i).trim());
            Elevation lowerLimit = processElevation(
                                                str.substring(i + 1).trim());

            airspace.setUpperLimit(upperLimit);
            airspace.setLowerLimit(lowerLimit);

            // get the remarks
            xpath.reset();
            str = xpath.evaluate("td[position()=3]/text()[position()=2]",
                                 airspaceNode);
            airspace.setRemarks(str);

            return airspace;
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
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
     *  @param errors all parsing errors will be written to this list
     */
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<ParseException>    errors) {

        NodeList nodes = null;

        // get the name & designator
        try {
            XPath          xpath     = XPathFactory.newInstance().newXPath();

            nodes = (NodeList) xpath.evaluate(
                          "//table/tbody/tr"
                        + "[not(descendant::processing-instruction('Fm')"
                                         + "[contains(., 'APSToBeDeleted')])]",
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
                Airspace airspace = processAirspace(nodes.item(i),
                                                    borderPoints);
                airspaces.add(airspace);
            } catch (ParseException e) {
                errors.add(e);
                continue;
            }
        }
    }

}
