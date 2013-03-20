package org.openaviationmap.converter.ourairports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.openaviationmap.converter.ParseException;
import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Circle;
import org.openaviationmap.model.Distance;
import org.openaviationmap.model.Elevation;
import org.openaviationmap.model.ElevationReference;
import org.openaviationmap.model.Frequency;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.Runway;
import org.openaviationmap.model.UOM;

/**
 * Implements an input reader which processes a set of CSV files as generated
 * and maintained by the "OurAirports" web site.
 * <p>
 * This reader supports "update" files for each of the main OurAirports csv
 * files. This allows changes to be separately maintained and submitted to the
 * OurAirports web site.
 * <p>
 * Additionally, a set of filters can be applied to the input to limit the
 * selected objects by country, latitude and longitude and ICAO ident.
 *
 * @author Raymond
 *
 */
public class OurAirportsReader {

    /** The Properties file which holds all of our processing keys.          */
    private Properties properties = null;

    /** The Property file key which defines the location of the input CSV set.
     */
    public static final String PROPERTYKEY_ROOTDIR = "OurAirportsCSVLocation";

    ///////////////////      Filtering related Properties

    /**
     * The Property file key which defines maximum allowable latitude value.
     * <p>
     * Any aerodrome, navaid or airspace that is above this value is excluded.
     */
    private static final String PROPERTYKEY_FILTERLATITUDEMAXIMUM = "FilterLatitudeMaximum";

    /**
     * The Property file key which defines minimum allowable latitude value.
     * <p>
     * Any aerodrome, navaid or airspace that is below this value is excluded.
     */
    private static final String PROPERTYKEY_FILTERLATITUDEMINIMUM = "FilterLatitudeMinimum";

    /**
     * The Property file key which defines maximum allowable longitude value.
     * <p>
     * Any aerodrome, navaid or airspace that is east this value is excluded.
     */
    private static final String PROPERTYKEY_FILTERLONGITUDEMAXIMUM = "FilterLongitudeMaximum";

    /**
     * The Property file key which defines minimum allowable longitude value.
     * <p>
     * Any aerodrome, navaid or airspace that is west this value is excluded.
     */
    private static final String PROPERTYKEY_FILTERLONGITUDEMINIMUM = "FilterLongitudeMinimum";

    /**
     * The Property file key which defines a regular expression for selecting
     * aerodromes by ident.
     */
    private static final String PROPERTYKEY_FILTERAERODROMEIDENTS = "FilterAerodromeIdents";

    /**
     * The Property file key which defines the ISO-COUNTRY codes to be selected.
     * <p>
     * The value is a comma separated list if ISO two character country code. If
     * the key is not included, then all countries are selected.
     */
    public static final String PROPERTYKEY_FILTERISOCOUNTRY = "FilterIsoCountry";

    /**
     * The Property file key which defines the ISO-Region codes to be selected.
     * <p>
     * The value is a comma separated list of ISO region codes. If the key is
     * not included, then all regions are selected. The ISO region code is of
     * the form "cc-rr" where "cc" is the country code and "rr" is a region
     * within the country. Note, that the ISO country filter and the ISO region
     * filter are "ANDed" together. If both are specified, then the "cc" part of
     * the region code must match the ISO country code.
     */
    public static final String PROPERTYKEY_FILTERISOREGION = "FilterIsoRegion";

    /**
     * The Property file key which controls the auto generation of a circular
     * control zone around aerodromes that have a tower.
     * <p>
     * The value is specified as a letter between A and G which determines the
     * control zone type. If no value is specified then a control zone is not
     * generated.
     */
    public static final String PROPERTYKEY_AUTOGENERATECZWHENTOWER = "AutoGenerateCZWhenTower";

    /**
     * The Property file key which controls the auto generation of a circular
     * control zone around aerodromes that have a separate approach frequency.
     * <p>
     * The value is specified as a letter between A and G which determines the
     * control zone type. If no value is specified then a control zone is not
     * generated.
     */
    public static final String PROPERTYKEY_AUTOGENERATECZWHENAPPROACH = "AutoGenerateCZWhenApproach";

    /**
     * The Property file key which controls the auto generation of a circular
     * control zone around aerodromes that have an AFIS .
     * <p>
     * The value is specified as a letter between A and G which determines the
     * control zone type. If no value is specified then a control zone is not
     * generated.
      */
    public static final String PROPERTYKEY_AUTOGENERATECZWHENAFIS = "AutoGenerateCZWhenAFIS";

    /**
     * The root directory where all of the various csv files exist.
     * <p>
     * This is loaded from a Property in the Property file specified as the
     * input file.
     */
    private final String rootDir;

    /**
     * Instantiate a new copy of the reader and opens the main Properties file.
     *
     * @param inputFile
     *            Is the fully qualified name of the Properties file that
     *            contains all of our processing options. Contained properties
     *            specify the directory containing the OurAirports files and
     *            the various filtering criteria.
     *
     * @throws IOException
     *             If the Properties file is not readable.
     *
     * @throws FileNotFoundException
     *             If the specified Properties file isn't found.
     *
     * @throws MissingResourceException
     *             If a required key in the Properties file isn't found.
     */
    public OurAirportsReader(final String inputFile) throws IOException,
            FileNotFoundException, MissingResourceException,
            OurAirportsValidationException {

        // Set defaults and load our Properties file
        this.properties = this.loadProperties(inputFile);

        // The root directory where our CSV file exists
        rootDir = this.properties.getProperty(PROPERTYKEY_ROOTDIR);
        if (rootDir == null) {
            String message = "Required key \"" + PROPERTYKEY_ROOTDIR
                    + "\" in file \"" + inputFile + "\" not found.";
            System.err.println(message + "\n");
            throw new MissingResourceException(message, inputFile,
                    PROPERTYKEY_ROOTDIR);
        }
    }

    /**
     * Read, parse and load a set CSV files in "OurAirports" format.
     * <p>
     *
     * @param airspaces
     *            A returned list of airspaces.
     *
     * @param navaids
     *            A returned List of navaids.
     *
     * @param aerodromes
     *            A returned list of aerodromes.
     *
     * @param errors
     *            A returned list of any parsing exceptions.
     *
     * @throws Exception
     *            If any of the input files fails primary validation or if
     *            an unexpected error occurs.
     */
    public final void processOurAirports(final List<Airspace> airspaces,
            final List<Navaid> navaids, final List<Aerodrome> aerodromes,
            final List<ParseException> errors) throws Exception {

        /* Note, that all Exceptions are allowed to purcolate. */
        this.loadAirports(airspaces, navaids, aerodromes, errors);
        this.loadNavaids(navaids, aerodromes, errors);

        System.out.println(aerodromes.size() + "  Aerodromes loaded");
        System.out.println(navaids.size() + "  Unassociated navaids loaded");
        System.out.println(errors.size() + "  Parse errors reported");

    }

