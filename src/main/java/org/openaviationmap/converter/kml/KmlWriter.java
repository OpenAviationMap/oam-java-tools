package org.openaviationmap.converter.kml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Boundary;
import org.openaviationmap.model.Circle;
import org.openaviationmap.model.CompoundBoundary;
import org.openaviationmap.model.Frequency;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.Point;
import org.openaviationmap.model.Ring;

/**
 * Generates KML output compatible with Google Earth and O2Interactive's
 * "Magellan Compass".
 *
 * @author rraw@rogers.com
 *
 */
public class KmlWriter {

    /** The FileWriter which is used to write the KML data to. */
    private final FileWriter outputWriter;

    /** The Properties file which contains all of the controlling Property
     * values. */
    private Properties properties = null;

    //@formatter:off
    // The following are Property file names that define how airspaces are
    // drawn.

    /** The property name which defines Class A outline colour. */
    private static final String
        AIRSPACECLASSABOUNDRYCOLOUR = "AirspaceClassABoundryColour";

    /** The property name which defines Class A fill colour. */
    private static final String
        AIRSPACECLASSAFILLCOLOUR = "AirspaceClassAFillColour";

    /** The property name which defines Class B outline colour. */
    private static final String
        AIRSPACECLASSBBOUNDRYCOLOUR = "AirspaceClassBBoundryColour";

    /** The property name which defines Class B fill colour. */
    private static final String
        AIRSPACECLASSBFILLCOLOUR = "AirspaceClassBFillColour";

    /** The property name which defines Class C outline colour. */
    private static final String
        AIRSPACECLASSCBOUNDRYCOLOUR = "AirspaceClassCBoundryColour";

    /** The property name which defines Class C fill colour. */
    private static final String
        AIRSPACECLASSCFILLCOLOUR = "AirspaceClassCFillColour";

    /** The property name which defines Class D outline colour. */
    private static final String
        AIRSPACECLASSDBOUNDRYCOLOUR = "AirspaceClassDBoundryColour";

    /** The property name which defines Class D fill colour. */
    private static final String
        AIRSPACECLASSDFILLCOLOUR = "AirspaceClassDFillColour";

    /** The property name which defines Class E outline colour. */
    private static final String
        AIRSPACECLASSEBOUNDRYCOLOUR = "AirspaceClassEBoundryColour";

    /** The property name which defines Class E fill colour. */
    private static final String
        AIRSPACECLASSEFILLCOLOUR = "AirspaceClassEFillColour";

    /** The property name which defines Class F outline colour. */
    private static final String
        AIRSPACECLASSFBOUNDRYCOLOUR = "AirspaceClassFBoundryColour";

    /** The property name which defines Class F fill colour. */
    private static final String
        AIRSPACECLASSFFILLCOLOUR = "AirspaceClassFFillColour";

    /** The property name which defines Class G outline colour. */
    private static final String
        AIRSPACECLASSGBOUNDRYCOLOUR = "AirspaceClassGBoundryColour";

    /** The property name which defines Class G fill colour. */
    private static final String
        AIRSPACECLASSGFILLCOLOUR = "AirspaceClassGFillColour";

    /** The property name which names the icon file used to display aerodromes
     *  that have no communications. */
    private static final String
        AERODROMENOCOMMICON = "AerodromeNoCommIcon";

    /** The property name which names the icon file used to display aerodromes
     *  that have AFIS communications. */
    private static final String
        AERODROMEAFISICON = "AerodromeAfisIcon";

    /** The property name which names the icon file used to display aerodromes
     *  that have tower communications. */
    private static final String
        AERODROMETOWERICON = "AerodromeTowerIcon";

    /** The property name which names the icon file used to display aerodromes
     *  that have both tower and approach communications. */
    private static final String
        AERODROMETOWERANDAPPROACHICON = "AerodromeTowerAndApproachIcon";

    /** The property name which names the icon file used to display aerodromes
     *  that are heliports. */
    private static final String
        HELIPORTICON = "HeliportIcon";

    /** The property name which defines the root path to the icon files where
     *  the KML file will be used. */
    private static final String
        ICONROOTDIRECTORY = "IconRootDirectory";

