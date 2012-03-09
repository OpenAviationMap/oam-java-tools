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
public class Converter {

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

            case 'h':
                System.out.println("help!");
                return;

            case '?':
                System.out.println("Invalid option '" + g.getOptopt()
                                   + "' specified");
                return;
            }
        }

        if (inputFile == null) {
            System.out.println("Required option input not specified");
            return;
        }
        if (inputFormat == null) {
            System.out.println("Required option input-format not specified");
            return;
        }
        if (outputFile == null) {
            System.out.println("Required option output not specified");
            return;
        }
        if (outputFormat == null) {
            System.out.println("Required option output-format not specified");
            return;
        }
        if (version <= 0) {
            System.out.println("Version not specified as positive ingeger");
            return;
        }

        List<Airspace> airspaces;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        if ("eAIP.Hungary".equals(inputFormat)) {
            Node eAipNode;

            try {
                DocumentBuilder db  = dbf.newDocumentBuilder();
                Document   d = db.parse(new FileInputStream(inputFile));
                eAipNode = d.getDocumentElement();

                EAIPHungaryReader reader = new EAIPHungaryReader();
                airspaces = reader.processEAIP(eAipNode);

            } catch (Exception e) {
                System.err.println(e.toString());
                return;
            }
        } else {
            System.err.println("input format " + inputFormat
                             + " not recognized");
            return;
        }

        if ("OAM".equals(outputFormat)) {
            try {
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

            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace(System.err);
                return;
            }
        } else {
            System.err.println("output format " + outputFormat
                    + " not recognized");
            return;
        }

        System.out.println("Conversion successful");
    }

}