    /**
     * Read, parse and load the "airports.csv", "airports-frequencies.csv" and
     * "runways.csv" files and generate aerodrome objects.
     * <p>
     * A set of filters are loaded from a Properties file and used to restrict
     * which airports are selected and generated. This is done primarily so that
     * we can generate subset KML files to be used on the Playbook tablet with
     * Magellan Compass.
     *
     * @param airspaces
     *            The List where airspaces are to be returned. We will generate
     *            circular Class D airspaces around any aerodrome that has a TWR
     *            frequency.
     *
     * @param navaids
     *            The List where navigation aids are returned. We don't return
     *            any for these files.
     *
     * @param aerodromes
     *            The List where our airports are to be returned.
     *
     * @param errors
     *
     * @throws OurAirportsValidationException If any of the input csv files
     *              fails initial validation. Note, that this is not a parse
     *              error but instead indicates that the entire file failed
     *              validation.
     *
     * @throws IOException
     *              If an IO error occurs reading any of the OurAirports CSV
     *              files.
     *
     * @throws FileNotFoundException
     *              If one of the required OurAirports CSV files is not found.
     *
     */
    /*
     * The "airport-frequencies" and "runways" files are read, parsed and loaded
     * into HashMaps keyed by the airport code.
     *
     * The "airports" file is then read and parsed. Any filters are applied. If
     * selected, the airport, airport-frequency and runway data is then merged
     * and the Aerodrome object created.
     */
    private void loadAirports(final List<Airspace> airspaces,
            final List<Navaid> navaids, final List<Aerodrome> aerodromes,
            final List<ParseException> errors)
            throws OurAirportsValidationException, FileNotFoundException,
            IOException {

        // Load the airport_frequencies csv.
        HashMap<String, ArrayList<AirportFrequenciesCsv>> airportFrequencies = this
                .loadAirportFrequencies(errors);

        // Go load the airport runway data from the OurAirports csv files
        HashMap<String, ArrayList<RunwayCsv>> airportRunwaysCsv = this
                .loadRunways(errors);

        // Load the main airports file.
        aerodromes.addAll(this.loadAirports(errors));

        // Post process the aerodrome list. Note, that selection filters have
        // already been applied to the list during loading. The runways,
        // frequency and navaid information is applied to the aerodrome.
        for (Aerodrome aerodrome : aerodromes) {

            // The airport frequency data. A mapping is done to the four
            // frequency types currently supported by OAM
            ArrayList<AirportFrequenciesCsv> frequencies = airportFrequencies
                    .get(aerodrome.getIcao());
            Frequency afis = this.mapFrequency(frequencies, "afis");
            Frequency approach = this.mapFrequency(frequencies, "approach");
            Frequency apron = this.mapFrequency(frequencies, "apron");
            Frequency atis = this.mapFrequency(frequencies, "atis");
            Frequency tower = this.mapFrequency(frequencies, "tower");

            if (afis != null) {
                aerodrome.setAfis(afis);
            }
            //aerodrome.setAirspaces(airspaces);
            if (approach != null) {
                aerodrome.setApproach(approach);
            }
            if (apron != null) {
                aerodrome.setApron(apron);
            }
            if (atis != null) {
                aerodrome.setAtis(atis);
            }
            if (tower != null) {
                aerodrome.setTower(tower);
            }

            // aerodrome.setNavaids(navaids);

            // Add the aerodromes runway data.
            ArrayList<RunwayCsv> runwayCsvs = airportRunwaysCsv.get(aerodrome
                    .getIcao());
            if ((runwayCsvs != null) && (!runwayCsvs.isEmpty())) {
                ArrayList<Runway> runways = new ArrayList<Runway>(10);
                for (RunwayCsv runwayCsv : runwayCsvs) {
                    runways.addAll(runwayCsv.convertToOamRunways());
                }
                aerodrome.setRunways(runways);
            }

            // Generate a control zone based on the existence of a Tower
            // frequency, Approach frequency and Afis frequency
            this.generateAerodromeControlZone(aerodrome);
        }
        return;
    }

    /**
     * Load the OurAirports "airports.csv file and apply any changes in the
     * "airports.updates.csv" file.
     * <p>
     * During loading filters are applied to the airports and only airports that
     * meet the filter criteria are loaded.
     * <p>
     * The "airports.updates.csv" file is optional. It is not part of the
     * OurAirports collection of files. This file is optional. If it exists,
     * then it is used to update the main "airports.csv" file with local
     * changes. The format is identical to the main "airports.csv" file and can
     * be used to submit changes to OurAirports. If the update file contains an
     * airport with the same ident as the main file, then the information is
     * replaced with the new information from the update file. If the ident
     * does not exist in the main file, then it is added to the aerodrome list.
     * In either case, if both the latitude and longitude are zero, then the
     * aerodrome is removed from the aerodrome list.
     *
     * @param errors
     *              A List where all parsing errors are returned.
     *
     * @return An ArrayList<Aerodrome> containing the airports.
     *
     * @throws IOException
     *             If there is an IO error reading the CSV files.
     *
     * @throws OurAirportsValidationException
     *             If one of the input csv files is determined not be be an
     *             OurAirports csv file.
     */
    private ArrayList<Aerodrome> loadAirports(final List<ParseException> errors)
            throws IOException, OurAirportsValidationException {
        ArrayList<Aerodrome> aerodromes = new ArrayList<Aerodrome>();

        String aCsvLine;

        {
            // Load the primary airports file.
            File airportsCsvFile = new File(this.rootDir + "/airports.csv");
            BufferedReader airportsCsvReader = new BufferedReader(
                    new FileReader(airportsCsvFile));

            // Read the column header line
            aCsvLine = airportsCsvReader.readLine();
            AirportCsv.verifyHeaders(this.parseCsvLine(aCsvLine));
            aCsvLine = airportsCsvReader.readLine();
            while (aCsvLine != null) {
                try {
                    String[] csVariables = this.parseCsvLine(aCsvLine);
                    AirportCsv airportCsv = new AirportCsv(csVariables,
                            this.properties);
                    if (this.isSelectedByFilter(airportCsv)) {
                        Aerodrome aerodrome = airportCsv
                                .convertToOamAerodrome();
                        aerodromes.add(aerodrome);
                    }
                } catch (ParseException pe) {
                    errors.add(pe);
                }
                aCsvLine = airportsCsvReader.readLine();
            }
        }

        {
            // Load and merge the updates airports file.
            File airportsCsvFile = new File(this.rootDir
                    + "/airports.updates.csv");
            try {
                BufferedReader airportsCsvReader = new BufferedReader(
                        new FileReader(airportsCsvFile));

                // Read the column header line
                aCsvLine = airportsCsvReader.readLine();
                AirportCsv.verifyHeaders(this.parseCsvLine(aCsvLine));
                aCsvLine = airportsCsvReader.readLine();
                while (aCsvLine != null) {
                    try {
                        String[] csVariables = this.parseCsvLine(aCsvLine);
                        AirportCsv airportCsv = new AirportCsv(csVariables,
                                this.properties);
                        if (this.isSelectedByFilter(airportCsv)) {
                            Aerodrome aerodrome = airportCsv
                                    .convertToOamAerodrome();
                            String ident = airportCsv.ident.trim();
                            aerodromes.remove(ident);
                            if ((airportCsv.latitude_deg != 0)
                                    || (airportCsv.longitude_deg != 0)) {
                                aerodromes.add(aerodrome);
                            }
                        }
                    } catch (ParseException pe) {
                        errors.add(pe);
                    }
                    aCsvLine = airportsCsvReader.readLine();
                }
            } catch (FileNotFoundException e) {
                // Just ignore the fact that "airports.updates.csv" doesn't
                // exist
            }
        }

        return aerodromes;
    }

