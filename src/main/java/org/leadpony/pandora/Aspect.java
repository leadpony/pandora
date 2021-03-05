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

/**
 * Page aspect ratio.
 *
 * @author leadpony
 */
class Aspect {
    private final float value;

    private Aspect(float value) {
        this.value = value;
    }

    float getValue() {
        return value;
    }

    static Aspect valueOf(String value) {
        try {
            PaperSize paperSize = PaperSize.valueOf(value.toUpperCase());
            return valueOf(paperSize.getAspectRatio());
        } catch (IllegalArgumentException e) {
            return valueOf(Float.valueOf(value));
        }
    }

    static Aspect valueOf(float value) {
        return new Aspect(value);
    }
}
