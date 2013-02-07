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

import hu.tyrell.openaviationmap.rendering.grid.GridFeatureBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * A builder to generate a grid of horizontal and/or vertical ortho-lines.
 *
 * @author mbedward
 * @since 8.0
 *
 *
 *
 * @source $URL$
 * @version $Id$
 */
public class OrthoLineBuilder {
    private static final double TOL = 1.0e-8;

    private final ReferencedEnvelope gridBounds;
    private boolean hasVerticals;
    private boolean hasHorizontals;
    private boolean densify;

    private SimpleFeatureBuilder featureBuilder;

    /**
     * Creates a new builder for the specified envelope.
     *
     * @param gridBounds bounds of the area for which lines will be generated
     */
    public OrthoLineBuilder(ReferencedEnvelope gridBounds) {
        this.gridBounds = gridBounds;
    }

    /**
     * Creates line features according to the provided {@code OrthoLineDef} objects and
     * places them into the provided {@link ListFeatureCollection}.
     * Densified lines (lines strings with additional vertices along their length) can be
     * created by setting the value of {@code vertexSpacing} greater than zero; if so, any
     * lines more than twice as long as this value will be densified.
     *
     * @param lineDefs line definitions specifying the orientation, spacing and level of lines
     * @param lineFeatureBuilder the feature build to create {@code SimpleFeatures} from
     *        line elements
     * @param vertexSpacing maximum distance between adjacent vertices along a line
     * @param fc the feature collection into which generated line features are placed
     */
    public void buildGrid(Collection<OrthoLineDef> lineDefs,
            GridFeatureBuilder lineFeatureBuilder, double vertexSpacing, ListFeatureCollection fc) {

        init(lineDefs, lineFeatureBuilder, vertexSpacing);

        List<OrthoLineDef> horizontal = new ArrayList<OrthoLineDef>();
        List<OrthoLineDef> vertical = new ArrayList<OrthoLineDef>();

        for (OrthoLineDef lineDef : lineDefs) {
            switch (lineDef.getOrientation()) {
                case HORIZONTAL:
                    horizontal.add(lineDef);
                    break;

                case VERTICAL:
                    vertical.add(lineDef);
                    break;
            }
        }

        doBuildLineFeatures(horizontal, LineOrientation.HORIZONTAL,
                lineFeatureBuilder, densify, vertexSpacing, fc);
        doBuildLineFeatures(vertical, LineOrientation.VERTICAL,
                lineFeatureBuilder, densify, vertexSpacing, fc);
    }