    /**
     * A simple minded CSV line parser.
     * <p>
     * This handles quoted (double quotes only) and non-quoted values. the
     * delimiter is fixed as "," (comma). Commas may be embedded within quoted
     * values.
     *
     * @param aCsvLine
     *            The raw, unparsed line containing CSV values without the
     *            trailing new line.
     *
     * @return An array of Strings where each element contains a single trimmed
     *            value. Empty values are returned as zero length Strings.
     */
    private String[] parseCsvLine(final String aCsvLine) {

        Vector<String> values = new Vector<String>(20);
        int scanPos = 0;
        boolean notAtEnd = true;
        String value = "";

        while (notAtEnd) {
            // Find the start of a value
            for (; scanPos < aCsvLine.length(); ++scanPos) {
                if (aCsvLine.charAt(scanPos) != ' ') {
                    break;
                }
            }

            if (scanPos < aCsvLine.length()) {
                // We have the start of a value

                if (aCsvLine.charAt(scanPos) == ',') {
                    // If its a comma then we have a null token
                    values.add("");
                    ++scanPos;
                } else if (aCsvLine.charAt(scanPos) != '\"') {
                    // We are not dealing with a quoted String. Find the
                    // separating , (comma) or line end
                    int commaPos = aCsvLine.substring(scanPos).indexOf(",");
                    if (commaPos >= 0) {
                        // We have a separating comma
                        value = aCsvLine.substring(scanPos, scanPos + commaPos);
                        values.add(value.trim());
                        scanPos += commaPos + 1;
                    } else {
                        // We're at the end
                        value = aCsvLine.substring(scanPos);
                        values.add(value.trim());
                        notAtEnd = false;
                    }
                } else {
                    // We have a qouted value
                    ++scanPos;
                    int closeQuotePos = aCsvLine.substring(scanPos).indexOf(
                            "\"");
                    value = aCsvLine
                            .substring(scanPos, scanPos + closeQuotePos);
                    values.add(value);
                    scanPos += closeQuotePos + 1;
                    // Now go find the comma. Note, everything between the
                    // closing quote and the comma is ignored
                    int commaPos = aCsvLine.substring(scanPos).indexOf(",");
                    scanPos += commaPos + 1;
                    if (commaPos < 0) {
                        // We've reached line end
                        notAtEnd = false;
                    }
                }
            }

            else {
                notAtEnd = false;
            }
        }

        return values.toArray(new String[0]);
    }

    /**
     * Looks for and maps the various radio types and frequencies to those
     * supported by Open Aviation Maps.
     *
     * @param frequencies
     *            Is a list of frequencies for this airport.
     *
     * @param type
     *            Is one of the Strings "afis", "approach", "apron", "atis" or
     *            "tower"
     *
     * @return A Frequency object or null is this airport does not have one of
     *            these frequencies.
     */
    private Frequency mapFrequency(
            final ArrayList<AirportFrequenciesCsv> frequencies,
            final String type) {

        /** The frequency that we'll return */
        Frequency frequency = null;

        /** Temporary used to hold variation for what we're looking for. */
        String[] variations;

        // If we don't have frequencies then there is nothing to do
        if (frequencies == null) {
            return null;
        }

        if (type.equals("ATIS")) {
            // Look for an instance of ATIS. We assume only ever one and exit
            // once we find one
            for (AirportFrequenciesCsv ourAirportsFrequency : frequencies) {
                if (ourAirportsFrequency.type.equals("ATIS")) {
                    frequency = new Frequency(
                            ourAirportsFrequency.frequency_mhz * 1000000.0D);
                    break;
                }
            }
        }

        else if (type.equalsIgnoreCase("TOWER")) {
            // Look for an instance of TWR
            for (AirportFrequenciesCsv ourAirportsFrequency : frequencies) {
                if (ourAirportsFrequency.type.equals("TWR")) {
                    frequency = new Frequency(
                            ourAirportsFrequency.frequency_mhz * 1000000.0D);
                    break;
                }
            }
        }

        else if (type.equalsIgnoreCase("APRON")) {
            // Look for an instance of GND. If there are multiple ground
            // frequencies we use the first
            for (AirportFrequenciesCsv ourAirportsFrequency : frequencies) {
                if (ourAirportsFrequency.type.equalsIgnoreCase("gnd")) {
                    frequency = new Frequency(
                            ourAirportsFrequency.frequency_mhz * 1000000.0D);
                    break;
                }
            }
        }

        else if (type.equalsIgnoreCase("AFIS")) {
            // Although technically not exactly correct, we are using AFIS to
            // mean any frequency that gives aerodrome traffic advice at
            // aerodromes that are not "Controlled". The following frequency
            // types that are present in the OurAirports data are selected.
            // "AFIS", "FIS", "UNICOM", "CTAF", "ATF", "MF"
            variations = new String[] {"AFIS", "FIS", "UNICOM", "CTAF", "ATF",
                    "MF"};
            for (AirportFrequenciesCsv ourAirportsFrequency : frequencies) {
                for (String variation : variations) {
                    if (ourAirportsFrequency.type.equals(variation)) {
                        frequency = new Frequency(
                                ourAirportsFrequency.frequency_mhz * 1000000.0D);
                        break;
                    }
                }
            }
        }

        else if (type.equalsIgnoreCase("APPROACH")) {
            // Look for an instance of APCH. We assume only ever one and exit
            // once we find one
            for (AirportFrequenciesCsv ourAirportsFrequency : frequencies) {
                if (ourAirportsFrequency.type.equalsIgnoreCase("apch")) {
                    frequency = new Frequency(
                            ourAirportsFrequency.frequency_mhz * 1000000.0D);
                    break;
                }
            }
        }

        return frequency;
    }

