package org.openaviationmap.converter.ourairports;

import java.util.ArrayList;
import java.util.Properties;

import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Elevation;
import org.openaviationmap.model.ElevationReference;
import org.openaviationmap.model.Frequency;
import org.openaviationmap.model.MagneticVariation;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.UOM;

/**
 * A simple data holder class to represent the data parsed from the OurAirports
 * "navaids.csv" file.
 *
 * @author Raymond Raw
 *
 */
public class NavaidCsv {

    /** The Properties as passed in from the main reader. */
    private final Properties properties;

    /** The name of the CSV file being processed by this class. */
    private static final String fileName = "navaids.csv";

    /** The "OurAirports" record id of this record. */
    public final int id;

    /** Not sure what this is. */
    public final String filename;

    /** The navaid's ident. */
    public final String ident;

    /** The common name for this navaid. */
    public final String name;

    /**
     * The navaid type which is one of "DME", "NDB", "NDB-DME", "TACAN", "VOR",
     * "VOR-DME", or "VORTAC".
    */
    public final String type;

    /** The navaid's frequency in khz. */
    public final double frequency_khz;

    /** The navaid's latitude. */
    public final double latitude_deg;

    /** The navaid's longitude. */
    public final double longitude_deg;

    /** The navaids elevation in feet. */
    public final int elevation_ft;

    /** The airport's ISO country identifier. */
    public final String iso_country;

    /** The frequency in khz of an associated DME. */
    public final double dme_frequency_khz;

    /** The DME channel. */
    public final String dme_channel;

    /** The latitude of the assicated DME. */
    public final double dme_latitude_deg;

    /** The longitude of the associated DME. */
    public final double dme_longitude_deg;

    /** The elevation in feet of the associated DME. */
    public final int dme_elevation_ft;

    /** Not sure what this is. */
    public final double slaved_variation_deg;

    /** The magnetic variation at the navaid's location. */
    public final double magnetic_variation_deg;

    /**
     * The usage type for this navaid which is one of "BOTH", "HI", "LOW",
     * "RNAV", "TERMINAL" or blank.
     */
    public final String usageType;

    /** The Navaids power as "HIGH", "MEDIUM", or "LOW". */
    public final String power;

    /** The record id of the "OurAirports" airport record to which this
     *  frequency applies.
     */
    public final String associated_airport;

    /**
     * Creates a new instance of a Navaid from the provided set of values.
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
    protected NavaidCsv(final String[] csValues, final Properties properties)
            throws ParseException {

        this.properties = properties;

        try {
            // If the CSV file doesn't have a value for all columns then the
            // trailing values won't be present. We'll default the values.
            // The default value is choosen so that it will still convert to a
            // double or whatever but is distinguishable later. Similarly, any
            // other zero length value that must be converted to a double are
            // defaulted.
            String[] tCsVariables = {"0", "", "", "", "", "-999", "-999",
                    "-999", "-999", "", "-999", "", "-999", "-999", "-999",
                    "-999", "-999", "", "-", ""};
            for (int i = 0; i < csValues.length; ++i) {
                if (csValues[i].length() != 0) {
                    tCsVariables[i] = csValues[i];
                }
            }

            this.id = Integer.parseInt(tCsVariables[0]);
            this.filename = tCsVariables[1];
            this.ident = tCsVariables[2];
            this.name = tCsVariables[3];
            this.type = tCsVariables[4];
            this.frequency_khz = Double.parseDouble(tCsVariables[5]);
            this.latitude_deg = Double.parseDouble(tCsVariables[6]);
            this.longitude_deg = Double.parseDouble(tCsVariables[7]);
            this.elevation_ft = Integer.parseInt(tCsVariables[8]);
            this.iso_country = tCsVariables[9];
            this.dme_frequency_khz = Double.parseDouble(tCsVariables[10]);
            this.dme_channel = tCsVariables[11];
            this.dme_latitude_deg = Double.parseDouble(tCsVariables[12]);
            this.dme_longitude_deg = Double.parseDouble(tCsVariables[13]);
            this.dme_elevation_ft = Integer.parseInt(tCsVariables[14]);
            this.slaved_variation_deg = Double.parseDouble(tCsVariables[15]);
            this.magnetic_variation_deg = Double.parseDouble(tCsVariables[16]);
            this.usageType = tCsVariables[17];
            this.power = tCsVariables[18];
            this.associated_airport = tCsVariables[19];
        } catch (Exception e) {
            throw new ParseException("Parse error in OurAirports file \""
                    + fileName + "\".", e);
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
        final String[] headers = {"id", "filename", "ident", "name", "type",
                "frequency_khz", "latitude_deg", "longitude_deg",
                "elevation_ft", "iso_country", "dme_frequency_khz",
                "dme_channel", "dme_latitude_deg", "dme_longitude_deg",
                "dme_elevation_ft", "slaved_variation_deg",
                "magnetic_variation_deg", "usageType", "power",
                "associated_airport"};

        if (csVariable.length != headers.length) {
            // This is likely not an OurAirports file. In this case, we're
            // throwing an Exception instead of a ParseException so that
            // execuation fails completely.
            throw new OurAirportsValidationException("Error validating "
                    + fileName + "." + " Expected " + headers.length
                    + " columns but found " + csVariable.length + ".");
        }

        for (int i = 0; i < headers.length; ++i) {
            if (!csVariable[i].equals(headers[i])) {
                throw new OurAirportsValidationException("Error validating "
                        + fileName + "." + " Expected column " + i
                        + " header value to be \"" + headers[i]
                        + "\" but found \"" + csVariable[i] + "\".");
            }
        }
    }

    /**
     * Converts a this OurAirports "NavaidCsv" object into an OAM "Navaid"
     * object.
     *
     * @return The converted Aerodrome object.
     */
    public ArrayList<Navaid> convertToOamNavaids() {
        ArrayList<Navaid> navaids = new ArrayList<Navaid>(2);

        // Its supported as is, just add it.
        Navaid aNavaid = new Navaid();
        //aNavaid.setAngle(angle);
        aNavaid.setDmeChannel(this.dme_channel.trim());
        aNavaid.setElevation(new Elevation(this.elevation_ft, UOM.FT,
                ElevationReference.MSL));
        aNavaid.setFrequency(new Frequency(this.frequency_khz * 1000));
        aNavaid.setId("" + this.id);
        aNavaid.setIdent(this.ident.trim());
        aNavaid.setLatitude(this.latitude_deg);
        aNavaid.setLongitude(this.longitude_deg);
        aNavaid.setName(this.name.trim());
        //aNavaid.setRemarks(remarks);
        aNavaid.setType(Navaid.Type.getType(this.type));
        aNavaid.setVariation(new MagneticVariation(this.magnetic_variation_deg,
                0));
        navaids.add(aNavaid);

        return navaids;
    }
}
