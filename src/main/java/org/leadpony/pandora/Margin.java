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

    private final Entry top;
    private final Entry right;
    private final Entry bottom;
    private final Entry left;

    private Margin(List<Entry> entries) {
        this.top = entries.get(0);
        this.right = entries.get(1);
        this.bottom = entries.get(2);
        this.left = entries.get(3);
    }

    PDRectangle getRectangle(PDRectangle whole) {
        final float top = this.top.getValue(whole.getHeight());
        final float right = this.right.getValue(whole.getWidth());
        final float bottom = this.bottom.getValue(whole.getHeight());
        final float left = this.left.getValue(whole.getWidth());

        final float x = whole.getLowerLeftX() + left;
        final float y = whole.getLowerLeftY() + bottom;
        final float width = whole.getWidth() - (left + right);
        final float height = whole.getHeight() - (top + bottom);

        return new PDRectangle(x, y, width, height);
    }

    static Margin valueOf(String value) {
        String[] parts = value.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException();
        }
        List<Entry> entries = new ArrayList<>();
        for (String part : parts) {
            entries.add(createEntry(part));
        }
        return new Margin(entries);
    }

    private static Entry createEntry(String value) {
        if (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
            return new Entry(Float.valueOf(value), true);
        } else {
            return new Entry(Float.valueOf(value), false);
        }
    }

    static class Entry {
        private final float value;
        private final boolean percent;

        Entry(float value, boolean percent) {
            this.value = value;
            this.percent = percent;
        }

        float getValue(float length) {
            if (percent) {
                return length * value / 100.f;
            } else {
                return value;
            }
        }
    };
}
