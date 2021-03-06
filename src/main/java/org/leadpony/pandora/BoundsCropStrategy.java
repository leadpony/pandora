/*
 * Copyright 2020-2021 the original author or authors.
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
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * The strategy using the bounding box to crop pages.
 *
 * @author leadpony
 */
class BoundsCropStrategy implements CropStrategy {

    private final float padding;

    BoundsCropStrategy(CroppingContext context) {
        this.padding = context.getPadding();
    }

    @Override
    public PDRectangle getCropBox(PDPage page, int pageNo) {
        BoundingBoxFinder finder = createBoundingBoxFinder(page);
        try {
            finder.processPage(page);
            Rectangle2D box = finder.getBoundingBox();
            if (box == null) {
                return page.getMediaBox();
            }
            return rectangleFrom(box, this.padding);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected BoundingBoxFinder createBoundingBoxFinder(PDPage page) {
        return new BoundingBoxFinder(page);
    }

    private static PDRectangle rectangleFrom(Rectangle2D box, float padding) {
        float minX = (float) box.getMinX();
        float minY = (float) box.getMinY();
        float maxX = (float) box.getMaxX();
        float maxY = (float) box.getMaxY();

        if (padding > 0) {
            minX -= padding;
            minY -= padding;
            maxX += padding;
            maxY += padding;
        }

        return new PDRectangle(minX, minY, maxX - minX, maxY - minY);
    }
}
