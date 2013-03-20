package org.openaviationmap.converter.ourairports;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Distance;
import org.openaviationmap.model.Elevation;
import org.openaviationmap.model.ElevationReference;
import org.openaviationmap.model.Point;
import org.openaviationmap.model.Runway;
import org.openaviationmap.model.SurfaceType;
import org.openaviationmap.model.UOM;

/**
 * A simple data holder class to represent the data parsed from the OurAirports
 * "runways.csv" file.
 *
 * @author Raymond
 *
 */
public class RunwayCsv {

    /** The Properties as passed in from the main reader. */
    private final Properties properties;

    /** Tracks the record number we're curently dealing with. */
    private static int recordNumber = 1;

    /** The name of the CSV file being processed by this class. */
    private static final String FILENAME = "runways.csv";

    /** The "OurAirports" record id of this record. */
    protected final int id;

    /** The record id of the "OurAirports" airport record to which this
     *  frequency applies.
     */
    protected final String airport_ref;

    /** The icao identification of the airport to which this frequency applies.
     */
    protected final String airport_ident;

    /** The length of the runway in feet. */
    protected final double length_ft;

    /** The width of the runwat in feet. */
    protected final double width_ft;

    /** The runway surface type. */
    protected final String surface;

    /** Indicates if the runway is ligthed or not. */
    protected final boolean lighted;

    /** Indicates if the runway is closed or not. */
    protected final boolean closed;

    /** Generally indicates the low bearing runway (i.e. 1 to 18). */
    protected String le_ident;

    /** The latitude of the end of the runway. */
    protected double le_latitude_deg;

    /** The longitude of the end of the runway. */
    protected double le_longitude_deg;

    /** The elevation of the runway in feet. */
    protected double le_elevation_ft;

    /** If present, this will indicate the true magnetic heading. */
    protected double le_heading_degT;

    /** The displacement from the end of the runway to the threshold. */
    protected double le_displaced_threshold_ft;

    /** Generally indicates the high bearing runway (i.e. 19 to 36). */
    protected String he_ident;

    /** The latitude of the end of the runway. */
    protected double he_latitude_deg;

    /** The longitude of the end of the runway. */
    protected double he_longitude_deg;

    /** The elevation of the runway in feet. */
    protected double he_elevation_ft;

    /** If present, this will indicate the true magnetic heading. */
    protected double he_heading_degT;

    /** The displacement from the end of the runway to the threshold. */
    protected double he_displaced_threshold_ft;