    /** The property name which names the icon file used to display aerodromes
     *  which are seaplane ports. */
    private static final String
        SEAPLANEPORTICON = "SeaplanePortIcon";


    // The following property names are used to define Navaid characteristics

    /** The property name which names the icon file used to display a VOR. */
    private static final String
        NAVAIDVORICON = "NavaidVORIcon";

    /** The property name which names the icon file used to display a VORDME. */
    private static final String
        NAVAIDVORDMEICON = "NavaidVORDMEIcon";

    /** The property name which names the icon file used to display a NDB. */
    private static final String
        NAVAIDNDBICON = "NavaidNDBIcon";

    /** The property name which names the icon file used to display a NDBDME. */
    private static final String
        NAVAIDNDBDMEICON = "NavaidNDBDMEIcon";

    /** The property name which names the icon file used to display a TACAN. */
    private static final String
        NAVAIDTACANICON = "NavaidTACANIcon";

    /** The property name which names the icon file used to display a VOT. */
    private static final String
        NAVAIDVOTICON = "NavaidVOTIcon";

    /** The property name which names the icon file used to display a VORTAC. */
    private static final String
        NAVAIDVORTACICON = "NavaidVORTACIcon";

    /** The property name which names the icon file used to display a DME. */
    private static final String
        NAVAIDDMEICON = "NavaidDMEIcon";

    /** The property name which names the icon file used to display a Marker. */
    private static final String
        NAVAIDMARKERICON = "NavaidMarkerIcon";

    /** The property name which names the icon file used to display a LOC. */
    private static final String
        NAVAIDLOCICON = "NavaidLOCcon";

    /** The property name which names the icon file used to display a GP. */
    private static final String
        NAVAIDGPICON = "NavaidGPIcon";

    /** The property name which names the default icon file used. */
    private static final String
        NAVAIDDEFAULTICON = "NavaidDefaultIcon";

    // Filtering related Properties

    /** The property file name used to define the maximum latitude filter. */
    private static final String
        FILTERLATITUDEMAXIMUM = "FilterLatitudeMaximum";

    /** The property file name used to define the minimum latitude filter. */
    private static final String
        FILTERLATITUDEMINIMUM = "FilterLatitudeMinimum";

    /** The property file name used to define the maximum longitude filter. */
    private static final String
        FILTERLONGITUDEMAXIMUM = "FilterLongitudeMaximum";

    /** The property file name used to define the minimum longitude filter. */
    private static final String
        FILTERLONGITUDEMINIMUM = "FilterLongitudeMinimum";

    /** The property file name used to control which type of navaids are
     *  included in the generated KML file.
     *  <p>
     *  The allowed values are "VOR", "VOT", "DME", "LOC", "TACAN", "VORTAC",
     *  "VORDME", "NDB", "NDBDME", or "Marker" */
    private static final String
        FILTERINCLUDENAVAIDS = "FilterIncludeNavaids";

    /** The property file name which is a regular expression which determines
     *  which aerodrome are to be included in the output KML file based on
     *  the aerodrom ICAO ident. */
    private static final String
        FILTERAERODROMEIDENTS = "FilterAerodromeIdents";

    /** The property name which contains a comma separated list of
     *  communication types available at an aerodrome.
     *  <p>
     *  Only aerodroms that contain at least one of the listed communication
     *  types will be included in the KML file.
     *  <p>
     *  Valid types are "AFIS", "GND", "TWR", "Approach" */
    private static final String
        FILTERAERODROMECOMMS = "FilterAerodromeComms";

    //formatter:on

