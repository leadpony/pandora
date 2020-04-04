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

import java.util.Objects;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A margin option.
 *
 * @author leadpony
 */
interface Margin {

    Margin BOUNDING_BOX_MARGIN = doc -> CroppingStrategy.BOUNDING_BOX_STRATEGY;

    /**
     * Creates an instance of Margin from the specified string value.
     *
     * @param value the value of the option parameter.
     * @return newly created instance of margin.
     */
    static Margin valueOf(String value) {
        Objects.requireNonNull(value, "value must not be null.");
        if ("bbox".equalsIgnoreCase(value)) {
            return BOUNDING_BOX_MARGIN;
        }
        return FixedMargin.valueOf(value);
    }

    /**
     * Creates a cropping strategy for this margin.
     *
     * @param doc the document to crop.
     * @return newly created instance of cropping strategy.
     */
    CroppingStrategy createStrategy(PDDocument doc);
}
