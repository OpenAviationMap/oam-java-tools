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
package org.openaviationmap.rendering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test cases for the UOM class.
 */
public class UOMTest {

    /**
     * Test incorrect input.
     */
    @Test
    public void testBadInput() {
        boolean caught = false;
        try {
            UOM.scaleValue("XXm", 1, 90);
        } catch (RenderException e) {
            caught = true;
        }
        assertTrue("expected RenderingException not caught", caught);

        caught = false;
        try {
            UOM.scaleValue("XXmX", 1, 90);
        } catch (RenderException e) {
            caught = true;
        }
        assertTrue("expected RenderingException not caught", caught);

        caught = false;
        try {
            UOM.scaleValue("1m", 0, 90);
        } catch (RenderException e) {
            caught = true;
        }
        assertTrue("expected RenderingException not caught", caught);

    }

    /**
     * Test strange input.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testFunnyInput() throws RenderException {
        assertEquals(4d, UOM.scaleValue("1 m", 1000, 90), 1.0);
        assertEquals(-4d, UOM.scaleValue("-1.0 m", 1000, 90), 1.0);
    }


    /**
     * Test meter scaling.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testScalingMeters() throws RenderException {
        // meters at 90dpi
        assertEquals(3543d, UOM.scaleValue("1m", 1, 90), 1.0);
        assertEquals(4d,    UOM.scaleValue("1m", 1000, 90), 1.0);
        assertEquals(354d,  UOM.scaleValue("100m", 1000, 90), 1.0);
        assertEquals(71d,   UOM.scaleValue("100m", 5000, 90), 1.0);

        // meters at 180dpi
        assertEquals(7087d, UOM.scaleValue("1m", 1, 180), 1.0);
        assertEquals(8d,    UOM.scaleValue("1m", 1000, 180), 1.0);
        assertEquals(708d,  UOM.scaleValue("100m", 1000, 180), 1.0);
        assertEquals(142d,  UOM.scaleValue("100m", 5000, 180), 1.0);

        // meters at 600dpi
        assertEquals(23622d, UOM.scaleValue("1m", 1, 600), 1.0);
        assertEquals(24d,    UOM.scaleValue("1m", 1000, 600), 1.0);
        assertEquals(2362d,  UOM.scaleValue("100m", 1000, 600), 1.0);
        assertEquals(472d,   UOM.scaleValue("100m", 5000, 600), 1.0);

        // meters at the default DPI
        assertEquals(3571d, UOM.scaleValue("1m", 1, ScaleSLD.DEFAULT_DPI), 1.0);
        assertEquals(4d, UOM.scaleValue("1m", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(357d, UOM.scaleValue("100m", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(71d, UOM.scaleValue("100m", 5000, ScaleSLD.DEFAULT_DPI),
                     1.0);
    }

    /**
     * Test feet scaling.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testScalingFeet() throws RenderException {
        // feet at 90dpi
        assertEquals(1080d, UOM.scaleValue("1ft", 1, 90), 1.0);
        assertEquals(1d,    UOM.scaleValue("1ft", 1000, 90), 1.0);
        assertEquals(108d,  UOM.scaleValue("100ft", 1000, 90), 1.0);
        assertEquals(22d,   UOM.scaleValue("100ft", 5000, 90), 1.0);

        // feet at 180dpi
        assertEquals(2160d, UOM.scaleValue("1ft", 1, 180), 1.0);
        assertEquals(2d,    UOM.scaleValue("1ft", 1000, 180), 1.0);
        assertEquals(216d,  UOM.scaleValue("100ft", 1000, 180), 1.0);
        assertEquals(43d,   UOM.scaleValue("100ft", 5000, 180), 1.0);

        // feet at 600dpi
        assertEquals(7200d,  UOM.scaleValue("1ft", 1, 600), 1.0);
        assertEquals(7d,     UOM.scaleValue("1ft", 1000, 600), 1.0);
        assertEquals(720d,   UOM.scaleValue("100ft", 1000, 600), 1.0);
        assertEquals(144d,   UOM.scaleValue("100ft", 5000, 600), 1.0);

        // feet at the default DPI
        assertEquals(1089d, UOM.scaleValue("1ft", 1, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(1d, UOM.scaleValue("1ft", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(109d, UOM.scaleValue("100ft", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(22d, UOM.scaleValue("100ft", 5000, ScaleSLD.DEFAULT_DPI),
                     1.0);
    }

    /**
     * Test nautical mile scaling.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testScalingNm() throws RenderException {
        // feet at 90dpi
        assertEquals(6562204d, UOM.scaleValue("1nm", 1, 90), 1.0);
        assertEquals(6562d,    UOM.scaleValue("1nm", 1000, 90), 1.0);
        assertEquals(1312d,    UOM.scaleValue("1nm", 5000, 90), 1.0);

        // feet at 180dpi
        assertEquals(13124409d, UOM.scaleValue("1nm", 1, 180), 1.0);
        assertEquals(13124d,    UOM.scaleValue("1nm", 1000, 180), 1.0);
        assertEquals(2625d,     UOM.scaleValue("1nm", 5000, 180), 1.0);

        // feet at 600dpi
        assertEquals(43748031d,  UOM.scaleValue("1nm", 1, 600), 1.0);
        assertEquals(43748d,     UOM.scaleValue("1nm", 1000, 600), 1.0);
        assertEquals(8749d,      UOM.scaleValue("1nm", 5000, 600), 1.0);

        // feet at the default DPI
        assertEquals(6614285d, UOM.scaleValue("1nm", 1, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(6614d, UOM.scaleValue("1nm", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(1322d, UOM.scaleValue("1nm", 5000, ScaleSLD.DEFAULT_DPI),
                     1.0);
    }

    /**
     * Test millimeter scaling.
     * These values are not dependent on the scaling factor, only on the DPI
     * value.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testScalingMm() throws RenderException {
        // millimeters at 90dpi
        assertEquals(4d,    UOM.scaleValue("1mm", 1, 90), 1.0);
        assertEquals(4d,    UOM.scaleValue("1mm", 1000, 90), 1.0);
        assertEquals(354d,  UOM.scaleValue("100mm", 1000, 90), 1.0);
        assertEquals(354d,  UOM.scaleValue("100mm", 5000, 90), 1.0);

        // millimeters at 180dpi
        assertEquals(7d,    UOM.scaleValue("1mm", 1, 180), 1.0);
        assertEquals(7d,    UOM.scaleValue("1mm", 1000, 180), 1.0);
        assertEquals(708d,  UOM.scaleValue("100mm", 1000, 180), 1.0);
        assertEquals(708d,  UOM.scaleValue("100mm", 5000, 180), 1.0);

        // millimeters at 600dpi
        assertEquals(24d,    UOM.scaleValue("1mm", 1, 600), 1.0);
        assertEquals(24d,    UOM.scaleValue("1mm", 1000, 600), 1.0);
        assertEquals(2362d,  UOM.scaleValue("100mm", 1000, 600), 1.0);
        assertEquals(2362d,   UOM.scaleValue("100mm", 5000, 600), 1.0);

        // millimeters at the default DPI
        assertEquals(4d, UOM.scaleValue("1mm", 1, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(4d, UOM.scaleValue("1mm", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(357d, UOM.scaleValue("100mm", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(357d, UOM.scaleValue("100mm", 5000, ScaleSLD.DEFAULT_DPI),
                     1.0);
    }

    /**
     * Test inch scaling.
     * These values are not dependent on the scaling factor, only on the DPI
     * value.
     *
     * @throws RenderException on parsing errors
     */
    @Test
    public void testScalingInch() throws RenderException {
        // millimeters at 90dpi
        assertEquals(90d,    UOM.scaleValue("1in", 1, 90), 1.0);
        assertEquals(90d,    UOM.scaleValue("1in", 1000, 90), 1.0);
        assertEquals(9000d,  UOM.scaleValue("100in", 1000, 90), 1.0);
        assertEquals(9000d,  UOM.scaleValue("100in", 5000, 90), 1.0);

        // millimeters at 180dpi
        assertEquals(180d,    UOM.scaleValue("1in", 1, 180), 1.0);
        assertEquals(180d,    UOM.scaleValue("1in", 1000, 180), 1.0);
        assertEquals(18000d,  UOM.scaleValue("100in", 1000, 180), 1.0);
        assertEquals(18000d,  UOM.scaleValue("100in", 5000, 180), 1.0);

        // millimeters at 600dpi
        assertEquals(600d,    UOM.scaleValue("1in", 1, 600), 1.0);
        assertEquals(600d,    UOM.scaleValue("1in", 1000, 600), 1.0);
        assertEquals(60000d,  UOM.scaleValue("100in", 1000, 600), 1.0);
        assertEquals(60000d,   UOM.scaleValue("100in", 5000, 600), 1.0);

        // millimeters at the default DPI
        assertEquals(91d, UOM.scaleValue("1in", 1, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(91d, UOM.scaleValue("1in", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(9071d, UOM.scaleValue("100in", 1000, ScaleSLD.DEFAULT_DPI),
                     1.0);
        assertEquals(9071d, UOM.scaleValue("100in", 5000, ScaleSLD.DEFAULT_DPI),
                     1.0);
    }
}
