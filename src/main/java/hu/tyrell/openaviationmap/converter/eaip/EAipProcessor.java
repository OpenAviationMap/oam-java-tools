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
import hu.tyrell.openaviationmap.model.Circle;
import hu.tyrell.openaviationmap.model.Distance;
import hu.tyrell.openaviationmap.model.Elevation;
import hu.tyrell.openaviationmap.model.ElevationReference;
import hu.tyrell.openaviationmap.model.Navaid;
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
     * The generic prefix for a circle description, the common denominator
     * for all circle prefixes.
     */
    protected static final String CIRCLE_PREFIX_GENERIC = "A circle";

    /**
     * The prefix string for a circle description.
     */
    protected static final String CIRCLE_PREFIX = "A circle radius";

    /**
     * The prefix string for a circle description.
     */
    protected static final String CIRCLE_PREFIX2 = "A circle with";

    /**
     * The infix string for a circle description, between radius an center
     * point.
     */
    protected static final String CIRCLE_INFIX = "centered on";

    /**
     * The infix string for a circle description, between radius an center
     * point. note: it's not a typo, this is how it is in the document
     */
    protected static final String CIRCLE_INFIX_SIC = "centred on";

    /**
     * The infix string for a circle description, between radius an center
     * point. note: it's not a typo, this is how it is in the document
     */
    protected static final String CIRCLE_INFIX_SIC2 = "entered on";

    /**
     * The infix string for a circle description, between radius an center
     * point.
     */
    protected static final String CIRCLE_INFIX_SIC3 = "radius centred at";

    /**
     * The prefix of a clockwise arc definition.
     */
    protected static final String ARC_PREFIX_CW = "then a clockwise arc radius";

    /**
     * The infix of an arc definition.
     */
    protected static final String ARC_INFIX = "centered on";

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
    protected double processLat(String designator, String latStr)
                                                        throws ParseException {
        if (latStr == null) {
            throw new ParseException(designator, "lat string is null");
        }
        if (latStr.length() < 7) {
            throw new ParseException(designator,
                                     "lat string too short: '" + latStr + "'");
        }
        String str = latStr.trim().toUpperCase();
        double degrees = Double.parseDouble(str.substring(0, 2));
        double minutes = Double.parseDouble(str.substring(2, 4));

        int i = str.indexOf('N');
        if (i == -1) {
            i = str.indexOf('S');
        }
        if (i == -1) {
            throw new ParseException(designator,
                                "latitude string missing N or S designator");
        }
        double seconds = Double.parseDouble(str.substring(4, i));

        double value = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return str.charAt(i) == 'S' ? -value : value;
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
    protected double processLon(String designator, String lonStr)
                                                        throws ParseException {
        if (lonStr == null) {
            throw new ParseException(designator, "lon string is null");
        }
        if (lonStr.length() < 8) {
            throw new ParseException(designator,
                                     "lon string too short: '" + lonStr + "'");
        }
        String str = lonStr.trim().toUpperCase();
        double degrees = Double.parseDouble(str.substring(0, 3));
        double minutes = Double.parseDouble(str.substring(3, 5));

        int i = str.indexOf('E');
        if (i == -1) {
            i = str.indexOf('W');
        }
        if (i == -1) {
            throw new ParseException(designator,
                                "latitude string missing E or W designator");
        }
        double seconds = Double.parseDouble(str.substring(5, i));

        double value = degrees + (minutes / 60.0) + (seconds / 3600.0);

        return str.charAt(i) == 'W' ? -value : value;
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
    protected Point processPoint(String designator, String pointDesc)
                                                        throws ParseException {
        String pd = pointDesc.trim();
        int space = pd.indexOf(" ");
        if (space == -1) {
            throw new ParseException(designator,
                    "error in point description '" + pointDesc + "'");
        }
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
     * @throws ParseException on parse errors
     */
    private void appendBorderSection(Point       borderSectionStart,
                                     Point       borderSectionEnd,
                                     List<Point> borderPoints,
                                     List<Point> pointList)
                                                         throws ParseException {
        if (borderPoints == null) {
            throw new ParseException("no border points provided");
        }

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
     * Process an arc definition.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param arcDef textual arc definition
     * @return a series of points representing the arc
     * @throws ParseException on parsing errors
     */
    List<Point> processArc(String designator,
                           String arcDef) throws ParseException {
        if (!arcDef.startsWith(ARC_PREFIX_CW)) {
            throw new ParseException(designator,
                                     "arc definition prefix missing");
        }

        String str = arcDef.substring(ARC_PREFIX_CW.length()).trim();
        int    ix  = str.indexOf(ARC_INFIX);
        if (ix == -1) {
            throw new ParseException(designator,
                                     "arc definition infix missing");
        }

        String  radiusStr = str.substring(0, ix);
        Distance radius = processDistance(radiusStr);

        String centerStr = str.substring(ix + ARC_INFIX.length());
        Point  center    = processPoint(designator, centerStr);

        // generate a circle with the above center & radius
        Vector<Point> arc = new Vector<Point>();

        double radiusInNm  = radius.inUom(UOM.NM).getDistance();
        double radiusInDeg = radiusInNm / 60.0;
        double radiusLat   = radiusInDeg;
        double radiusLon   = radiusInDeg / Math.cos(
                              Math.toRadians(center.getLatitude()));

        // FIXME: calculate number of points on some required precision metric
        int totalPoints = 32;
        double tpHalf = totalPoints / 2.0;
        for (int i = 0; i < totalPoints; ++i) {
            double theta = Math.PI * i / tpHalf;
            double x = center.getLongitude()
                    + (radiusLon * Math.cos(theta));
            double y = center.getLatitude()
                    + (radiusLat * Math.sin(theta));

            Point p = new Point();
            p.setLongitude(x);
            p.setLatitude(y);

            arc.add(p);
        }

        return arc;
    }

    /**
     * Insert the points of an arc between the last point in the point list
     * and the additional point specified. The points are currently inserted
     * in a clockwise direction.
     *
     * @param arcPoints the points of the arc
     * @param pointList the list to insert the arc points into
     * @param point the point after the arc
     */
    private void insertArcPoints(List<Point> arcPoints,
                                 List<Point> pointList,
                                 Point       point) {
        // find the closes point on the arc to the last point in the list
        Point  lastPoint = pointList.get(pointList.size() - 1);
        double dist      = Double.MAX_VALUE;
        int    startIx   = 0;
        for (int ix = 0; ix < arcPoints.size(); ++ix) {
            Point  p = arcPoints.get(ix);
            double d = lastPoint.distance(p);
            if (d < dist) {
                dist    = d;
                startIx = ix;
            }
        }

        // find the closes point on the arc to the additional point
        dist         = Double.MAX_VALUE;
        int  endIx   = 0;
        for (int ix = 0; ix < arcPoints.size(); ++ix) {
            Point  p = arcPoints.get(ix);
            double d = point.distance(p);
            if (d < dist) {
                dist    = d;
                endIx   = ix;
            }
        }

        // insert points between the found ones, in a clockwise direction
        for (int ix = startIx; ix != endIx; --ix) {
            if (ix < 0) {
                ix += arcPoints.size();
            }
            pointList.add(arcPoints.get(ix));
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
    protected Ring processPointList(String      designator,
                                    String      boundaryDesc,
                                    List<Point> borderPoints)
                                                      throws ParseException {

        if (boundaryDesc.startsWith("The borders of")) {
            Vector<Point> pointList = new Vector<Point>(borderPoints);
            Ring          boundary  = new Ring();

            boundary.setPointList(pointList);

            return boundary;
        }

        Vector<Point> pointList          = new Vector<Point>();
        List<Point>   arcPoints          = null;
        Point         borderSectionStart = null;

        StringTokenizer tokenizer = new StringTokenizer(boundaryDesc, "-");
        while (tokenizer.hasMoreTokens()) {
            String str = tokenizer.nextToken().trim();

            if (str.startsWith("HUNGARY_")) {
                // this is not a point, just a note that the border should be
                // followed
                borderSectionStart = pointList.lastElement();
                continue;
            }

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


            if (arcPoints != null) {
                // put in the arc points between the last point and this one
                insertArcPoints(arcPoints, pointList, p);
                arcPoints = null;
            }

            pointList.add(p);

            int ix = str.indexOf(ARC_PREFIX_CW);
            if (str.contains(ARC_INFIX) && ix != -1) {
                // this is a point definition that refers to a partial arc
                // as part of the airspace definition
                arcPoints = processArc(designator, str.substring(ix).trim());
            }
        }

        // add the first point to close the ring, if not so already
        if (!pointList.get(0).equals(pointList.get(pointList.size() - 1))) {
            pointList.add(new Point(pointList.get(0)));
        }

        Ring boundary = new Ring();
        boundary.setPointList(pointList);

        return boundary;
    }

    /**
     * Process a textual elevation description.
     *
     * @param designator the airspace designator for the point list,
     *                   used to display warnings about the incompleteness
     *                   of the airspace.
     * @param elevDesc the textual elevation description
     * @return the elevation described by elevDesc
     * @throws ParseException in parsing errors
     */
    protected Elevation processElevation(String designator, String elevDesc)
                                                    throws ParseException {
        String ed = elevDesc.trim();

        Elevation elevation = new Elevation();

        if ("GND".equals(ed) || "SFC".equals(ed) || "0 FT".equals(ed)) {
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
            if (j == -1) {
                j = ed.length();
            }
            String uom = ed.substring(i, j).trim();
            if ("FT".equals(uom)) {
                elevation.setUom(UOM.FT);
            } else if ("M".equals(uom) || "MM".equals(uom)) {
                // yes, sometimes its misspelled as MM
                elevation.setUom(UOM.M);
            } else {
                throw new ParseException(designator,
                              "unknown elevation unit if measurement " + uom);
            }

            // get the reference
            String reference = ed.substring(j).trim();
            if ("ALT".equals(reference) || "AMSL".equals(reference)) {
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
     * @throws ParseException on parsing errors.
     */
    protected Distance processDistance(String distDesc) throws ParseException {
        String dd = distDesc.trim().toLowerCase();

        Distance distance = new Distance();

        if (dd.endsWith("km")) {
            distance.setDistance(Double.parseDouble(
                    dd.substring(0, dd.length() - 2)) * 1000.0);
            distance.setUom(UOM.M);
        } else if (dd.endsWith("nm")) {
            distance.setDistance(Double.parseDouble(
                    dd.substring(0, dd.length() - 2)));
            distance.setUom(UOM.NM);
        } else {
            throw new ParseException("unknown distance unit in " + dd);
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
        String prefix = null;
        String infix  = null;

        // find the prefix string
        int p = cd.indexOf(CIRCLE_PREFIX);
        if (p != -1) {
            prefix = CIRCLE_PREFIX;
        } else {
            p = cd.indexOf(CIRCLE_PREFIX2);
            if (p != -1) {
                prefix = CIRCLE_PREFIX2;
            } else {
                throw new ParseException(designator,
                                         "Circle description missing prefix");
            }
        }

        // find the infix string
        int i = cd.indexOf(CIRCLE_INFIX);
        if (i != -1) {
            infix = CIRCLE_INFIX;
        } else {
            i = cd.indexOf(CIRCLE_INFIX_SIC);
            if (i != -1) {
                infix = CIRCLE_INFIX_SIC;
            } else {
                i = cd.indexOf(CIRCLE_INFIX_SIC2);
                if (i != -1) {
                    infix = CIRCLE_INFIX_SIC2;
                } else {
                    i = cd.indexOf(CIRCLE_INFIX_SIC3);
                    if (i != -1) {
                        infix = CIRCLE_INFIX_SIC3;
                    } else {
                        throw new ParseException(designator,
                                            "Circle description missing infix");
                    }
                }
            }
        }

        int prefixLen = prefix.length();
        int infixLen  = infix.length();

        String radiusStr = cd.substring(p + prefixLen, i).trim();
        circle.setRadius(processDistance(radiusStr));

        String centerStr = cd.substring(i + infixLen + 1).trim();
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
    protected String getAirspaceType(String designator) throws ParseException {
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
        // to be implemented by subclasses
        return new Airspace();
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
    public void processEAIP(Node                    eAipNode,
                            List<Point>             borderPoints,
                            List<Airspace>          airspaces,
                            List<Navaid>            navaids,
                            List<Aerodrome>         aerodromes,
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
