package org.openaviationmap.converter.ourairports;

import java.util.Properties;

import org.openaviationmap.converter.ParseException;

/**
 * A simple data holder class to represent the data parsed from the OurAirports
 * "airport-frequencies.csv" file.
 *
 * @author Raymond Raw
 *
 */
public class AirportFrequenciesCsv {

    /** The Properties as passed in from the main reader. */
    private final Properties properties;

    /** The name of the CSV file being processed by this class. */
    private static final String FILENAME = "airport-frequencies.csv";

    /** The "OurAirports" record id of this record. */
    protected final int id;

    /** The record id of the "OurAirports" airport record to which this
     *  frequency applies.
     */
    protected final String airport_ref;

    /** The icao identification of the airport to which this frequency applies.
     */
    protected final String airport_ident;

    /** The use of the frequency, for example GND, TWR etc. */
    protected String type;

    /** An optional free form description. */
    protected final String description;

    /** The frequency expressed in megahertz. */
    protected final double frequency_mhz;

    /**
     * Creates a new instance of an Airport_Frequency from the provided set of
     * values.
     * <p>
     * Also conducts some sanitization of the frequency types.
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
    protected AirportFrequenciesCsv(String[] csValues,
            final Properties properties) throws ParseException {

        this.properties = properties;

        try {
            // If the CSV file doesn't have a value for all columns then the
            // trailing values won't be present. We'll default them.
            String[] tCsVariables = {"0", "", "", "", "", "0.0"};
            for (int i = 0; (i < csValues.length) && (i < 6); ++i) {
                tCsVariables[i] = csValues[i];
            }

            this.id = Integer.parseInt(tCsVariables[0]);
            this.airport_ref = tCsVariables[1];
            this.airport_ident = tCsVariables[2].trim().toUpperCase();
            this.type = tCsVariables[3].trim().toUpperCase();
            this.description = tCsVariables[4];
            this.frequency_mhz = Double.parseDouble(tCsVariables[5]);

            ////// Do some sanitization of the "type"
            String[] variations;

            // Make variations of Ground all "GND"
            variations = new String[] {"GROUND", "GROUND (EAST)",
                    "GROUND (WEST)"};
            for (String alternative : variations) {
                if (this.type.equalsIgnoreCase(alternative)) {
                    this.type = "GND";
                    break;
                }
            }

            // Make variations of Unicom all "UNICOM"
            variations = new String[] {"UNIC"};
            for (String alternative : variations) {
                if (this.type.equalsIgnoreCase(alternative)) {
                    this.type = "UNICOM";
                    break;
                }
            }

            // Make variations of Approach all "APCH"
            variations = new String[] {"APP", "APPROACH", "APP/DEP", "APP?TWR",
                    "ARR", "ARR ALPNACH", "ARR EMMEN"};
            for (String alternative : variations) {
                if (this.type.equalsIgnoreCase(alternative)) {
                    this.type = "APCH";
                    break;
                }
            }

            // Make variations of Approach all "ATIS"
            variations = new String[] {"ATIS-A", "ATIS-B", "ATIS/VOR"};
            for (String alternative : variations) {
                if (this.type.equalsIgnoreCase(alternative)) {
                    this.type = "ATIS";
                    break;
                }
            }
        } catch (Exception e) {
            throw new ParseException("Parse error in OurAirports file \""
                    + FILENAME + "\".", e);
        }

    }

    /**
     * Called to verify that the header line of the CSV file is what we expect.
     * <p>
     * This is a simple check that the input file is infact an "OurAirports"
     * style CSV file.
     *
     * @param csVariable
     *            A String array which contains the column headers for
     *            the airport-frequencies.csv file.
     *
     * @throws OurAirportsValidationException
     *            If the file headers do not verify
     */
    protected static void verifyHeaders(String[] csVariable)
            throws OurAirportsValidationException {

        // The expected csv header values.
        final String[] headers = {"id", "airport_ref", "airport_ident", "type",
                "description", "frequency_mhz"};

        if (csVariable.length != headers.length) {
            // This is likely not an OurAirports file. In this case, we're
            // throwing an Exception instead of a ParseException so that
            // Execuation fails completely.
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

}