    private void doBuildLineFeatures(List<OrthoLineDef> lineDefs,
            LineOrientation orientation,
            GridFeatureBuilder lineFeatureBuilder,
            boolean densify,
            double vertexSpacing,
            ListFeatureCollection fc) {

        final int NDEFS = lineDefs.size();
        if (NDEFS > 0) {
            double minOrdinate, maxOrdinate;

            if (orientation == LineOrientation.HORIZONTAL) {
                minOrdinate = gridBounds.getMinY();
                maxOrdinate = gridBounds.getMaxY();
            } else {
                minOrdinate = gridBounds.getMinX();
                maxOrdinate = gridBounds.getMaxX();
            }

            double[] pos = new double[NDEFS];
            boolean[] active = new boolean[NDEFS];
            boolean[] atCurPos = new boolean[NDEFS];
            boolean[] generate = new boolean[NDEFS];

            Map<String, Object> attributes = new HashMap<String, Object>();
            String geomPropName = lineFeatureBuilder.getType().getGeometryDescriptor().getLocalName();

            for (int i = 0; i < NDEFS; i++) {
                pos[i] = minOrdinate;
                active[i] = true;
            }

            int numActive = NDEFS;
            while (numActive > 0) {
                /*
                 * Update scan position (curPos)
                 */
                double curPos = maxOrdinate;
                for (int i = 0; i < NDEFS; i++) {
                    if (active[i] && pos[i] < curPos - TOL) {
                        curPos = pos[i];
                    }
                }

                /*
                 * Check which line elements are at the current scan position
                 */
                for (int i = 0; i < NDEFS; i++) {
                    atCurPos[i] = active[i] && Math.abs(pos[i] - curPos) < TOL;
                }

                /*
                 * Get line with highest precedence for the current position
                 */
                System.arraycopy(atCurPos, 0, generate, 0, NDEFS);
                for (int i = 0; i < NDEFS - 1; i++) {
                    if (generate[i] && atCurPos[i]) {
                        for (int j = i + 1; j < NDEFS; j++) {
                            if (generate[j] && atCurPos[j]) {
                                if (lineDefs.get(i).getLevel() >= lineDefs.get(j).getLevel()) {
                                    generate[j] = false;
                                } else {
                                    generate[i] = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        generate[i] = false;
                    }
                }

                /*
                 * Create the line feature with highest precedence
                 */
                for (int i = 0; i < NDEFS; i++) {
                    if (generate[i]) {
                        OrthoLine element = new OrthoLine(gridBounds, orientation,
                                pos[i], lineDefs.get(i).getLevel());

                        if (lineFeatureBuilder.getCreateFeature(element)) {
                            lineFeatureBuilder.setAttributes(element, attributes);

                            if (densify) {
                                featureBuilder.set(geomPropName, element.toDenseGeometry(vertexSpacing));
                            } else {
                                featureBuilder.set(geomPropName, element.toGeometry());
                            }

                            for (String propName : attributes.keySet()) {
                                featureBuilder.set(propName, attributes.get(propName));
                            }

                            String featureID = lineFeatureBuilder.getFeatureID(element);
                            SimpleFeature feature = featureBuilder.buildFeature(featureID);
                            fc.add(feature);
                        }

                        // create a subscale if needed
                        if (lineDefs.get(i).getSubscaleSpacing() > 0
                         && lineDefs.get(i).getSubscaleLength() > 0) {

                            buildSubscale(
                                    (OrthoLineFeatureBuilder)lineFeatureBuilder,
                                    element, lineDefs.get(i), pos[i], fc);
                        }
                    }
                }

                /*
                 * Update line element positions
                 */
                for (int i = 0; i < NDEFS; i++) {
                    if (atCurPos[i]) {
                        pos[i] += lineDefs.get(i).getSpacing();
                        if (pos[i] > maxOrdinate + TOL) {
                            active[i] = false;
                            numActive-- ;
                        }
                    }
                }
            }
        }
    }

    /**
     * Build a subscale for an ortho line, by inserting small subscale lines
     * of specified length and specified intervals, perpendicular to the ortho
     * line itself.
     *
     * @param lineFeatureBuilder the line feature builder to use
     * @param element the element which the subscales are based on
     * @param lineDef the orth line definition which the subscale is based on
     * @param pi the vertical or horizontal position of the ortho line
     * @param fc the feature collection to enter the generated lines into
     */
    private void
    buildSubscale(OrthoLineFeatureBuilder   lineFeatureBuilder,
                  OrthoLine                 element,
                  OrthoLineDef              lineDef,
                  double                    pi,
                  ListFeatureCollection     fc) {

        final GeometryFactory geomFactory =
                                    JTSFactoryFinder.getGeometryFactory(null);

        Map<String, Object> attributes = new HashMap<String, Object>();
        String geomPropName = lineFeatureBuilder.getType()
                                        .getGeometryDescriptor().getLocalName();

        double q0 = pi;
        double q1 = q0 + lineDef.getSubscaleLength();

        if (lineDef.getOrientation() == LineOrientation.HORIZONTAL) {

            double upperBound = gridBounds.getMaxX();

            for (double p = gridBounds.getMinX();
                 p < upperBound;
                 p += lineDef.getSubscaleSpacing()) {

                Coordinate v0 = new Coordinate(p, q0);
                Coordinate v1 = new Coordinate(p, q1);

                lineFeatureBuilder.setAttributesSubscale(element, attributes);

                featureBuilder.set(geomPropName,
                        geomFactory.createLineString(new Coordinate[]{v0, v1}));

                for (String propName : attributes.keySet()) {
                    featureBuilder.set(propName, attributes.get(propName));
                }

                String featureID = lineFeatureBuilder.getFeatureID(element);
                SimpleFeature feature = featureBuilder.buildFeature(featureID);
                fc.add(feature);
            }
        }

        if (lineDef.getOrientation() == LineOrientation.VERTICAL) {

            double upperBound = gridBounds.getMaxY();

            for (double p = gridBounds.getMinY();
                 p < upperBound;
                 p += lineDef.getSubscaleSpacing()) {

                // adjust for the constant of the cone
                q1 = q0 + lineDef.getSubscaleLength()
                        * Math.cos(Math.toRadians(p));

                Coordinate v0 = new Coordinate(q0, p);
                Coordinate v1 = new Coordinate(q1, p);

                lineFeatureBuilder.setAttributesSubscale(element, attributes);

                featureBuilder.set(geomPropName,
                        geomFactory.createLineString(new Coordinate[]{v0, v1}));

                for (String propName : attributes.keySet()) {
                    featureBuilder.set(propName, attributes.get(propName));
                }

                String featureID = lineFeatureBuilder.getFeatureID(element);
                SimpleFeature feature = featureBuilder.buildFeature(featureID);
                fc.add(feature);
            }
        }
    }


    private boolean isValidDenseVertexSpacing(double v) {
        double minDim;

        if (hasVerticals) {
            if (hasHorizontals) {
                minDim = Math.min(gridBounds.getWidth(), gridBounds.getHeight());
            } else {
                minDim = gridBounds.getHeight();
            }
        } else {
            minDim = gridBounds.getWidth();
        }

        return v > 0 && v < minDim / 2;
    }

    private void init(Collection<OrthoLineDef> controls,
            GridFeatureBuilder lineFeatureBuilder,
            double vertexSpacing) {

        if (gridBounds == null || gridBounds.isEmpty()) {
            throw new IllegalArgumentException("gridBounds must not be null or empty");
        }
        if (controls == null || controls.isEmpty()) {
            throw new IllegalArgumentException("required one or more line parameters");
        }

        for (OrthoLineDef param : controls) {
            if (param.getOrientation() == LineOrientation.HORIZONTAL) {
                hasHorizontals = true;
            } else if (param.getOrientation() == LineOrientation.VERTICAL) {
                hasVerticals = true;
            } else {
                throw new IllegalArgumentException(
                        "Only horizontal and vertical lines are supported");
            }
        }

        densify = isValidDenseVertexSpacing(vertexSpacing);
        featureBuilder = new SimpleFeatureBuilder(lineFeatureBuilder.getType());
    }

}