    /**
    * Creates a new instance of a Runway from the provided set of values.
     * <p>
     * Very little actual validation of the values occur and many of the values
     * may be blank.
     *
     * @param csValues
     *            Is a String array containing the values to use.
     *
     * @throws ParseException
     *            If any of the CSV values cannot be parsed.
     */
    protected RunwayCsv(final String[] csValues, final Properties properties)
            throws ParseException {

        this.properties = properties;

        ++RunwayCsv.recordNumber;

        try {
            // If the CSV file doesn't have a value for all columns then the
            // trailing values won't be present so I set a default value which
            // will still convert to a double but is distinguishable later.
            // Similarly, any other zero length value that must be converted
            // to a double are defaulted.
            String[] tCsVariables = {"", "", "", "-999", "-999", "", "", "",
                    "", "-999", "-999", "-999", "-999", "-999", "", "-999",
                    "-999", "-999", "-999", "-999"};
            for (int i = 0; i < csValues.length; ++i) {
                if (csValues[i].length() != 0) {
                    tCsVariables[i] = csValues[i];
                }
            }

            this.id = Integer.parseInt(tCsVariables[0]);
            this.airport_ref = tCsVariables[1];
            this.airport_ident = tCsVariables[2].trim().toUpperCase();
            this.length_ft = Double.parseDouble(tCsVariables[3]);
            this.width_ft = Double.parseDouble(tCsVariables[4]);
            this.surface = tCsVariables[5].trim().toUpperCase();
            this.lighted = (tCsVariables[6].equals("1")) ? true : false;
            this.closed = (tCsVariables[7].equals("1")) ? true : false;
            this.le_ident = tCsVariables[8];
            this.le_latitude_deg = Double.parseDouble(tCsVariables[9]);
            this.le_longitude_deg = Double.parseDouble(tCsVariables[10]);
            this.le_elevation_ft = Double.parseDouble(tCsVariables[11]);
            this.le_heading_degT = Double.parseDouble(tCsVariables[12]);
            this.le_displaced_threshold_ft = Double
                    .parseDouble(tCsVariables[13]);
            this.he_ident = tCsVariables[14];
            this.he_latitude_deg = Double.parseDouble(tCsVariables[15]);
            this.he_longitude_deg = Double.parseDouble(tCsVariables[16]);
            this.he_elevation_ft = Double.parseDouble(tCsVariables[17]);
            this.he_heading_degT = Double.parseDouble(tCsVariables[18]);
            this.he_displaced_threshold_ft = Double
                    .parseDouble(tCsVariables[19]);
        } catch (Exception e) {
            throw new ParseException("Parse error in OurAirports file \""
                    + FILENAME + "\" at record " + this.recordNumber + ".", e);
        }

        // Now do some essential validation of the data
        // Make sure the ident is a valid format

        // Convert N, E, S, NE etc into standard runway numbers
        String[] compassPoints = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        String[] runwayNumbers = {"36", "04", "09", "13", "18", "22", "27",
                "31"};
        String[] oppositRunwayNumbers = {"18", "22", "27", "31", "36", "04",
                "09", "13"};
        for (int i = 0; i < 8; ++i) {
            if (this.le_ident.equals(compassPoints[i])) {
                this.le_ident = runwayNumbers[i];
                this.he_ident = oppositRunwayNumbers[i];
                break;
            }
        }
        if (!this.le_ident.matches("[0-9]?[0-9][RCL]?")) {
            // We'll just ignore Heliport pads for now
            if (this.le_ident.matches("^H\\d$")) {
                return;
            } else {
                throw new ParseException("Invalid le runway ident = \""
                        + this.le_ident + "\" at record = "
                        + RunwayCsv.recordNumber);
            }
        }
        // Check that it is the low heading runway and if not, swap it
        // Check if we need to prepend a 0
        if (!this.le_ident.matches("[0-9][0-9][RCL]?")) {
            this.le_ident = "0" + this.le_ident;
        }
        if (Integer.parseInt(this.le_ident.substring(0, 2)) > 18) {
            // We need to swap the low and the high
            String tempS = this.le_ident;
            this.le_ident = this.he_ident;
            this.he_ident = tempS;
            double tempD = this.le_displaced_threshold_ft;
            this.le_displaced_threshold_ft = this.he_displaced_threshold_ft;
            this.he_displaced_threshold_ft = tempD;
            tempD = this.le_elevation_ft;
            this.le_elevation_ft = this.he_elevation_ft;
            this.he_elevation_ft = tempD;
            tempD = this.le_heading_degT;
            this.le_heading_degT = this.he_heading_degT;
            this.he_heading_degT = tempD;
            tempD = this.le_latitude_deg;
            this.le_latitude_deg = this.he_latitude_deg;
            this.he_latitude_deg = tempD;
            tempD = this.le_longitude_deg;
            this.le_longitude_deg = this.he_longitude_deg;
            this.he_longitude_deg = tempD;
            // Make sure the ident is a valid format
            if (!this.le_ident.matches("[0-9]?[0-9][RCL]?")) {
                throw new ParseException("Invalid he runway ident = \""
                        + this.le_ident + "\" at record = "
                        + RunwayCsv.recordNumber);
            }
        }
        // If there is a ident for the opposite end of the runway, make sure its
        // valid. If not, generate one.
        // First we prepend a leading zero to the le ident if we need to
        if (!this.le_ident.matches("[0-9][0-9][RCL]?")) {
            this.le_ident = "0" + this.le_ident;
        }
        String heIdent = ""
                + (Integer.parseInt(this.le_ident.substring(0, 2)) + 18);
        if (this.le_ident.length() == 3) {
            // Add the suffix character
            String[] heSuffix = {"L", "R", "C"};
            heIdent += heSuffix["RLC".indexOf(this.le_ident.substring(2, 3))];
        }
        if (this.he_ident.length() == 0) {
            this.he_ident = heIdent;
        } else {
            if (!this.he_ident.equals(heIdent)) {
                throw new ParseException("Invalid he runway ident = \""
                        + this.he_ident + "\" at record = "
                        + RunwayCsv.recordNumber + ". Expected \"" + heIdent
                        + "\".");
            }
        }
    }

    /**
     * Called to verify that the header line of the CSV file is what we expect.
     * <p>
     * This is a simple check that the input file is in fact an "OurAirports"
     * style CSV file.
     *
     * @param csVariable
     *            A String array which contains the column headers for
     *            the airports.csv file.
     *
     * @throws OurAirportsValidationException
     *            If the file headers do not verify
     */
    protected static void verifyHeaders(String[] csVariable)
            throws OurAirportsValidationException {

        // The expected csv header values.
        final String[] headers = {"id", "airport_ref", "airport_ident",
                "length_ft", "width_ft", "surface", "lighted", "closed",
                "le_ident", "le_latitude_deg", "le_longitude_deg",
                "le_elevation_ft", "le_heading_degT",
                "le_displaced_threshold_ft", "he_ident", "he_latitude_deg",
                "he_longitude_deg", "he_elevation_ft", "he_heading_degT",
                "he_displaced_threshold_ft"};

        if (csVariable.length != headers.length) {
            // This is likely not an OurAirports file. In this case, we're
            // throwing an Exception instead of a ParseException so that
            // execuation fails completely.
            throw new OurAirportsValidationException("Error validating "
                    + FILENAME + "." + " Expected " + headers.length
                    + " columns but found " + csVariable.length + ".");
        }

        for (int i = 0; i < headers.length; ++i) {
            if (!csVariable[i].equals(headers[i])) {
                throw new OurAirportsValidationException("Error validating "
                        + FILENAME + "." + " Expected column " + i
                        + " header value to be \"" + headers[i]
                        + "\" but found \"" + csVariable[i] + "\".");
            }
        }
    }