    /**
     * Generate a circular control zone around an aerodrome based on the radio
     * frequencies at the aerodrome.
     * <p>
     * This is at best a guess of whether a given aerodrome is in fact a
     * controlled airport or not but it does at least provide a starting point
     * and appears to do a reason job when using the OurAirports csv files.
     * <p>
     * If the aerodrome has a tower frequency then we generate a 5nmi class D
     * zone. If it also has an approach frequency then we generate a 7nmi class
     * C zone.
     *
     * @param aerodrome
     *            Is the Aerodrome for whom we will potentially generate the
     *            control zone.
     */
    //TODO Need a way to obtain ControlZone heights and set them appropriately
    //TODO Need a way to find and define irregular control zones
    private void generateAerodromeControlZone(final Aerodrome aerodrome) {

        // Used to put the control zone height into the label
        DecimalFormat makeInt = new DecimalFormat("0");
        if (aerodrome.getTower() != null) {
            // We have a tower. Generate a control zone
            Object[] cz = (Object[]) this.properties
                    .get(PROPERTYKEY_AUTOGENERATECZWHENTOWER);
            if (cz.length != 0) {
                // We need to generate a control zone.
                Airspace controlZone = new Airspace();
                Circle circle = new Circle();
                circle.setCenter(aerodrome.getArp());
                controlZone.setBoundary(circle);
                controlZone.setLowerLimit(new Elevation(0.0d, UOM.FT,
                        ElevationReference.SFC));
                controlZone.setCommFrequency(aerodrome.getTower().toString());
                controlZone.setType("CTR");
                List<Airspace> airspaces = aerodrome.getAirspaces();
                if (airspaces == null) {
                    // No airspaces so far, create the new List for ours
                    airspaces = new Vector<Airspace>(5);
                    aerodrome.setAirspaces(airspaces);
                }

                if (aerodrome.getApproach() == null) {
                    // We need to generate a "Tower" only airspace.
                    circle.setRadius(new Distance((Double) cz[1], UOM.NM));
                    controlZone.setAirspaceClass((String) cz[0]);
                    controlZone.setUpperLimit(new Elevation((Double) cz[2],
                            UOM.FT, ElevationReference.SFC));
                    controlZone.setName("CZ \"" + cz[0] + "\" "
                            + makeInt.format(cz[2]));
                }

                else {
                    // We need to generate an airspace when there's appraoch.
                    Object[] czAPRCH = (Object[]) this.properties
                            .get(PROPERTYKEY_AUTOGENERATECZWHENAPPROACH);
                    if (czAPRCH.length != 0) {
                        // Use the "Approach control zone type
                        cz = czAPRCH;
                    }
                    circle.setRadius(new Distance((Double) cz[1], UOM.NM));
                    controlZone.setAirspaceClass((String) cz[0]);
                    controlZone.setUpperLimit(new Elevation((Double) cz[2],
                            UOM.FT, ElevationReference.SFC));
                    controlZone.setName("CZ \"" + cz[0] + "\" "
                            + makeInt.format(cz[2]));
                }

                // Add the new airspace
                airspaces.add(controlZone);
            }
        }

        else if (aerodrome.getAfis() != null) {
            // This aerodrome has a Flight Information Service
            Object[] cz = (Object[]) this.properties
                    .get(PROPERTYKEY_AUTOGENERATECZWHENAFIS);
            if (cz.length != 0) {
                // We need to generate a control zone.
                Airspace controlZone = new Airspace();
                Circle circle = new Circle();
                circle.setCenter(aerodrome.getArp());
                controlZone.setBoundary(circle);
                controlZone.setLowerLimit(new Elevation(0.0d, UOM.FT,
                        ElevationReference.SFC));
                controlZone.setCommFrequency(aerodrome.getAfis().toString());
                controlZone.setType("CTR");
                List<Airspace> airspaces = aerodrome.getAirspaces();
                if (airspaces == null) {
                    // No airspaces so far, create the new List for ours
                    airspaces = new Vector<Airspace>(5);
                    aerodrome.setAirspaces(airspaces);
                }
                circle.setRadius(new Distance((Double) cz[1], UOM.NM));
                controlZone.setAirspaceClass((String) cz[0]);
                controlZone.setUpperLimit(new Elevation((Double) cz[2], UOM.FT,
                        ElevationReference.SFC));
                controlZone.setName("CZ \"" + cz[0] + "\" "
                        + makeInt.format(cz[2]));

                // Add the new airspace
                airspaces.add(controlZone);
            }
        }

    }

    /**
     * Loads the OurAirports Navaid.csv file and Navaid.updates.csv file and
     * loads them into the Navaids list.
     *
     * @param navaids Is the List into which the loaded and filtered Navaids
     *          will be inserted and returned.
     *
     * @param aerodromes If a Navaid is directly associated with an aerodrome,
     *          then the Navaid is attached to the Aerodrome. Otherwise, it is
     *          returned in the Navaids List.
     *
     * @param errors A List containing "ParseExceptions" that may occur during
     *          the load.
     *
     * @throws IOException
     *             If the input file cannot be read.
     *
     * @throws OurAirportsValidationException
     *             If one of the input csv files is determined not be be an
     *             OurAirports csv file.
     */
    private void loadNavaids(final List<Navaid> navaids,
            final List<Aerodrome> aerodromes, final List<ParseException> errors)
            throws IOException, OurAirportsValidationException {
        /*
         * The file is loaded into a local HashMap and indexed by the id. This
         * HashMap contains only the CSV version of the Navaid as loaded from
         * OurAirports. An updated file is loaded and used to update the initial
         * load. Once this is complete, the HashMap is reprocessed and converted
         * into proper Navaid objects. These Navaid objects are then added to
         * the appropriate aerodrome if an association exists else they are
         * added to the master Navaids List.
         */

        String aCsvLine;

        HashMap<Integer, NavaidCsv> navaidsCsv = new HashMap<Integer, NavaidCsv>(
                25000);

        {
            /*
             * Load the primary CSV file
             */
            File navaidsCsvFile = new File(this.rootDir + "/navaids.csv");
            BufferedReader navaidsCsvReader = new BufferedReader(
                    new FileReader(navaidsCsvFile));

            // Read the column header line and verify it. A
            // OurAirportsValidationException will be thrown if it doesn't match
            aCsvLine = navaidsCsvReader.readLine();
            NavaidCsv.verifyHeaders(this.parseCsvLine(aCsvLine));

            // Now read the rest of the file
            aCsvLine = navaidsCsvReader.readLine();
            while (aCsvLine != null) {
                try {
                    String[] csValues = this.parseCsvLine(aCsvLine);
                    NavaidCsv navaid = new NavaidCsv(csValues, this.properties);
                    this.addOrReplaceNavaid(navaid, navaidsCsv);
                } catch (ParseException pe) {
                    errors.add(pe);
                }
                aCsvLine = navaidsCsvReader.readLine();
            }
        }

        {
            /*
             * Now apply an updates file if it exists.
             */
            boolean doUpdates = true;
            File navaidsCsvFile = new File(this.rootDir
                    + "/navaids.updates.csv");
            BufferedReader navaidsCsvReader = null;
            try {
                navaidsCsvReader = new BufferedReader(new FileReader(
                        navaidsCsvFile));
            } catch (FileNotFoundException e) {
                doUpdates = false;
            }

            if (doUpdates) {
                // Read the column header line and verify it. A
                // OurAirportsValidationException will be thrown if it doesn't
                // match.
                aCsvLine = navaidsCsvReader.readLine();
                NavaidCsv.verifyHeaders(this.parseCsvLine(aCsvLine));

                // Now read the rest of the file
                aCsvLine = navaidsCsvReader.readLine();
                while (aCsvLine != null) {
                    try {
                        String[] csValues = this.parseCsvLine(aCsvLine);
                        NavaidCsv navaid = new NavaidCsv(csValues,
                                this.properties);
                        this.addOrReplaceNavaid(navaid, navaidsCsv);
                    } catch (ParseException pe) {
                        errors.add(pe);
                    }
                    aCsvLine = navaidsCsvReader.readLine();
                }
            }
        }

        {
            /*
             * Pass through the loaded and updated Navaids file and make OAM
             * Navaids while associating them to aerodromes if appropriate
             */
            //TODO This may be a major performance issue when there are lots of aerodromes
            TreeMap<String, Aerodrome> aerodromesMap = new TreeMap<String, Aerodrome>();
            for (Aerodrome aerodrome : aerodromes) {
                aerodromesMap.put(aerodrome.getIcao(), aerodrome);
            }

            Collection<NavaidCsv> csvNavaids = navaidsCsv.values();
            for (NavaidCsv aNavaidCsv : csvNavaids) {
                if (this.isSelectedByFilter(aNavaidCsv)) {
                    ArrayList<Navaid> navaid = aNavaidCsv.convertToOamNavaids();
                    Aerodrome aerodrome = null;
                    if (aNavaidCsv.associated_airport.length() != 0) {
                        // If there is an associated aerodrome but we get a null
                        // returned, then, this aerodrome has propably been
                        // filtered out so we ignor it
                        aerodrome = aerodromesMap
                                .get(aNavaidCsv.associated_airport.trim());
                        if (aerodrome != null) {
                            this.addNavaidToAerodrome(aerodrome, navaid);
                        }
                    } else {
                        // There is no associated aerodrome so, its a free
                        // navaid.
                        navaids.addAll(navaid);
                    }
                }
            }

        }

    }

