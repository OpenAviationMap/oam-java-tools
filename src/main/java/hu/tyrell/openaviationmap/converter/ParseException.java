/**
 *
 */
package hu.tyrell.openaviationmap.converter;

/**
 * An exception thrown when there are issues with parsing input files.
 */
public class ParseException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 5019596445578509079L;

    /**
     * Default constructor.
     */
    public ParseException() {
    }

    /**
     * Constructor with a simple text explanation.
     *
     * @param desc the description of the issue.
     */
    public ParseException(String desc) {
        super(desc);
    }

    /**
     * Constructor with an underlying cause.
     *
     * @param cause the original cause of the issue.
     */
    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with a description & underlying cause.
     *
     * @param desc the description of the issue.
     * @param cause the original cause of the issue.
     */
    public ParseException(String desc, Throwable cause) {
        super(desc, cause);
    }

}
