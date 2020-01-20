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

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @author leadpony
 */
class Margin {

    private final Length top;
    private final Length right;
    private final Length bottom;
    private final Length left;

    private Margin(List<Length> entries) {
        this.top = entries.get(0);
        this.right = entries.get(1);
        this.bottom = entries.get(2);
        this.left = entries.get(3);
    }

    PDRectangle getRectangle(PDRectangle page) {
        final float top = this.top.apply(page.getHeight());
        final float right = this.right.apply(page.getWidth());
        final float bottom = this.bottom.apply(page.getHeight());
        final float left = this.left.apply(page.getWidth());

        final float x = page.getLowerLeftX() + left;
        final float y = page.getLowerLeftY() + bottom;
        final float width = page.getWidth() - (left + right);
        final float height = page.getHeight() - (top + bottom);

        return new PDRectangle(x, y, width, height);
    }

    static Margin valueOf(String value) {
        String[] parts = value.split(",");
        if (parts.length < 1 || parts.length > 4) {
            throw new IllegalArgumentException();
        }
        List<Length> entries = new ArrayList<>();
        for (String part : parts) {
            entries.add(createLength(part));
        }

        switch (entries.size()) {
        case 1:
            entries.add(entries.get(0));
        case 2:
            entries.add(entries.get(0));
        case 3:
            entries.add(entries.get(1));
            break;
        default:
            break;
        }

        return new Margin(entries);
    }

    private static Length createLength(String value) {
        if (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
            return new Percentage(Float.valueOf(value));
        } else {
            return new Length(Float.valueOf(value));
        }
    }

    private static class Length {
        protected final float value;

        Length(float value) {
            this.value = value;
        }

        float apply(float fullLength) {
            return value;
        }
    }

    private static class Percentage extends Length {

        Percentage(float value) {
            super(value);
        }

        @Override
        float apply(float fullLength) {
            return fullLength * value / 100.f;
        }
    }
}