    /**
     * Establish default property values then read and apply the given
     * Properties file.
     *
     * @param propertiesFileName
     *            The full path name of the Property file to apply. This may be
     *            specified as null or a zero length String.
     *
     * @return A new Properties file with all Property values either set from
     *            the file or set to the default value.
     *
     * @throws FileNotFoundException
     *            If the given Property file does not exist.
     *
     * @throws IOException
     *            If the Properties file cannot be read.
     */
    //TODO Cleanup the contructor and make use of this "loadProperties" method
    private Properties loadProperties(final String propertiesFileName)
            throws FileNotFoundException, IOException,
            OurAirportsValidationException {

        Properties properties = new Properties();

        // Set the defaults.

        // Filtering related Properties
        properties.setProperty(PROPERTYKEY_FILTERLATITUDEMAXIMUM, "90.0");
        properties.setProperty(PROPERTYKEY_FILTERLATITUDEMINIMUM, "-90.0");
        properties.setProperty(PROPERTYKEY_FILTERLONGITUDEMAXIMUM, "180.0");
        properties.setProperty(PROPERTYKEY_FILTERLONGITUDEMINIMUM, "-180.0");
        properties.setProperty(PROPERTYKEY_FILTERAERODROMEIDENTS, "");

        // Now load the external Properties file which is a standard Java
        // Properties file with the options and settings that we support.
        if ((propertiesFileName != null) && (propertiesFileName.length() != 0)) {
            FileInputStream propertyFile = new FileInputStream(
                    propertiesFileName);
            properties.load(propertyFile);
        }

        // Convert the latitude and longitude values into doubles for later use.
        String maxLatitudeStr = properties
                .getProperty(PROPERTYKEY_FILTERLATITUDEMAXIMUM);
        String minLatitudeStr = properties
                .getProperty(PROPERTYKEY_FILTERLATITUDEMINIMUM);
        String maxLongitudeStr = properties
                .getProperty(PROPERTYKEY_FILTERLONGITUDEMAXIMUM);
        String minLongitudeStr = properties
                .getProperty(PROPERTYKEY_FILTERLONGITUDEMINIMUM);

        double maxLatitude = Double.parseDouble(maxLatitudeStr);
        double minLatitude = Double.parseDouble(minLatitudeStr);
        double maxLongitude = Double.parseDouble(maxLongitudeStr);
        double minLongitude = Double.parseDouble(minLongitudeStr);

        properties.put(PROPERTYKEY_FILTERLATITUDEMAXIMUM, maxLatitude);
        properties.put(PROPERTYKEY_FILTERLATITUDEMINIMUM, minLatitude);
        properties.put(PROPERTYKEY_FILTERLONGITUDEMAXIMUM, maxLongitude);
        properties.put(PROPERTYKEY_FILTERLONGITUDEMINIMUM, minLongitude);

        properties.put(PROPERTYKEY_AUTOGENERATECZWHENAFIS, "");
        properties.put(PROPERTYKEY_AUTOGENERATECZWHENAPPROACH, "C,7.0,4000");
        properties.put(PROPERTYKEY_AUTOGENERATECZWHENTOWER, "D,5.0,4000");

        // ISO-Country filter. This is a comma separated list of country codes.
        // Separate each country, trim it and uppercase it. We then stuff the
        // array back into the Properties instance for later use;
        String isoCountries = properties.getProperty(
                PROPERTYKEY_FILTERISOCOUNTRY, "");
        String[] countries = isoCountries.split(",");
        if ((countries.length == 1) && (countries[0].length() == 0)) {
            countries = new String[0];
        } else {
            for (int i = 0; i < countries.length; ++i) {
                countries[i] = countries[i].trim().toUpperCase();
            }
        }
        properties.put(PROPERTYKEY_FILTERISOCOUNTRY, countries);

        // ISO-Region filter. This is a comma separated list of region codes.
        // Separate each region, trim it and uppercase it. We then stuff the
        // array back into the Properties instance for later use;
        String isoRegions = properties.getProperty(PROPERTYKEY_FILTERISOREGION,
                "");
        String[] regions = isoRegions.split(",");
        if ((regions.length == 1) && (regions[0].length() == 0)) {
            regions = new String[0];
        } else {
            for (int i = 0; i < regions.length; ++i) {
                regions[i] = regions[i].trim().toUpperCase();
            }
        }
        properties.put(PROPERTYKEY_FILTERISOREGION, regions);

        // Check to regular expression used to match airport idents. If its all
        // blank or a zero length string, then we want to force a 0 length
        // String. This is what isSelectedByFilter() expects.
        String regexp = properties
                .getProperty(PROPERTYKEY_FILTERAERODROMEIDENTS);
        if (regexp != null) {
            if (regexp.trim().length() == 0) {
                properties.put(PROPERTYKEY_FILTERAERODROMEIDENTS, "");
            }
        }

        // Parse the auto generate control zone settings. The control zone
        // String must be in the form "z,ddd,hhh" where "z" is a letter from
        // A - G, ddd is a decimal number representing the zone diameter in
        // nautical miles and hhh is the zone height in feet.
        String[] propNames = {PROPERTYKEY_AUTOGENERATECZWHENAFIS,
                PROPERTYKEY_AUTOGENERATECZWHENAPPROACH,
                PROPERTYKEY_AUTOGENERATECZWHENTOWER};
        for (String propName : propNames) {
            String czDef = properties.getProperty(propName);
            Double diameter;
            Double height;
            if (czDef.length() != 0) {
                String[] czParts = czDef.split(",");
                try {
                    if ((czParts[0].length() != 1)
                            || (!czParts[0].matches("[ABCDEFG]"))) {
                        throw new ParseException(
                                "Control zone type must a single letter A - G");
                    }
                    diameter = new Double(czParts[1]);
                    height = new Double(czParts[2]);
                } catch (Exception e) {
                    throw new OurAirportsValidationException(
                            "Invalid value for property \""
                                    + PROPERTYKEY_AUTOGENERATECZWHENAFIS
                                    + "\". The Exception was "
                                    + e.getLocalizedMessage());
                }
                Object[] values = {czParts[0], diameter, height};
                properties.put(propName, values);
            } else {
                properties.put(propName, new Object[0]);
            }
        }

        return properties;

    }

