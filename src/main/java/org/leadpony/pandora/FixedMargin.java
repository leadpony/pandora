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

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @author leadpony
 */
class FixedMargin implements Margin {

    private final Length top;
    private final Length right;
    private final Length bottom;
    private final Length left;

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

        return new FixedMargin(entries);
    }

    private FixedMargin(List<Length> entries) {
        this.top = entries.get(0);
        this.right = entries.get(1);
        this.bottom = entries.get(2);
        this.left = entries.get(3);
    }

    private FixedMargin(Length top, Length right, Length bottom, Length left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    @Override
    public CropStrategy createStrategy(PDDocument doc, CroppingContext context) {
        return new FixedCropStrategy(this);
    }

    @Override
    public Margin flip() {
        return new FixedMargin(top, left, bottom, right);
    }

    PDRectangle getCropBox(PDPage page) {
        PDRectangle rect = page.getMediaBox();

        final float top = this.top.apply(rect.getHeight());
        final float right = this.right.apply(rect.getWidth());
        final float bottom = this.bottom.apply(rect.getHeight());
        final float left = this.left.apply(rect.getWidth());

        final float x = rect.getLowerLeftX() + left;
        final float y = rect.getLowerLeftY() + bottom;
        final float width = rect.getWidth() - (left + right);
        final float height = rect.getHeight() - (top + bottom);

        return new PDRectangle(x, y, width, height);
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

        @Override
        public String toString() {
            return String.valueOf(this.value);
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

    static class FixedCropStrategy implements CropStrategy {

        private final FixedMargin margin;

        FixedCropStrategy(FixedMargin margin) {
            this.margin = margin;
        }

        @Override
        public PDRectangle getCropBox(PDPage page, int pageNo) {
            return margin.getCropBox(page);
        }
    }
}
