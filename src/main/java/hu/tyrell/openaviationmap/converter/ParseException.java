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

import org.w3c.dom.Node;

/**
 * An exception thrown when there are issues with parsing input files.
 */
public class ParseException extends Exception {

    /**
     * Serialization unique id.
     */
    private static final long serialVersionUID = 5019596445578509079L;

    /**
     * The XML node related to this error.
     */
    private Node node;

    /**
     * Unique aeronautical designator (airspace designator, etc.)
     * which this error relates to.
     */
    private String designator;

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

    /**
     * Constructor with a designator & description.
     *
     * @param designator the unique aviation designator this issue is about
     * @param desc the description of the issue.
     */
    public ParseException(String    designator,
                          String    desc) {
        super(desc);

        this.designator = designator;
    }

    /**
     * Constructor with a designator, description & underlying cause.
     *
     * @param designator the unique aviation designator this issue is about
     * @param desc the description of the issue.
     * @param cause the original cause of the issue.
     */
    public ParseException(String    designator,
                          String    desc,
                          Throwable cause) {
        super(desc, cause);

        this.designator = designator;
    }

    /**
     * Constructor with an XML node & description.
     *
     * @param node the XML node fromt the source document this issue is about
     * @param desc the description of the issue.
     */
    public ParseException(Node      node,
                          String    desc) {
        super(desc);

        this.node       = node;
    }

    /**
     * Constructor with an XML node & description.
     *
     * @param node the XML node fromt the source document this issue is about
     * @param cause the original cause of the issue.
     */
    public ParseException(Node      node,
                          Throwable cause) {
        super(cause);

        this.node       = node;
    }

    /**
     * Constructor with a designator, XML node, description & underlying cause.
     *
     * @param designator the unique aviation designator this issue is about
     * @param node the XML node fromt the source document this issue is about
     * @param desc the description of the issue.
     * @param cause the original cause of the issue.
     */
    public ParseException(String    designator,
                          Node      node,
                          String    desc,
                          Throwable cause) {
        super(desc, cause);

        this.designator = designator;
        this.node       = node;
    }

    /**
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * @return the designator
     */
    public String getDesignator() {
        return designator;
    }

    /**
     * @param designator the designator to set
     */
    public void setDesignator(String designator) {
        this.designator = designator;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("ParseException [designator=");
        builder.append(designator);
        builder.append(", node=");
        builder.append(node);
        builder.append(", message=");
        builder.append(getMessage());
        builder.append(", cause=");
        builder.append(getCause());
        builder.append("]");

        return builder.toString();
    }

}
