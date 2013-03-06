package org.openaviationmap.converter.ourairports;

/**
 * Exception thrown when any of the OurAirports csv files fails to validate.
 *
 * @author Raymond
 *
 */
public class OurAirportsValidationException extends Exception {

    /**  For standard Java serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Standard constructor to set the message text for the Exception.
     *
     * @param message
     *            The message text to associate with the Exception
     */
    public OurAirportsValidationException(String message) {
        super(message);
    }

}
