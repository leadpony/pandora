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

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * A strategy for cropping pages.
 *
 * @author leadpony
 */
interface CropStrategy {

    /**
     * The strategy using the bounding box of the page.
     */
    CropStrategy BOUNDING_BOX_STRATEGY = new BoundsCropStrategy();

    /**
     * The strategy using the bounding box of the texts in the page.
     */
    CropStrategy TEXT_BOUNDING_BOX_STRATEGY = new BoundsCropStrategy() {

        @Override
        protected BoundingBoxFinder createBoundingBoxFinder(PDPage page) {
            return new TextBoundingBoxFinder(page);
        }
    };

    /**
     * Calculates the crop box of the page.
     *
     * @param page the page to crop.
     * @return the crop box calculated, must not be {@code null}.
     */
    PDRectangle getCropBox(PDPage page);
}