    /**
     * Reads and parses the "runways.csv" OurAirports file.
     * <p>
     * A secondary file called "runways.updates.csv" is also read. It is in an
     * identical format as the first file. Each record in this secondary file is
     * used to add, update, or delete runways added by the first. The concept
     * here is that this update file can be submitted back to OurAirports and
     * applied to there masters.
     *
     * @param errors
     *              A List where all parsing errors are returned.
     *
     * @return HashMap<String, ArrayList<Runway>> where the String is an airport
     *              ident and the ArrayList is a list of runways for that
     *              airport.
     *
     * @throws FileNotFoundException
     *              If the primary "runways.csv" file is not found.
     *
     * @throws IOException
     *              If the either the primary or update file is unreadable.
     *
     * @throws OurAirportsValidationException
     *             If one of the input csv files is determined not be be an
     *             OurAirports csv file.
     */
    private HashMap<String, ArrayList<RunwayCsv>> loadRunways(
            final List<ParseException> errors) throws FileNotFoundException,
            IOException, OurAirportsValidationException {

        /*
         * The file is loaded into a local HashMap and indexed by the airport
         * ICAO id. Each entry in the map is an ArrayList of objects that
         * contain all information about a single runway used at the airport.
         *
         * Note, that the csv format were loading here contains all of the
         * information for both directions of the runway.
         *
         * Once both the main file and the update file have been loaded, a pass
         * is made through the HashMap and a new HashMap is produced which
         * contains OAM style Runway objects instead of OurAirports RunwayCsv
         * objects.
         */

        HashMap<String, ArrayList<RunwayCsv>> runwayCsvs = new HashMap<String, ArrayList<RunwayCsv>>(
                100000);

        {
            // Load the primary file
            File runwaysCsvFile = new File(this.rootDir + "/runways.csv");
            BufferedReader runwaysCsvReader = new BufferedReader(
                    new FileReader(runwaysCsvFile));

            // Read the column header line and verify it. A
            // OurAirportsValidationException will be thrown if it doesn't match
            String aCsvLine = runwaysCsvReader.readLine();
            RunwayCsv.verifyHeaders(this.parseCsvLine(aCsvLine));

            // Now read the rest of the file
            aCsvLine = runwaysCsvReader.readLine();
            while (aCsvLine != null) {
                try {
                    String[] csVariables = this.parseCsvLine(aCsvLine);
                    RunwayCsv runwayCsv = new RunwayCsv(csVariables,
                            this.properties);
                    this.addOrReplaceRunway(runwayCsv, runwayCsvs);
                } catch (ParseException pe) {
                    errors.add(pe);
                }
                aCsvLine = runwaysCsvReader.readLine();
            }
        }

        {
            // Load and apply any updates
            File runwaysCsvFile = new File(this.rootDir
                    + "/runways.updates.csv");
            BufferedReader runwaysCsvReader = null;
            try {
                runwaysCsvReader = new BufferedReader(new FileReader(
                        runwaysCsvFile));
            } catch (FileNotFoundException e) {
                runwaysCsvReader = null;
            }
            if (runwaysCsvReader != null) {
                // Read the column header line and verify it. A
                // OurAirportsValidationException will be thrown if it doesn't
                // match.
                String aCsvLine = runwaysCsvReader.readLine();
                RunwayCsv.verifyHeaders(this.parseCsvLine(aCsvLine));

                // Now read the rest of the file
                aCsvLine = runwaysCsvReader.readLine();
                while (aCsvLine != null) {
                    try {
                        String[] csVariables = this.parseCsvLine(aCsvLine);
                        RunwayCsv runwayCsv = new RunwayCsv(csVariables,
                                this.properties);
                        this.addOrReplaceRunway(runwayCsv, runwayCsvs);
                    } catch (ParseException pe) {
                        errors.add(pe);
                    }
                    aCsvLine = runwaysCsvReader.readLine();
                }
            }
        }

        return runwayCsvs;
    }

