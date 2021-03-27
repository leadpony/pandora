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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

interface BoundingBoxFinder {

    BoundingBoxFinder SIMPLE = new SimpleBoundingBoxFinder();

    BoundingBoxFinder TEXT_ONLY = new TextBoundingBoxFinder();

    /**
     * Returns the found bounding box.
     *
     * @param page the page for which the bounding box will be calculated.
     * @param pageIndex the page number which starts from zero.
     * @return the bounding box found, can be {@code null}.
     * @throws IOException
     */
    default Rectangle2D getBoundingBox(PDDocument doc, int pageIndex) throws IOException {
        return getBoundingBox(doc.getPage(pageIndex));
    }

    default Rectangle2D getBoundingBox(PDPage page) throws IOException {
        var box = page.getMediaBox();
        return new Rectangle2D.Double(
                box.getLowerLeftX(),
                box.getLowerLeftY(),
                box.getWidth(),
                box.getHeight());
    }
}