    /**
     * Main constructor.
     *
     * @param outputFile
     *          The String name of the output file to write the data
     *          to.
     *
     * @param propertiesFileName
     *          The full name including the path of the Properties file to use
     *          for this run.
     *
     * @throws IOException If there is any problems writing to the output.
     */
    public KmlWriter(final String outputFile, final String propertiesFileName)
            throws IOException {

        this.properties = this.loadProperties(propertiesFileName);

        //@formatter:off
        //CHECKSTYLE:OFF
        // This table defines the ICON files that will be named for each of the
        // corresponding object types. These ICONS are set in the <Styles>
        // section of the KML file.

        String[][] styles = { { "AerodromeHeliport", this.properties.getProperty(HELIPORTICON) },
                              { "AerodromeNoComm", this.properties.getProperty(AERODROMENOCOMMICON) },
                              { "AerodromeAfis", this.properties.getProperty(AERODROMEAFISICON) },
                              { "AerodromeTower", this.properties.getProperty(AERODROMETOWERICON) },
                              { "AerodromeTowerAndApproach", this.properties.getProperty(AERODROMETOWERANDAPPROACHICON) },
                              { "AerodromeSeaplanePort", this.properties.getProperty(SEAPLANEPORTICON) },

                              { "NavaidDme", this.properties.getProperty(NAVAIDDMEICON) },
                              { "NavaidGp", this.properties.getProperty(NAVAIDGPICON) },
                              { "NavaidLoc", this.properties.getProperty(NAVAIDLOCICON) },
                              { "NavaidMarker", this.properties.getProperty(NAVAIDMARKERICON) },
                              { "NavaidNdbDme", this.properties.getProperty(NAVAIDNDBDMEICON) },
                              { "NavaidNdb", this.properties.getProperty(NAVAIDNDBICON) },
                              { "NavaidTacan", this.properties.getProperty(NAVAIDTACANICON) },
                              { "NavaidVorDme", this.properties.getProperty(NAVAIDVORDMEICON) },
                              { "NavaidVor", this.properties.getProperty(NAVAIDVORICON) },
                              { "NavaidVorTac", this.properties.getProperty(NAVAIDVORTACICON) },
                              { "NavaidVot", this.properties.getProperty(NAVAIDVOTICON) },
                              };
        //CHECKSTYLE:ON
        //@formatter:on

        this.outputWriter = new FileWriter(outputFile);
        this.outputWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
                + "<Document>\n" + "<name>OurAirports</name>\n");

        String icon = this.properties.getProperty(ICONROOTDIRECTORY);

