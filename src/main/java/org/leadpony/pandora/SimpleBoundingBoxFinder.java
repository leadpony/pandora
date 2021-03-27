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

import org.apache.pdfbox.pdmodel.PDPage;

class SimpleBoundingBoxFinder implements BoundingBoxFinder {

    @Override
    public Rectangle2D getBoundingBox(PDPage page) throws IOException {
        var calculator = createCalculator(page);
        calculator.processPage(page);
        return calculator.getBoundingBox();
    }

    protected BoundingBoxCalculator createCalculator(PDPage page) {
        return new BoundingBoxCalculator(page);
    }
}
