/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package hu.tyrell.openaviationmap.rendering.grid.ortholine;

/**
 * Defines how to generate a set of regularly-spaced, ortho-line elements with
 * given orientation and level.
 *
 * @author mbedward
 * @since 8.0
 *
 *
 *
 * @source $URL$
 * @version $Id$
 */
public class OrthoLineDef {

    private final int level;
    private final LineOrientation orientation;
    private final double spacing;
    private final double subscaleSpacing;
    private final double subscaleLength;

    /**
     * Creates a new ortho-line definition.
     *
     * @param orientation line orientation
     * @param level an integer level (user-defined values) indicating line precedence
     * @param spacing the spacing between lines in world distance units
     */
    public OrthoLineDef(LineOrientation orientation, int level, double spacing) {
        this.level = level;
        this.orientation = orientation;
        this.spacing = spacing;

        subscaleSpacing = -1;
        subscaleLength = -1;
    }

    /**
     * Creates a new ortho-line definition with a subscale on the orth-line
     *
     * @param orientation line orientation
     * @param level an integer level (user-defined values) indicating line precedence
     * @param spacing the spacing between lines in world distance units
     * @param subscaleSpacing the spacing of the subscale on the line
     * @param subscaleLength the lenght of each subscale line
     */
    public OrthoLineDef(LineOrientation orientation, int level, double spacing,
                        double subscaleSpacing, double subscaleLength) {
        this.level = level;
        this.orientation = orientation;
        this.spacing = spacing;

        this.subscaleSpacing = subscaleSpacing;
        this.subscaleLength = subscaleLength;
    }

    /**
     * Creates a copy of an existing line definition.
     *
     * @param lineDef the definition to copy
     * @throws IllegalArgumentException if {@code lineDef} is {@code null}
     */
    public OrthoLineDef(OrthoLineDef lineDef) {
        if (lineDef == null) {
            throw new IllegalArgumentException("lineDef arg must not be null");
        }
        this.level = lineDef.level;
        this.orientation = lineDef.orientation;
        this.spacing = lineDef.spacing;
        this.subscaleSpacing = lineDef.subscaleSpacing;
        this.subscaleLength = lineDef.subscaleLength;
    }

    /**
     * Gets the integer level (line precedence).
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the orientation.
     *
     * @return orientation
     */
    public LineOrientation getOrientation() {
        return orientation;
    }

    /**
     * Gets the spacing between lines.
     *
     * @return line spacing
     */
    public double getSpacing() {
        return spacing;
    }

    /**
     * Returns the subscale spacing, -1 means no subscale.
     *
     * @return the subscale spacing
     */
    public double getSubscaleSpacing() {
        return subscaleSpacing;
    }

    /**
     * Returns the subscale length, -1 means no subscale.
     *
     * @return the subscale length
     */
    public double getSubscaleLength() {
        return subscaleLength;
    }

}
