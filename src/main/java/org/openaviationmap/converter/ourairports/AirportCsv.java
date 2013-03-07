package org.openaviationmap.converter.ourairports;

import java.util.Properties;

import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.AerodromeType;
import org.openaviationmap.model.Point;

/**
 * A simple data holder class to represent the data parsed from the OurAirports
 * "airports.csv" file.
 *
 * @author Raymond Raw
 *
 */
public final class AirportCsv {

    /** The Properties as passed in from the main reader. */
    private final Properties properties;

    /** The name of the CSV file being processed by this class. */
    private static final String fileName = "airports.csv";

    /** The "OurAirports" record id of this record. */
    protected final String id;

    /** Is the ICAO identifier for this airport. */
    protected final String ident;

    /**
     *  Is the type airport.
     *  <p>
     *  Known values include "ballonport", "closed", "heliport",
     *  "large_airport", "medium_airport", "small_airport", and "seaplane_base"
     *
     */
    protected final String type;

    /** The airport's common name. */
    protected final String name;

    /** The airport's latitude. */
    protected final double latitude_deg;

    /** The airport's longitude. */
    protected final double longitude_deg;

    /** The airport's elevation in feet. */
    protected final String elevation_ft;

    /**
     * The airports's continent.
     * <p>
     * known values include "AF", "AS", "EU", "NA", "OC", and "SA".
     */
    protected final String continent;

    /** The airport's ISO country identifier. */
    protected final String iso_country;

    /** The airport's ISO region identifier such as a province or state. */
    protected final String iso_region;

    /** The airport's municipality which is usually the city. */
    protected final String municipality;

    /** Indicates whether or not the airport has scheduled service. */
    protected final boolean scheduled_service;

    /**
     * The airport's GPS code is seems most often to be the same as the ICAO
     * identifier.
     */
    protected final String gps_code;

    /** The airport's IATA identifier which is often blank. */
    protected final String iata_code;

    /** The airport's local identifier which is iften the same as the ICAO. */
    protected final String local_code;

    /** The URL of a direct link to a page for this airport. */
    protected final String home_link;

    /** The URL of a wikipedia page for this airport. */
    protected final String wikipedia_link;

    /** Miscellaneous keywords and data. */
    protected final String keywords;

    /**
     * Creates a new instance of an Airport from the provided set of values.
     * <p>
     * Very little actual validation of the values occur and many of the values
     * may be blank.
     *
     * @param csVariables
     *            Is a String array containing the values to use.
     *
     * @param properties
     *            Is the Properties object that contains the .properties file
     *            that was specified on the command line for the OurAirports
     *            input.
     *
     * @throws ParseException
     *            If any of the CSV values cannot be parsed.
     */
    protected AirportCsv(String[] csVariables, final Properties properties)
            throws ParseException {

        this.properties = properties;

        try {
            // If the CSV file doesn't have a value for all columns then the
            // trailing values won't be present. We'll default the values.
            String[] tCsVariables = {"", "", "", "", "", "", "", "", "", "",
                    "", "", "", "", "", "", "", ""};
            for (int i = 0; (i < csVariables.length) && (i < 18); ++i) {
                tCsVariables[i] = csVariables[i];
            }

            this.id = tCsVariables[0];
            this.ident = tCsVariables[1];
            this.type = tCsVariables[2];
            this.name = tCsVariables[3];
            this.latitude_deg = Double.parseDouble(tCsVariables[4]);
            this.longitude_deg = Double.parseDouble(tCsVariables[5]);
            this.elevation_ft = tCsVariables[6];
            this.continent = tCsVariables[7];
            this.iso_country = tCsVariables[8].trim().toUpperCase();
            this.iso_region = tCsVariables[9].trim().toUpperCase();
            this.municipality = tCsVariables[10];
            this.scheduled_service = (tCsVariables[11].equals("yes")) ? true
                    : false;
            this.gps_code = tCsVariables[12];
            this.iata_code = tCsVariables[13];
            this.local_code = tCsVariables[14];
            this.home_link = tCsVariables[15];
            this.wikipedia_link = tCsVariables[16];
            this.keywords = tCsVariables[17];
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
        final String[] headers = {"id", "ident", "type", "name",
                "latitude_deg", "longitude_deg", "elevation_ft", "continent",
                "iso_country", "iso_region", "municipality",
                "scheduled_service", "gps_code", "iata_code", "local_code",
                "home_link", "wikipedia_link", "keywords"};

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
     * Converts a this OurAirports "AirportCsv" object into an OAM "Aerodrome"
     * object.
     *
     * @return The converted Aerodrome object.
     */
    public Aerodrome convertToOamAerodrome() {
        Aerodrome aerodrome = new Aerodrome();

        // The airport location
        Point arp = new Point();
        arp.setLatitude(this.latitude_deg);
        arp.setLongitude(this.longitude_deg);

        // Set the aerodrome type. Note, that the default type if not set is
        // AERODROME
        if (this.type.equalsIgnoreCase("Heliport")) {
            aerodrome.setAerodrometype(AerodromeType.HELIPORT);
        }

        aerodrome.setArp(arp);
        aerodrome.setIata(this.iata_code);
        aerodrome.setIcao(this.ident);
        aerodrome.setName(this.name);

        return aerodrome;
    }
}
