/*
 * Copyright 2020 the original author or authors.
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

import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

/**
 * @author leadpony
 */
class TextBoundingBoxFinder extends BoundingBoxFinder {

    TextBoundingBoxFinder(PDPage page) {
        super(page);
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        // Does nothing
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        // Does nothing
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        // Does nothing
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        // Does nothing
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        // Does nothing
    }
}