    /**
     * Reads and parses the "airport-frequencies.csv" OurAirports file.
     * <p>
     * A secondary file called "airport-frequencies.updates.csv" is also read.
     * It is in an identical format as the first file. Each record in this
     * secondary file is used to add, update, or delete a frequency added by the
     * first. The concept here is that this update file can be submitted back to
     * OurAirports and applied to there masters.
     *
     * @param errors
     *              A List where all parsing errors are returned.
     *
     * @return HashMap<String, ArrayList<Airport_FrequenciesCsv>> where the
     *              String is an airport ident and the ArrayList is a list of
     *              frequencies for that airport.
     *
     * @throws FileNotFoundException
     *              If the primary "airport-frequencies.csv" file is not found.
     *
     * @throws IOException
     *              If there is an IO error while reading the files.
     *
     * @throws OurAirportsValidationException
     *              If validation of the "airport_frequencies.csv" file
     *              fails. Note, that this is different than a parsing error
     *              and indicates that the entire file is invalid.
     */
    private HashMap<String, ArrayList<AirportFrequenciesCsv>> loadAirportFrequencies(
            final List<ParseException> errors)
            throws OurAirportsValidationException, FileNotFoundException,
            IOException {
        /*
         * The file is loaded into a local HashMap and indexed by the airport
         * call sign. Each entry in the map is an ArrayList of objects that
         * contain information about a single frequency used at the airport.
         * The data is then referenced when we read the airports file and is
         * used to help create the main Aerodrome objects.
         */

        String aCsvLine;

        HashMap<String, ArrayList<AirportFrequenciesCsv>> airportFrequencies = new HashMap<String, ArrayList<AirportFrequenciesCsv>>(
                10000);

        {
            File airportFrequenciesCsvFile = new File(this.rootDir
                    + "/airport-frequencies.csv");
            BufferedReader airportFrequenciesCsvReader = new BufferedReader(
                    new FileReader(airportFrequenciesCsvFile));

            // Read the column header line and verify it. A
            // OurAirportsValidationException will be thrown if it doesn't match
            aCsvLine = airportFrequenciesCsvReader.readLine();
            AirportFrequenciesCsv.verifyHeaders(this.parseCsvLine(aCsvLine));

            // Now read the rest of the file
            aCsvLine = airportFrequenciesCsvReader.readLine();
            while (aCsvLine != null) {
                try {
                    String[] csValues = this.parseCsvLine(aCsvLine);
                    AirportFrequenciesCsv airportFrequency = new AirportFrequenciesCsv(
                            csValues, this.properties);
                    this.addOrReplaceFrequency(airportFrequency,
                            airportFrequencies);
                } catch (ParseException pe) {
                    errors.add(pe);
                }
                aCsvLine = airportFrequenciesCsvReader.readLine();
            }
        }

        /*
         * Now apply an updates file if it exists.
         */
        {
            File airportFrequenciesCsvFile = new File(this.rootDir
                    + "/airport-frequencies.updates.csv");
            BufferedReader airportFrequenciesCsvReader = null;
            try {
                airportFrequenciesCsvReader = new BufferedReader(
                        new FileReader(airportFrequenciesCsvFile));
            } catch (FileNotFoundException e) {
                airportFrequenciesCsvReader = null;
            }

            if (airportFrequenciesCsvReader != null) {
                // Read the column header line and verify it. A
                // OurAirportsValidationException will be thrown if it doesn't
                // match
                aCsvLine = airportFrequenciesCsvReader.readLine();
                AirportFrequenciesCsv
                        .verifyHeaders(this.parseCsvLine(aCsvLine));

                // Now read the rest of the file
                aCsvLine = airportFrequenciesCsvReader.readLine();
                while (aCsvLine != null) {
                    try {
                        String[] csValues = this.parseCsvLine(aCsvLine);
                        AirportFrequenciesCsv airportFrequency = new AirportFrequenciesCsv(
                                csValues, this.properties);
                        this.addOrReplaceFrequency(airportFrequency,
                                airportFrequencies);
                    } catch (ParseException pe) {
                        errors.add(pe);
                    }
                    aCsvLine = airportFrequenciesCsvReader.readLine();
                }
            }
        }

        return airportFrequencies;
    }

    /**
     * Given a new Airport_FrequenciesCsv object either add it to the list of
     * frequencies for the airport or replace an existing instance if the Id
     * field is the same.
     * <p>
     * If the given airport has no frequencies, then this method will also
     * create the ArrayList that holds the frequencies for an airport.
     * <p>
     * Replacement matching is based on the If field of the record which all
     * OurAirports data carries.
     * <p>
     * If the frequency is 0.0 then the new record is treated as a delete. If
     * the new record has a zero in the Id field then the record is treated as
     * an addition.
     *
     * @param airportFrequency
     *              Is the airport frequency to either add or replace and
     *              existing one with.
     */
    private void addOrReplaceFrequency(
            final AirportFrequenciesCsv airportFrequency,
            final HashMap<String, ArrayList<AirportFrequenciesCsv>> airport_frequencies) {

        // Get the current list of frequencies for this airport
        ArrayList<AirportFrequenciesCsv> frequencies = airport_frequencies
                .get(airportFrequency.airport_ident);
        if (frequencies == null) {
            // This is the first frequency for this airport
            frequencies = new ArrayList<AirportFrequenciesCsv>(5);
            frequencies.add(airportFrequency);
            airport_frequencies
                    .put(airportFrequency.airport_ident, frequencies);
        } else {
            // We already have at least one frequency for this airport.
            if (airportFrequency.id == 0) {
                // Always add records with an Id of zero unless the frequency is
                // also zero
                if (airportFrequency.frequency_mhz != 0.0d) {
                    frequencies.add(airportFrequency);
                }
            } else {
                boolean haveNew = true;
                for (AirportFrequenciesCsv existingAirportFrequency : frequencies) {
                    if (existingAirportFrequency.id == airportFrequency.id) {
                        // Remove the existing frequency
                        frequencies.remove(existingAirportFrequency);
                        if (airportFrequency.frequency_mhz != 0.0d) {
                            // Only replace the record if the new record has a
                            // frequency otherwise its just a delete.
                            frequencies.add(airportFrequency);
                        }
                        haveNew = false;
                        break;
                    }
                }
                if (haveNew) {
                    // Add the new record unless the frequency is zero
                    if (airportFrequency.frequency_mhz != 0.0d) {
                        frequencies.add(airportFrequency);
                    }
                }
            }
        }
    }

    /**
     * Given a new NavaidCsv object either add it to the list of Navaids or
     * replace an existing instance if the Id field is the same.
     * <p>
     * Replacement matching is based on the Id field of the record which all
     * OurAirports data carries.
     * <p>
     * If the "filename" field is blank then the record is treated as a delete.
     * If the new record has a zero in the Id field then the record is treated
     * as an addition.
     *
     * @param navaid
     *          Is the new CSV navaid record to be added, updated or deleted.
     *
     * @param navaids
     *          Is the HashMap of navaids to be updated.
     *
     * @throws ParseException
     *          If there is any errors during the parsing process.
     */
    private void addOrReplaceNavaid(final NavaidCsv navaid,
            final HashMap<Integer, NavaidCsv> navaids) throws ParseException {

        if (navaid.id == 0) {
            // Always add records with an Id of zero unless the "filename" is
            // also null or sero length
            if ((navaid.filename != null) && (navaid.filename.length() != 0)) {
                navaids.put(new Integer(navaid.id), navaid);
            }
        } else {
            NavaidCsv existingNavaid = navaids.get(new Integer(navaid.id));
            if (existingNavaid != null) {
                // Remove the existing frequency
                navaids.remove(new Integer(navaid.id));
                if ((navaid.filename != null)
                        && (navaid.filename.length() != 0)) {
                    // Only replace the record if the new record has a
                    // "filename" otherwise its just a delete
                    navaids.put(new Integer(navaid.id), navaid);
                }
            } else {
                navaids.put(new Integer(navaid.id), navaid);
            }
        }
    }