    /**
     * Converts a single OurAirports "RunwaysCsv" object into one of two OAM
     * "Runway" objects.
     *
     * @return An ArrayList<Runway> with two Oam style Runways in it. These are
     *             the two opposing runways from the single OurAirports runway.
     */
    public ArrayList<Runway> convertToOamRunways() {

        ArrayList<Runway> runways = new ArrayList<Runway>(2);
        DecimalFormat df = new DecimalFormat("00");

        // Only create runway objects if we have a valid heading
        if (this.le_heading_degT != -999.0d) {
            {
                // Do the primary runway
                Runway runway = new Runway();
                // runway.setAsda(asda);
                runway.setBearing(this.le_heading_degT);
                runway.setDesignator(this.le_ident);
                runway.setElevation(new Elevation(this.le_elevation_ft, UOM.FT,
                        ElevationReference.MSL));
                Point runwayEndpoint = new Point(this.le_latitude_deg,
                        this.le_longitude_deg);
                runway.setEnd(runwayEndpoint);
                //runway.setLda();
                runway.setLength(new Distance(this.length_ft, UOM.FT));
                //runway.setSlope(slope);
                //TODO Consider a better mapping of runway surface type
                SurfaceType surface = SurfaceType.GRASS;
                String[] hardSurfaces = {"ASP", "ALU", "ASF", "CON"};
                for (String aHardSurface : hardSurfaces) {
                    if (this.surface.startsWith(aHardSurface)) {
                        surface = SurfaceType.ASPHALT;
                        break;
                    }
                }
                runway.setSurface(surface);
                Point threshold = runwayEndpoint.displace(new Distance(
                        this.le_displaced_threshold_ft, UOM.FT),
                        this.le_heading_degT);
                runway.setThreshold(threshold);
                // runway.setToda(toda);
                // runway.setTora(tora);
                runway.setWidth(new Distance(this.width_ft, UOM.FT));
                runways.add(runway);
            }

            {
                // Do the high bearing runway
                Runway runway = new Runway();
                /* Set defaults for missing values */
                // Default the runway heading to the opposite heading plus 180
                double bearing = this.he_heading_degT;
                if (bearing == -999.0d) {
                    bearing = this.le_heading_degT + 180.0d;
                    bearing = (bearing >= 360.0d) ? bearing - 360.0d : bearing;
                }
                runway.setDesignator(this.he_ident);

                // Default the elevation to the same as the opposite end
                double elevation = (this.he_elevation_ft == -999.0d) ? this.le_elevation_ft
                        : this.he_elevation_ft;
                // Default the runway endpoint to the end of the opposite runway
                // displaced by the runway length and bearing of the opposite end.
                Point runwayEndpoint = null;
                if ((this.he_latitude_deg != -999.0d)
                        || (this.he_longitude_deg != -999.0d)) {
                    runwayEndpoint = new Point(this.he_latitude_deg,
                            this.he_longitude_deg);
                } else {
                    runwayEndpoint = new Point(this.le_latitude_deg,
                            this.le_longitude_deg);
                    runwayEndpoint = runwayEndpoint.displace(new Distance(
                            this.length_ft, UOM.FT), this.le_heading_degT);
                }
                //TODO Consider a better mapping of runway surface type
                SurfaceType surface = SurfaceType.GRASS;
                String[] hardSurfaces = {"ASP", "ALU", "ASF", "CON"};
                for (String aHardSurface : hardSurfaces) {
                    if (this.surface.startsWith(aHardSurface)) {
                        surface = SurfaceType.ASPHALT;
                        break;
                    }
                }
                runway.setSurface(surface);
                // Default the threshold displacement to be the same as the
                // opposite end
                Point threshold = null;
                if (this.he_displaced_threshold_ft != -999.0d) {
                    threshold = runwayEndpoint.displace(new Distance(
                            this.he_displaced_threshold_ft, UOM.FT), bearing);
                } else {
                    threshold = runwayEndpoint.displace(new Distance(
                            this.le_displaced_threshold_ft, UOM.FT), bearing);
                }

                // runway.setAsda(asda);
                runway.setBearing(bearing);
                runway.setElevation(new Elevation(elevation, UOM.FT,
                        ElevationReference.MSL));
                runway.setEnd(runwayEndpoint);
                //runway.setLda();
                runway.setLength(new Distance(this.length_ft, UOM.FT));
                //runway.setSlope(slope);
                runway.setSurface(surface);
                runway.setThreshold(threshold);
                // runway.setToda(toda);
                // runway.setTora(tora);
                runway.setWidth(new Distance(this.width_ft, UOM.FT));
                runways.add(runway);
            }
        }

        return runways;
    }
}
