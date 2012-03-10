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
package hu.tyrell.openaviationmap.converter;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import hu.tyrell.openaviationmap.model.Airspace;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
        System.out.println(
            "Open Aviation Map converter utility");
        System.out.println("");
        System.out.println(
            "usage:");
        System.out.println("");
        System.out.println(
            "  -i | --input <input.file>");
        System.out.println(
            "                             specify the input file, required");
        System.out.println(
            "  -f | --input-format <input.format>");
        System.out.println(
            "                             specify the input format, required");
        System.out.println(
            "                             supported formats: eAIP.Hungary");
        System.out.println(
            "  -o | --output <output.file>");
        System.out.println(
            "                             specify the output file, required");
        System.out.println(
            "  -F | --output-format <output.format>");
        System.out.println(
            "                             specify the output format, required");
        System.out.println(
            "                             supported formats: OAM");
        System.out.println(
            "  -c | --create");
        System.out.println(
            "                             if specified, the OAM output file");
        System.out.println(
            "                             is created in 'create' mode, for");
        System.out.println(
            "                             adding new OAM nodes & ways");
        System.out.println(
            "  -v | --version");
        System.out.println(
            "                             specify the OAM node versions");
        System.out.println(
            "                             required if output format is OAM");
        System.out.println(
            "  -h | --help");
        System.out.println(
            "                             show this usage page");
    }

    /**
     * Program entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        LongOpt[] longopts = new LongOpt[7];

        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("input", LongOpt.REQUIRED_ARGUMENT,
                null, 'i');
        longopts[2] = new LongOpt("input-format", LongOpt.REQUIRED_ARGUMENT,
                null, 'f');
        longopts[3] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                null, 'o');
        longopts[4] = new LongOpt("output-format", LongOpt.REQUIRED_ARGUMENT,
                null, 'F');
        longopts[5] = new LongOpt("create", LongOpt.NO_ARGUMENT,
                null, 'c');
        longopts[6] = new LongOpt("version", LongOpt.REQUIRED_ARGUMENT,
                null, 'v');

        Getopt g = new Getopt("Converter", args, "hi:f:o:F:cv:", longopts);

        int c;

        String  inputFile    = null;
        String  inputFormat  = null;
        String  outputFile   = null;
        String  outputFormat = null;
        boolean create       = false;
        int     version      = 0;

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

            case 'v':
                version = Integer.parseInt(g.getOptarg());
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
        if (version <= 0) {
            System.out.println("Version not specified as positive ingeger");
            System.out.println();
            printHelpMessage();
            return;
        }

        try {
            convert(inputFile,
                    inputFormat,
                    outputFile,
                    outputFormat,
                    create,
                    version);
        } catch (Exception e) {
            System.out.println("Conversion failed.");
            return;
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
     * @param create flag to indicate if OAM create mode is to be used
     * @param version the OAM node / way version to be set
     * @throws Exception on conversion problems.
     */
    private static void convert(String   inputFile,
                                String   inputFormat,
                                String   outputFile,
                                String   outputFormat,
                                boolean  create,
                                int      version)        throws Exception {
        List<Airspace> airspaces;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        if ("eAIP.Hungary".equals(inputFormat)) {
            Node eAipNode;

            DocumentBuilder db  = dbf.newDocumentBuilder();
            Document   d = db.parse(new FileInputStream(inputFile));
            eAipNode = d.getDocumentElement();

            EAIPHungaryReader reader = new EAIPHungaryReader();
            airspaces = reader.processEAIP(eAipNode);
        } else {
            throw new Exception("input format " + inputFormat
                              + " not recognized");
        }

        if ("OAM".equals(outputFormat)) {
            DocumentBuilder db  = dbf.newDocumentBuilder();
            Document d = db.newDocument();
            OAMWriter writer = new OAMWriter();

            d = writer.processAirspaces(d, airspaces, 0, 0,
                                        create, version);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(d);
            StreamResult result = new StreamResult(outputFile);
            transformer.transform(source, result);

        } else {
            throw new Exception("output format " + outputFormat
                              + " not recognized");
        }
    }

}