    /**
     * Given a new RunwayCsv object either add it to the list of runways for the
     * airport or replace an existing instance if the Id field is the same.
     * <p>
     * If the given airport has no runways, then this method will also create
     * the ArrayList that holds the runways for an airport.
     * <p>
     * Replacement matching is based on the Id field of the record which all
     * OurAirports data carries.
     * <p>
     * If the runway length is 0.0 then the new record is treated as a delete.
     * If the new record has a zero in the Id field then the record is treated
     * as an addition.
     *
     * @param runway
     *          Is the runway to either add or replace and existing one with.
     *
     * @param runwayCsvs
     *          Is the HashMap that contains the current runway definitions.
     */
    private void addOrReplaceRunway(final RunwayCsv runway,
            final HashMap<String, ArrayList<RunwayCsv>> runwayCsvs) {

        // Get the current list of runways for this airport
        ArrayList<RunwayCsv> runways = runwayCsvs.get(runway.airport_ident);
        if (runways == null) {
            // This is the first runway for this airport
            runways = new ArrayList<RunwayCsv>(5);
            runways.add(runway);
            runwayCsvs.put(runway.airport_ident, runways);
        } else {
            // We already have at least one runway for this airport.
            if (runway.id == 0) {
                // Always add records with an Id of zero unless the runway
                // length is also zero
                if (runway.length_ft != 0.0d) {
                    runways.add(runway);
                }
            } else {
                // The Id is not zero. Either replace or delete and existing
                // record
                boolean haveNew = true;
                for (RunwayCsv existingRunway : runways) {
                    if (existingRunway.id == runway.id) {
                        // Remove the existing frequency
                        runways.remove(existingRunway);
                        if (runway.length_ft != 0.0d) {
                            // Only replace the record if the new record has a
                            // runway length otherwise its just a delete
                            runways.add(runway);
                        }
                        haveNew = false;
                        break;
                    }
                }
                if (haveNew) {
                    // We had a record Id of non zero but there was no match to
                    // remove or update.
                    // We'll add the runway unless the length is zero.
                    if (runway.length_ft != 0.0d) {
                        runways.add(runway);
                    }
                }
            }
        }
    }

    /**
     * Creates an empty List of Navaids for the aerodrome if a List does not
     * already exist and then adds the given Navaid to the List.
     *
     * @param aerodrome The aerodrome to add this Navaid to.
     *
     * @param navaids The Navaid(s) to be added.
     *
     */
    private void addNavaidToAerodrome(Aerodrome aerodrome,
            ArrayList<Navaid> navaids) {
        List<Navaid> aerodromeNavaids = aerodrome.getNavaids();
        if (aerodromeNavaids == null) {
            aerodromeNavaids = new Vector<Navaid>(5);
            aerodrome.setNavaids(aerodromeNavaids);
        }
        aerodromeNavaids.addAll(navaids);
    }

    /**
     * Given either a Navaid, Airspace or Airport, apply the filters to
     * determine if we should process this object.
     *
     * @param anObject
     *             The object to check.
     *
     * @return "true" if the Point is within our selection filter otherwise
     *             "false"
     */
    private boolean isSelectedByFilter(Object anObject) {
        boolean selected = true;

        double maxLatitude = (Double) this.properties
                .get(PROPERTYKEY_FILTERLATITUDEMAXIMUM);
        double minLatitude = (Double) this.properties
                .get(PROPERTYKEY_FILTERLATITUDEMINIMUM);
        double maxLongitude = (Double) this.properties
                .get(PROPERTYKEY_FILTERLONGITUDEMAXIMUM);
        double minLongitude = (Double) this.properties
                .get(PROPERTYKEY_FILTERLONGITUDEMINIMUM);

        if ((anObject instanceof NavaidCsv) || (anObject instanceof AirportCsv)
                || (anObject instanceof Airspace)) {
            // We have an object that we'll apply the filters to

            if (anObject instanceof NavaidCsv) {
                // We have a Navaid.
                NavaidCsv navaid = (NavaidCsv) anObject;

                // *** Filter on the latitude and longitude
                double latitude = navaid.latitude_deg;
                double longitude = navaid.longitude_deg;
                if ((latitude < minLatitude) || (latitude > maxLatitude)
                        || (longitude < minLongitude)
                        || (longitude > maxLongitude)) {
                    selected = false;
                }

                if (selected) {
                    // *** Filter on the associated airport ICAO ident
                    String identRegexp = this.properties
                            .getProperty(PROPERTYKEY_FILTERAERODROMEIDENTS);
                    if (!(identRegexp.length() == 0)) {
                        if (!navaid.associated_airport.matches(identRegexp)) {
                            selected = false;
                        }
                    }
                }
            }

            else if (anObject instanceof AirportCsv) {
                // We have an airport. Filter on country, location, Ident
                AirportCsv airport = (AirportCsv) anObject;

                // *** Filter on the ISO country code
                // Note that the property value was converted to an array during
                // initialization within the "loadProperties()" method
                String[] isoCountries = (String[]) this.properties
                        .get(PROPERTYKEY_FILTERISOCOUNTRY);
                if (isoCountries.length != 0) {
                    selected = false;
                    for (String aCountry : isoCountries) {
                        if (airport.iso_country.equals(aCountry)) {
                            selected = true;
                            break;
                        }
                    }
                }

                if (selected) {
                    // *** Filter on the ISO country and region code
                    // Note that the property value was converted to an array during
                    // initialization within the "loadProperties()" method
                    String[] isoRegions = (String[]) this.properties
                            .get(PROPERTYKEY_FILTERISOREGION);
                    if (isoRegions.length != 0) {
                        selected = false;
                        for (String aRegion : isoRegions) {
                            if (airport.iso_region.equals(aRegion)) {
                                selected = true;
                                break;
                            }
                        }
                    }
                }

                if (selected) {
                    // *** Filter on the latitude and longitude limitation
                    double latitude = airport.latitude_deg;
                    double longitude = airport.longitude_deg;
                    if ((latitude < minLatitude) || (latitude > maxLatitude)
                            || (longitude < minLongitude)
                            || (longitude > maxLongitude)) {
                        selected = false;
                    }
                }

                if (selected) {
                    // *** Filter in the airport ICAO ident
                    String identRegexp = this.properties
                            .getProperty(PROPERTYKEY_FILTERAERODROMEIDENTS);
                    if (!(identRegexp.length() == 0)) {
                        if (!airport.ident.matches(identRegexp)) {
                            selected = false;
                        }
                    }
                }
            }

            else if (anObject instanceof Airspace) {
                //TODO Build code to filter airspaces.
            }

        }

        else {
            // This isn't an object that we know about, issue a message but keep
            // on going.
            System.out.println("Unsupported filter object = "
                    + anObject.getClass().getName());
        }

        return selected;
    }

}
