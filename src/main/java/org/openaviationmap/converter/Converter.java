/*
    Open Aviation Map
    Copyright (C) 2012-2013 Ákos Maróy

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
package org.openaviationmap.converter;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.openaviationmap.converter.eaip.EAipProcessorAd13;
import org.openaviationmap.converter.kml.KmlWriter;
import org.openaviationmap.converter.ourairports.OurAirportsReader;
import org.openaviationmap.model.Aerodrome;
import org.openaviationmap.model.Airspace;
import org.openaviationmap.model.Navaid;
import org.openaviationmap.model.Point;
import org.openaviationmap.model.oam.Action;
import org.openaviationmap.model.oam.Oam;
import org.openaviationmap.model.oam.Way;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import aero.aixm.schema._5_1.message.AIXMBasicMessageType;

/**
 * A command line tool to convert between various formats.
 */
public final class Converter {
    /**
     * Private default constructor.
     */
    private Converter() {
    }

    /**
     * Print a help message.
     */
    private static void printHelpMessage() {
        //@formatter:off
        //CHECKSTYLE:OFF
        System.out.println(
            "Open Aviation Map converter utility\n"
          + "\n"
          + "usage:\n"
          + ""
          + "  -i | --input <input.file>             Specify the input file, [required]     \n"
          + "  -f | --input-format <input.format>    Specify the input format, [required]\n"
          + "                                        Supported formats: eAIP.Hungary,\n"
          + "                                        OurAirports\n"
          + "  -o | --output <output.file>           Specify the output file, [required]\n"
          + "  -F | --output-format <output.format>  Specify the output format, [required]\n"
          + "                                        Supported formats: OAM, AIXM, KML\n"
          + "  -p | --output-options                 The file name of the properties file \n"
          + "                                        which contains output formater options.\n"
          + "                                        Currently only used by the KML output\n"
          + "                                        formatter. [optional]\n"
          + "  -c | --create                         If specified, the OAM output file is\n"
          + "                                        created in 'create' mode, for adding\n"
          + "                                        new OAM nodes & ways. [[optional]\n"
          + "  -b | --border                         A border polygon file in OSM format to\n"
          + "                                        be used for airspaces that refer to\n"
          + "                                        national borders. [optional]\n"
          + "  -a | --aerodromes                     An aerodrome index file, in the eAIP\n"
          + "                                        section AD 1.3 format. used when\n"
          + "                                        processing aerodromes from eAIP.\n"
          + "                                        [optional].\n"
          + "  -s | --validity-start                 The start of the validity period for \n"
          + "                                        the data to be converted. Only \n"
          + "                                        relevant for AIXM output formats.\n"
          + "                                        Accepted format: YYYY-MM-DDTHH:MM:SSZ\n"
          + "  -e | --validity-end                   The end of the validity period for \n"
          + "                                        the data to be converted. Only\n"
          + "                                        relevant for AIXM output formats.\n"
          + "                                        Accepted format:\n"
          + "                                        YYYY-MM-DDTHH:MM:SSZ\n"
          + "  -v | --version                        Specify the OAM node version\n"
          + "                                        Required if output format is OAM\n"
          + "  -h | --help                           Show this usage page\n\n"
          );
        //CHECKSTYLE:ON
        //@formatter:on
    }

    /**
     * Program entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        LongOpt[] longopts = new LongOpt[12];

        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] =
                new LongOpt("input", LongOpt.REQUIRED_ARGUMENT, null, 'i');
        longopts[2] =
                new LongOpt("input-format", LongOpt.REQUIRED_ARGUMENT, null,
                        'f');
        longopts[3] =
                new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
        longopts[4] =
                new LongOpt("output-format", LongOpt.REQUIRED_ARGUMENT, null,
                        'F');
        longopts[5] = new LongOpt("create", LongOpt.NO_ARGUMENT, null, 'c');
        longopts[6] =
                new LongOpt("border", LongOpt.REQUIRED_ARGUMENT, null, 'b');
        longopts[7] =
                new LongOpt("aerodromes", LongOpt.REQUIRED_ARGUMENT, null, 'a');
        longopts[8] =
                new LongOpt("validity-start", LongOpt.REQUIRED_ARGUMENT, null,
                        's');
        longopts[9] =
                new LongOpt("validity-end", LongOpt.REQUIRED_ARGUMENT, null,
                        'e');
        longopts[10] =
                new LongOpt("version", LongOpt.REQUIRED_ARGUMENT, null, 'v');
        longopts[11] =
                new LongOpt("output-options", LongOpt.REQUIRED_ARGUMENT, null,
                        'p');

        Getopt g =
                new Getopt("Converter", args, "hi:f:o:F:cb:a:s:e:v:p:",
                        longopts);

        int c;

        String inputFile = null;
        String inputFormat = null;
        String outputFile = null;
        String outputFormat = null;
        boolean create = false;
        String borderFile = null;
        String adFile = null;
        GregorianCalendar validityStart = null;
        GregorianCalendar validityEnd = null;
        int version = 0;
        /* Holds the value of the "outputProperties" command line arg.        */
        String outputOptions = null;
        List<ParseException> errors = new Vector<ParseException>();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'i':
                inputFile = g.getOptarg();
                break;

