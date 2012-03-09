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