        for (String[] aStyle : styles) {
            this.outputWriter.append("<Style id=\"" + aStyle[0] + "\">\n");
            this.outputWriter.append("<IconStyle>\n");
            this.outputWriter.append("<Icon>\n");
            this.outputWriter.append("<href>" + icon + aStyle[1] + "</href>\n");
            this.outputWriter.append("</Icon>\n");
            this.outputWriter.append("</IconStyle>\n");
            this.outputWriter.append("</Style>\n");
        }
    }

    /**
     * Close the output file and do any required cleanup.
     *
     * @throws IOException If there is a problem closing the file.
     */
    public void close() throws IOException {
        this.outputWriter.append("</Document>\n" + "</kml>\n");
        this.outputWriter.close();
    }

    /**
     * The main driver method that generates the KML creation.
     *
     * @param airspaces
     *          A List of all known (unfiltered) airspaces.
     *
     * @param navaids
     *          A List of all known (unfiltered) navaids.
     *
     * @param aerodromes
     *          A List of all known (unfiltered) aerodromes.
     *
     * @throws IOException
     *          If any IO error occurs while generating the output.
     */
    public void convertToKml(List<Airspace> airspaces, List<Navaid> navaids,
            List<Aerodrome> aerodromes) throws IOException {

        //////////////////////////////////////////////////////////////
        //                 Aerodromes
        this.outputWriter.append(this.kmlAerodromes(aerodromes));

        //////////////////////////////////////////////////////////////
        //                 Airspaces
        this.outputWriter.append(this.kmlAirspaces(airspaces));

        //////////////////////////////////////////////////////////////
        //                 Navigation Aids
        this.outputWriter.append(this.kmlNavaids(navaids));

    }

    /**
     * Generate the KML output for a List of aerodromes.
     *
     * @param aerodromes Is the List<aerodromes> for which output is to be
     *             generated.
     *
     * @return A String that contains all aerodromes formatted for KML output.
     */
    private String kmlAerodromes(final List<Aerodrome> aerodromes) {
        StringBuffer output = new StringBuffer(500000);

        for (Aerodrome aerodrome : aerodromes) {
            output.append(this.kmlAerodrome(aerodrome));
        }
        return output.toString();
    }

    /**
     * Generate the KML output for a single aerodrome.
     *
     * @param aerodrome Is the aerodrome to be generated.
     *
     * @return A String containing a single aerodrome formatted for KML.
     */
    private String kmlAerodrome(final Aerodrome aerodrome) {

        // First check that we want to output this guy
        if (!this.isSelectedByFilter(aerodrome)) {
            return "";
        }

        StringBuffer output = new StringBuffer(1000);

        Frequency towerFreq = aerodrome.getTower();
        String towerFreqDisplay = "";
        if (towerFreq != null) {
            towerFreqDisplay = "T=" + towerFreq.toString();
        }

        Frequency afisFreq = aerodrome.getAfis();
        String afisFreqDisplay = "";
        if (afisFreq != null) {
            afisFreqDisplay = "U=" + afisFreq.toString();
        }

        Frequency atisFreq = aerodrome.getAtis();
        String atisFreqDisplay = "";
        if (atisFreq != null) {
            atisFreqDisplay = "A=" + atisFreq.toString();
        }

        // Choose an Icon set based on the Aerodrome type
        String styleUrl = "";

        switch (aerodrome.getAerodrometype()) {

        case HELIPORT:
        case HOSPITALPORT:
            styleUrl = "AerodromeHeliport";
            break;

        default:
            // Choose an icon based on whether we have a tower frequency or
            // AFIS/Unicom or none.
            if (towerFreq != null) {
                if (aerodrome.getApproach() != null) {
                    styleUrl = "AerodromeTowerAndApproach";
                } else {
                    styleUrl = "AerodromeTower";
                }
            } else if (afisFreq != null) {
                styleUrl = "AerodromeAfis";
            } else {
                styleUrl = "AerodromeNoComm";
            }
        }

        String name = aerodrome.getIcao() + " " + towerFreqDisplay + " "
                + afisFreqDisplay + " " + atisFreqDisplay;
        DecimalFormat cdf = new DecimalFormat("#.#####");
        String coordinates = cdf.format(aerodrome.getArp().getLongitude())
                + "," + cdf.format(aerodrome.getArp().getLatitude());

        output.append("<Placemark>\n");
        output.append("<name>" + name + "</name>\n");
        output.append("<styleUrl>#" + styleUrl + "</styleUrl>");
        output.append("<Point>\n");
        output.append("<coordinates>" + coordinates + "</coordinates>\n");
        output.append("</Point>\n");
        output.append("</Placemark>\n");
        // Format any Aerodrome Airspaces
        List<Airspace> airspaces = aerodrome.getAirspaces();
        if ((airspaces != null) && (!airspaces.isEmpty())) {
            output.append(this.kmlAirspaces(airspaces));
        }
        // Format any Aerodrome Navaids
        List<Navaid> navaids = aerodrome.getNavaids();
        if ((navaids != null) && (!navaids.isEmpty())) {
            output.append(this.kmlNavaids(navaids));
        }

        return output.toString();

    }

    /**
     * Generate the KML output for a List of airspaces.
     *
     * @param airspaces Is the List<Airspace> for which output is to be
     *             generated.
     *
     * @return A String containing all airspaces formatted for KML.
     */
    private String kmlAirspaces(final List<Airspace> airspaces) {
        StringBuffer output = new StringBuffer(1000);

        for (Airspace airspace : airspaces) {
            output.append(this.kmlAirspace(airspace));
        }
        return output.toString();
    }

    /**
     * Generate the KML output for a single airspace.
     *
     * @param airspace Is the Airspace to be generated.
     *
     * @return A String containing a single airspace formatted for KML.
     */
    private String kmlAirspace(final Airspace airspace) {
        StringBuffer output = new StringBuffer(1000);
        String name = airspace.getName();

        String boundaryColour;
        String fillColour;

        if (airspace.getAirspaceClass().equals("A")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSABOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSAFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("B")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSBBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSBFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("C")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSCBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSCFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("D")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSDBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSDFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("E")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSEBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSEFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("F")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSFBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSFFILLCOLOUR);
        } else if (airspace.getAirspaceClass().equals("G")) {
            boundaryColour = this.properties
                    .getProperty(AIRSPACECLASSGBOUNDRYCOLOUR);
            fillColour = this.properties.getProperty(AIRSPACECLASSGFILLCOLOUR);
        } else {
            boundaryColour = null;
            fillColour = null;
        }

        output.append("<Placemark>\n");
        output.append("<name>" + name + "</name>\n");

        Boundary boundary = airspace.getBoundary();

        switch (boundary.getType()) {

        case CIRCLE:
            Circle circle = (Circle) boundary;
            output.append(this.drawPolygon(circle.approximate(16)
                    .getPointList().toArray(new Point[0]), boundaryColour,
                    fillColour));
            break;

        case RING:
            Ring ring = (Ring) boundary;
            output.append(this.drawPolygon(
                    ring.getPointList().toArray(new Point[0]), boundaryColour,
                    fillColour));
            break;

        case COMPOUND:
            CompoundBoundary compoundBoundary = (CompoundBoundary) boundary;
            output.append(this.drawCompoundBoundary(
                    compoundBoundary.getBoundaryList(), boundaryColour,
                    fillColour));
            break;

        default:

        }
        output.append("</Placemark>\n");
        return output.toString();
    }

    /**
     * Generate the KML output for a List of navigation aids.
     *
     * @param navaids
     *          Is the List<Navaid> for which output is to be generated.
     *
     * @return  A String containing all navaids formatted for KML.
     */
    private String kmlNavaids(final List<Navaid> navaids) {
        StringBuffer output = new StringBuffer(1000);

        for (Navaid navaid : navaids) {
            output.append(this.kmlNavaid(navaid));
        }
        return output.toString();
    }

    /**
     * Generate the KML output for a single navigation aid.
     *
     * @param navaid Is the Navaid to be generated.
     *
     * @return A String containing a single navaid formatted for KML.
     */
    private String kmlNavaid(final Navaid navaid) {

        // First check that we want to output this guy
        if (!this.isSelectedByFilter(navaid)) {
            return "";
        }

        StringBuffer output = new StringBuffer(1000);

        // Choose an Icon set based on the Aerodrome type
        String styleUrl;

        switch (navaid.getType()) {

        case VOR:
            styleUrl = "NavaidVor";
            break;

        case VORDME:
            styleUrl = "NavaidVorDme";
            break;

        case VOT:
            styleUrl = "NavaidVot";
            break;

        case DME:
            styleUrl = "NavaidDme";
            break;

        case NDB:
            styleUrl = "NavaidNdb";
            break;

        case NDBDME:
            styleUrl = "NavaidNdbDme";
            break;

        case TACAN:
            styleUrl = "NavaidTacan";
            break;

        case VORTAC:
            styleUrl = "NavaidVorTac";
            break;

        case MARKER:
            styleUrl = "NavaidMarker";
            break;

        case LOC:
            styleUrl = "NavaidLoc";
            break;

        case GP:
            styleUrl = "NavaidGp";
            break;

        case DESIGNATED:
            styleUrl = "NavaidDefault";
            break;

        default:
            styleUrl = "NavaidDefault";
            break;

        }

        String name = navaid.getName() + " " + navaid.getFrequency().toString();
        DecimalFormat cdf = new DecimalFormat("#.#####");
        String coordinates = cdf.format(navaid.getLongitude()) + ","
                + cdf.format(navaid.getLatitude());

        output.append("<Placemark>\n");
        output.append("<name>" + name + "</name>\n");
        output.append("<styleUrl>#" + styleUrl + "</styleUrl>");
        output.append("<Point>\n");
        output.append("<coordinates>" + coordinates + "</coordinates>\n");
        output.append("</Point>\n");
        output.append("</Placemark>\n");

        return output.toString();
    }

    /**
     * Draw a closed polygon with optional line style and fill colour.
     *
     * @param points         Is an array of points that define the polygon. The
     *                       last point in the array must equal the first point
     *                       if it doesn't, a closing point will be
     *                       automatically generated which is the same as the
     *                       first point.
     *
     * @param boundaryColour Is the colour of the boundary or border. It is a
     *                       String of the form "w,aabbggrr" where "w" is the
     *                       line width in pixels, "aa" is the alpha channel or
     *                       transparency, it is a hexadecimal number between 00
     *                       and ff. "bb", "gg" and "rr" are hexadecimal number
     *                       representing the blue green and red components of
     *                       the colour.
     *
     * @param fillColour     The fill colour expected in the form "aabbggrr".
     *
     * @return               A String containing the KML
     *
     */
    private String drawPolygon(final Point[] points,
            final String boundaryColour, final String fillColour) {
        StringBuffer output = new StringBuffer(1000);

        String tFillColour = "00000000";
        if ((fillColour != null) && (fillColour.length() != 0)) {
            tFillColour = fillColour;
        }

        String tBoundryColour = "00000000";
        String tBoundryWidth = "2";
        if ((boundaryColour != null) && (boundaryColour.length() != 0)) {
            String[] t = boundaryColour.split(",");
            tBoundryWidth = t[0].trim();
            tBoundryColour = t[1].trim();
        }

        output.append("<Style>\n");
        output.append("<LineStyle>\n");
        output.append("<color>" + tBoundryColour + "</color>\n");
        output.append("<width>" + tBoundryWidth + "</width>\n");
        output.append("</LineStyle>\n");
        output.append("<PolyStyle>\n");
        output.append("<color>" + tFillColour + "</color>\n");
        output.append("<colorMode>Normal</colorMode>\n");
        output.append("</PolyStyle>\n");
        output.append("</Style>\n");
        output.append("<Polygon>\n");
        output.append("<outerBoundaryIs>\n");
        output.append("<LinearRing>\n");
        output.append("<coordinates>");
        DecimalFormat df = new DecimalFormat("#.####");
        for (Point point : points) {
            output.append(df.format(point.getLongitude()) + ","
                    + df.format(point.getLatitude()) + " ");
        }
        if (!points[0].equals(points[points.length - 1])) {
            output.append(df.format(points[0].getLongitude()) + ","
                    + df.format(points[0].getLatitude()) + " ");
        }
        output.append("</coordinates>\n");
        output.append("</LinearRing>\n");
        output.append("</outerBoundaryIs>\n");
        output.append("</Polygon>\n");

        return output.toString();
    }

    /**
     * Draw a closed polygon with optional line style and fill colour.
     *
     * @param boundaries
     *          Is an array of boundaries that define the polygon.
     *
     * @param boundaryColour
     *          Is the colour of the boundary or border. It is a String of the
     *          form "w,aabbggrr" where "w" is the line width in pixels, "aa" is
     *          the alpha channel or transparency, it is a hexadecimal number
     *          between 00 and ff. "bb", "gg" and "rr" are hexadecimal number
     *          representing the blue green and red components of the colour.
     *
     * @param fillColour
     *          The fill colour expected in the form "aabbggrr".
     *
     * @return  A String containing the KML
     *
     */
    private String drawCompoundBoundary(final List<Boundary> boundaries,
            final String boundaryColour, final String fillColour) {
        StringBuffer output = new StringBuffer(1000);

        String tFillColour = "00000000";
        if ((fillColour != null) && (fillColour.length() != 0)) {
            tFillColour = fillColour;
        }

        String tBoundryColour = "00000000";
        String tBoundryWidth = "2";
        if ((boundaryColour != null) && (boundaryColour.length() != 0)) {
            String[] t = boundaryColour.split(",");
            tBoundryWidth = t[0].trim();
            tBoundryColour = t[1].trim();
        }

        output.append("<Style>\n");
        output.append("<LineStyle>\n");
        output.append("<color>" + tBoundryColour + "</color>\n");
        output.append("<width>" + tBoundryWidth + "</width>\n");
        output.append("</LineStyle>\n");
        output.append("<PolyStyle>\n");
        output.append("<color>" + tFillColour + "</color>\n");
        output.append("<colorMode>Normal</colorMode>\n");
        output.append("</PolyStyle>\n");
        output.append("</Style>\n");
        Point[] points = null;
        for (Boundary boundary : boundaries) {
            if (boundary instanceof Circle) {
                points = ((Circle) boundary).approximate().getPointList()
                        .toArray(new Point[0]);
            } else if (boundary instanceof Ring) {
                points = ((Ring) boundary).getPointList().toArray(new Point[0]);
            }
            output.append("<Polygon>\n");
            output.append("<outerBoundaryIs>\n");
            output.append("<LinearRing>\n");
            output.append("<coordinates>");
            DecimalFormat df = new DecimalFormat("#.####");
            for (Point point : points) {
                output.append(df.format(point.getLongitude()) + ","
                        + df.format(point.getLatitude()) + " ");
            }
            if (!points[0].equals(points[points.length - 1])) {
                output.append(df.format(points[0].getLongitude()) + ","
                        + df.format(points[0].getLatitude()) + " ");
            }
            output.append("</coordinates>\n");
            output.append("</LinearRing>\n");
            output.append("</outerBoundaryIs>\n");
            output.append("</Polygon>\n");
        }

        return output.toString();
    }

    /**
     * Establish default property values then read and apply the given
     * Properties file.
     *
     * @param propertiesFileName
     *          The full path name of the Property file to apply. This may be
     *          specified as null or a zero length String.
     *
     * @return  A new Properties file with all Property values either set from
     *          the file or set to the default value.
     *
     * @throws FileNotFoundException
     *          If the given Property file does not exist.
     *
     * @throws IOException
     *          If the Properties file cannot be read or encounters an error.
     */
    private Properties loadProperties(final String propertiesFileName)
            throws FileNotFoundException, IOException {

        Properties properties = new Properties();

        // Set the defaults. Boundary lines have two parts, the first is the
        // line width in pixels, the second is the line colour. Colours are in
        // KML alpha,blue,green,red format. If the definition is set to the
        // empty String then that item will not be drawn.

        // Airspaces
        properties.setProperty(AIRSPACECLASSABOUNDRYCOLOUR, "");
        properties.setProperty(AIRSPACECLASSAFILLCOLOUR, "");
        properties.setProperty(AIRSPACECLASSBBOUNDRYCOLOUR, "");
        properties.setProperty(AIRSPACECLASSBFILLCOLOUR, "");
        properties.setProperty(AIRSPACECLASSCBOUNDRYCOLOUR, "2,ff0000ff");
        properties.setProperty(AIRSPACECLASSCFILLCOLOUR, "400000ff");
        properties.setProperty(AIRSPACECLASSDBOUNDRYCOLOUR, "2,ffff0000");
        properties.setProperty(AIRSPACECLASSDFILLCOLOUR, "40ff0000");
        properties.setProperty(AIRSPACECLASSEBOUNDRYCOLOUR, "2,ff00ff00");
        properties.setProperty(AIRSPACECLASSEFILLCOLOUR, "4000ff00");
        properties.setProperty(AIRSPACECLASSFBOUNDRYCOLOUR, "2,ff00ffff");
        properties.setProperty(AIRSPACECLASSFFILLCOLOUR, "4000ffff");
        properties.setProperty(AIRSPACECLASSGBOUNDRYCOLOUR, "");
        properties.setProperty(AIRSPACECLASSGFILLCOLOUR, "");

        // Icons
        properties.setProperty(ICONROOTDIRECTORY,
                "file:///C:/!Programming/OpenAviationMaps/Kml_Icons/");
        properties.setProperty(AERODROMENOCOMMICON, "Aerodrome_Black.png");
        properties.setProperty(AERODROMEAFISICON, "Aerodrome_Yellow.png");
        properties.setProperty(AERODROMETOWERICON, "Aerodrome_Green.png");
        properties.setProperty(AERODROMETOWERANDAPPROACHICON,
                "Aerodrome_Red.png");
        properties.setProperty(HELIPORTICON, "Heliport_Black.png");
        properties.setProperty(SEAPLANEPORTICON, "Aerodrome_Black.png");

        // Navaid Icons
        properties.setProperty(NAVAIDVORICON, "VOR.png");
        properties.setProperty(NAVAIDVORDMEICON, "VOR-DME.png");
        properties.setProperty(NAVAIDNDBICON, "NDB.png");
        properties.setProperty(NAVAIDNDBDMEICON, "NDB-DME.png");
        properties.setProperty(NAVAIDTACANICON, "TACAN.png");
        properties.setProperty(NAVAIDVORTACICON, "VORTAC.png");
        properties.setProperty(NAVAIDDMEICON, "DME.png");

        // Filtering related Properties
        properties.setProperty(FILTERLATITUDEMAXIMUM, "90.0");
        properties.setProperty(FILTERLATITUDEMINIMUM, "-90.0");
        properties.setProperty(FILTERLONGITUDEMAXIMUM, "180.0");
        properties.setProperty(FILTERLONGITUDEMINIMUM, "-180.0");
        properties.setProperty(FILTERINCLUDENAVAIDS, "");
        properties.setProperty(FILTERAERODROMEIDENTS, "");
        properties.setProperty(FILTERAERODROMECOMMS, "");

        // Now load the external Properties file which is a standard Java
        // Properties file with the options and settings that we support.
        if ((propertiesFileName != null) && (propertiesFileName.length() != 0)) {
            FileInputStream propertyFile = new FileInputStream(
                    propertiesFileName);
            properties.load(propertyFile);
        }

        return properties;

    }

    /**
     * Given either a Navaid, Airspace or Aerodrome, apply the filters.
     *
     * @param anObject
     *          The object (Aerodrome, Navaid or Airspace) to be checked.
     *
     * @return  "true" if the object is within our selection filter otherwise
     *          "false".
     */
    private boolean isSelectedByFilter(Object anObject) {
        boolean selected = true;
        double maxLatitude = Double.parseDouble(this.properties
                .getProperty(FILTERLATITUDEMAXIMUM));
        double minLatitude = Double.parseDouble(this.properties
                .getProperty(FILTERLATITUDEMINIMUM));
        double maxLongitude = Double.parseDouble(this.properties
                .getProperty(FILTERLONGITUDEMAXIMUM));
        double minLongitude = Double.parseDouble(this.properties
                .getProperty(FILTERLONGITUDEMINIMUM));

        if ((anObject instanceof Navaid) || (anObject instanceof Aerodrome)
                || (anObject instanceof Airspace)) {
            // We have an object that we'll apply the filters to

            if (anObject instanceof Navaid) {
                // We have a Navaid. Filter of location and type
                Navaid navaid = (Navaid) anObject;
                double latitude = navaid.getLatitude();
                double longitude = navaid.getLongitude();
                if ((latitude < minLatitude) || (latitude > maxLatitude)
                        || (longitude < minLongitude)
                        || (longitude > maxLongitude)) {
                    selected = false;
                } else {
                    String[] includeNavaids = this.properties.getProperty(
                            FILTERINCLUDENAVAIDS).split(",");
                    if ((includeNavaids.length != 0)
                            && !((includeNavaids.length == 1) && (includeNavaids[0]
                                    .length() == 0))) {
                        Navaid.Type navaidType = navaid.getType();
                        selected = false;
                        for (String includeNavaid : includeNavaids) {
                            if (navaidType == Navaid.Type.getType(includeNavaid
                                    .toUpperCase())) {
                                selected = true;
                                break;
                            }
                        }
                    }
                }
            }

            else if (anObject instanceof Aerodrome) {
                // We have a Aerodrome. Filter of location, Ident and Comm types
                //TODO Add Comm Types
                Aerodrome aerodrome = (Aerodrome) anObject;
                double latitude = aerodrome.getArp().getLatitude();
                double longitude = aerodrome.getArp().getLongitude();
                if ((latitude < minLatitude) || (latitude > maxLatitude)
                        || (longitude < minLongitude)
                        || (longitude > maxLongitude)) {
                    selected = false;
                } else {
                    String identRegexp = this.properties
                            .getProperty(FILTERAERODROMEIDENTS);
                    String aerodromeIdent = aerodrome.getIcao();
                    if (aerodromeIdent.startsWith("C")) {
                        int i = 1;
                    }
                    if (!aerodromeIdent.matches(identRegexp)) {
                        selected = false;
                    }
                }
            }

            else if (anObject instanceof Airspace) {

            }

        }

        return selected;
    }
}