            case 'f':
                inputFormat = g.getOptarg();
                break;

            case 'o':
                outputFile = g.getOptarg();
                break;

            case 'F':
                outputFormat = g.getOptarg();
                break;

            case 'c':
                create = true;
                break;

            case 'b':
                borderFile = g.getOptarg();
                break;

            case 'a':
                adFile = g.getOptarg();
                break;

            case 's':
                try {
                    Date d = dateFormat.parse(g.getOptarg());
                    validityStart =
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    validityStart.setTime(d);
                } catch (java.text.ParseException e) {
                    System.out.println("unable to parse validity date string "
                            + g.getOptarg());
                    return;
                }
                break;

            case 'e':
                try {
                    Date d = dateFormat.parse(g.getOptarg());
                    validityEnd =
                            new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                    validityEnd.setTime(d);
                } catch (java.text.ParseException e) {
                    System.out.println("unable to parse validity date string "
                            + g.getOptarg());
                    return;
                }
                break;

            case 'v':
                version = Integer.parseInt(g.getOptarg());
                break;

            case 'p':
                outputOptions = g.getOptarg();
                break;

            default:
            case 'h':
                printHelpMessage();
                return;

            case '?':
                System.out.println("Invalid option '" + g.getOptopt()
                        + "' specified");
                return;
            }
        }

        if (inputFile == null) {
            System.out.println("Required option input not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (inputFormat == null) {
            System.out.println("Required option input-format not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (outputFile == null) {
            System.out.println("Required option output not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if (outputFormat == null) {
            System.out.println("Required option output-format not specified");
            System.out.println();
            printHelpMessage();
            return;
        }
        if ((outputFormat.equalsIgnoreCase("KML"))
                && ((outputOptions == null) || (outputOptions.length() == 0))) {
            System.out.println("Required option \"output-options\" when"
                    + " out-format is \"KML\" was not specified");
            System.out.println();
            printHelpMessage();
            return;
        }

        if (outputFormat.equalsIgnoreCase("OAM") && version <= 0) {
            System.out.println("Version not specified as positive ingeger");
            System.out.println();
            printHelpMessage();
            return;
        }

        System.out.println("Converting " + inputFile + " to " + outputFile);

        try {
            convert(inputFile, inputFormat, outputFile, outputFormat,
                    create ? Action.CREATE : Action.NONE, borderFile, adFile,
                    outputOptions, validityStart, validityEnd, version, errors);
        } catch (Exception e) {
            System.out.println("Conversion failed.");
            System.out.println();
            System.out.println("error details: " + e.toString());
            e.printStackTrace(System.out);
            return;
        }

        if (!errors.isEmpty()) {
            System.out.println("The following errors were encountered:");
            for (ParseException e : errors) {
                System.out.println(e.toString());
            }
            System.out.println();
        }

        System.out.println("Conversion successful");
    }

    /**
     * Perform the conversion itself.
     *
     * @param inputFile the name of the input file
     * @param inputFormat the name of the input format
     * @param outputFile the name of the output file
     * @param outputFormat the name of the output format
     * @param action the action to specify for all OAM elements
     * @param borderFile a file describing the country border to be used
     *        for airspaces that refer to country borders. may be null
     * @param adFile a file containing an aerodrome index, in the eAIP AD 1.3
     *        format. used when processing eAIP Aerodrome definitions.
     *        may be null.
     * @param outputProperties a Java Properties file which is used by the
     *        output formatter to control its function and the output
     *        generate. Currently only used by the KML formatter.
     * @param validityStart the start of the validity period for the data to be
     *        converted. may be null if unknown
     * @param validityEnd the end of the validity period for the data to be
     *        converted. may be null if unknown
     * @param version the OAM node / way version to be set
     * @param errors all parsing errors will be put into this list
     * @throws Exception on conversion problems.
     */
    public static void convert(String inputFile, String inputFormat,
            String outputFile, String outputFormat, Action action,
            String borderFile, String adFile, String outputProperties,
            GregorianCalendar validityStart, GregorianCalendar validityEnd,
            int version, List<ParseException> errors) throws Exception {

        List<Airspace> airspaces = new Vector<Airspace>();
        List<Navaid> navaids = new Vector<Navaid>();
        List<Aerodrome> aerodromes = new Vector<Aerodrome>();
        List<Point> borderPoints = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        if (borderFile != null) {
            Document d = db.parse(new FileInputStream(borderFile));
            OAMReader reader = new OAMReader();
            Oam oam = new Oam();
            reader.processOsm(d.getDocumentElement(), oam, errors);

            if (!oam.getWays().isEmpty()) {
                // extract the border as a point list
                Way border = oam.getWays().values().iterator().next();

                borderPoints = new Vector<Point>(border.getNodeList().size());
                for (Integer ref : border.getNodeList()) {
                    borderPoints.add(oam.getNodes().get(ref).asPoint());
                }
            }
        }

        if (adFile != null) {
            Document d = db.parse(new FileInputStream(adFile));

            if ("e:AD-1.3".equals(d.getDocumentElement().getTagName())) {
                EAipProcessorAd13 p = new EAipProcessorAd13();
                p.processEAIP(d.getDocumentElement(), borderPoints, airspaces,
                        navaids, aerodromes, errors);
            }

        }

        if ("eAIP.Hungary".equals(inputFormat)) {
            Node eAipNode;

            Document d = db.parse(new FileInputStream(inputFile));
            eAipNode = d.getDocumentElement();

            EAIPHungaryReader reader = new EAIPHungaryReader();
            reader.processEAIP(eAipNode, borderPoints, airspaces, navaids,
                    aerodromes, errors);
        }

        // Check for an input file type of "OurAirports" and process it
        else if ("OurAirports".equals(inputFormat)) {
            OurAirportsReader ourAirportsReader =
                    new OurAirportsReader(inputFile);
            ourAirportsReader.processOurAirports(airspaces, navaids,
                    aerodromes, errors);
        }

        // We didn't find a valid input format. Throw an Exception.
        else {
            throw new Exception("input format " + inputFormat
                    + " not recognized");
        }

        ////////////////////////////////////////////////////////////////////////
        //    Generate the output
        ////////////////////////////////////////////////////////////////////////
        if ("OAM".equals(outputFormat)) {
            // convert the airspaces to OAM
            Oam oam = new Oam();

            OamConverter.airspacesToOam(airspaces, oam, action, version, 1);
            OamConverter.navaidsToOam(navaids, oam, action, version,
                    oam.getMaxNodeId() + 1);
            OamConverter.aerodromesToOam(aerodromes, oam, action, version,
                    oam.getMaxNodeId() + 1);

            OAMWriter.write(oam, new FileWriter(outputFile));

        } else if ("AIXM".equals(outputFormat)) {
            // convert
            JAXBElement<AIXMBasicMessageType> m =
                    AixmConverter
                            .convertToAixm(airspaces, navaids, aerodromes,
                                    validityStart, validityEnd, "BASELINE",
                                    version, 0L);

            // marshal the data into XML using the JAXB marshaller
            JAXBContext ctx =
                    JAXBContext.newInstance("aero.aixm.schema._5_1.message");
            Marshaller marsh = ctx.createMarshaller();

            Document document = db.newDocument();
            marsh.marshal(m, document);

            ConverterUtil.canonizeNS(document, AixmConverter.getNsCtx());

            ConverterUtil.serializeDocument(document, new FileOutputStream(
                    outputFile));

        } else if ("KML".equals(outputFormat)) {
            KmlWriter kmlWriter = new KmlWriter(outputFile, outputProperties);
            kmlWriter.convertToKml(airspaces, navaids, aerodromes);
            kmlWriter.close();

        } else {
            throw new Exception("output format " + outputFormat
                    + " not recognized");
        }
    }

}
