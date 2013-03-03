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

import java.awt.image.BufferedImage;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 * A batik transcoder class that writes into a buffered image.
 *
 * @see http://bbgen.net/blog/2011/06/java-svg-to-bufferedimage/
 */
class BufferedImageTranscoder extends ImageTranscoder {

    /** The buffered image to write into. */
    private BufferedImage img = null;

    @Override
    public BufferedImage createImage(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        return bi;
    }

    @Override
    public void writeImage(BufferedImage image, TranscoderOutput output) {
        this.img = image;
    }

    public BufferedImage getBufferedImage() {
        return img;
    }
}
