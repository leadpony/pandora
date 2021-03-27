/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.leadpony.pandora;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

class ImageBasedBoundingBoxFinder implements BoundingBoxFinder {

    private final PDFRenderer renderer;
    private static final byte WHITE = -1;

    ImageBasedBoundingBoxFinder(PDDocument doc) {
        this.renderer = new PDFRenderer(doc);
    }

    @Override
    public Rectangle2D getBoundingBox(PDDocument doc, int pageIndex) throws IOException {
        BufferedImage image = renderer.renderImage(pageIndex, 1, ImageType.GRAY);
        return calculateBoundingBox(image);
    }

    private static Rectangle2D calculateBoundingBox(RenderedImage image) {
        byte[] pixels = ((DataBufferByte) image.getData().getDataBuffer()).getData();

        final int w = image.getWidth();
        final int h = image.getHeight();

        final int minX = getMinX(pixels, w, h);
        final int maxX = getMaxX(pixels, w, h);
        final int minY = getMinY(pixels, w, h);
        final int maxY = getMaxY(pixels, w, h);

        return new Rectangle2D.Double(
                minX,
                h - (maxY + 1),
                maxX - minX + 1,
                maxY - minY + 1);
    }

    private static int getMinX(byte[] pixels, int w, int h) {
        for (int x = 0; x < w; x++) {
            int index = x;
            for (int y = 0; y < h; y++) {
                if (pixels[index] != WHITE) {
                    return x;
                }
                index += w;
            }
        }
        return 0;
    }

    private static int getMaxX(byte[] pixels, int w, int h) {
        for (int x = w - 1; x >= 0; x--) {
            int index = x;
            for (int y = 0; y < h; y++) {
                if (pixels[index] != WHITE) {
                    return x;
                }
                index += w;
            }
        }
        return w - 1;
    }

    private static int getMinY(byte[] pixels, int w, int h) {
        int index = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (pixels[index++] != WHITE) {
                    return y;
                }
            }
        }
        return 0;
    }

    private static int getMaxY(byte[] pixels, int w, int h) {
        int index = pixels.length - 1;
        for (int y = h - 1; y >= 0; y--) {
            for (int x = w - 1; x >= 0; x--) {
                if (pixels[index--] != WHITE) {
                    return y;
                }
            }
        }
        return h - 1;
    }
}
