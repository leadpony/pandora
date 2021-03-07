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
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

/**
 * @author leadpony
 */
class Pages {

    private static final Pages ALL = new Pages();

    private final List<PageRange> ranges;

    private Pages() {
        this.ranges = Collections.emptyList();
    }

    private Pages(List<PageRange> ranges) {
        this.ranges = ranges;
    }

    IntPredicate testing(int totalPages) {
        if (this.ranges.isEmpty()) {
            return pageIndex -> true;
        }

        List<PageRange> normalized = this.ranges.stream()
            .map(range -> range.normalize(totalPages))
            .collect(Collectors.toList());

        return pageIndex -> normalized.stream()
                .anyMatch(range -> range.test(pageIndex));
    }

    static Pages all() {
        return ALL;
    }

    static Pages valueOf(String value) {
        String[] tokens = value.trim().split("\\s*,\\s*");
        if (tokens.length < 1) {
            throw new IllegalArgumentException();
        }

        List<PageRange> ranges = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                ranges.add(parseRange(token));
            }
        }
        return new Pages(ranges);
    }

    /**
     * Parses a range into a {@code PageRange} instance.
     *
     * @param value the string to be parsed, which is already trimmed.
     * @return a newly created instance of {@code PageRange}.
     */
    private static PageRange parseRange(String value) {
        String[] tokens = value.trim().split("\\s*-\\s*", -1);
        if (tokens.length > 2) {
            throw new IllegalArgumentException();
        }
        if (tokens.length == 1) {
            return new Single(parsePageNumber(value, 1));
        } else {
            int first = parsePageNumber(tokens[0], 1);
            int last = parsePageNumber(tokens[1], -1);
            return new Bounded(first, last);
        }
    }

    private static int parsePageNumber(String value, int defaultValue) {
        if (value.isEmpty()) {
            return defaultValue;
        }
        int sign =  1;
        if (value.startsWith("b")) {
            sign = -1;
            value = value.substring(1);
        }
        return sign * Integer.valueOf(value);
    }

    private interface PageRange extends IntPredicate {

        PageRange normalize(int totalPages);
    }

    private static class Single implements PageRange {

        private final int index;

        Single(int index) {
            this.index = index;
        }

        @Override
        public boolean test(int index) {
            return index == this.index;
        }

        @Override
        public PageRange normalize(int totalPages) {
            if (index >= 0) {
                return this;
            } else {
                return new Single(totalPages + index + 1);
            }
        }
    }

    private static class Bounded implements PageRange {

        private final int first;
        private final int last;

        Bounded(int first, int last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public boolean test(int index) {
            return this.first <= index && index <= this.last;
        }

        @Override
        public PageRange normalize(int totalPages) {
            if (first >= 0 && last >= 0) {
                return this;
            }

            int first = this.first >= 0
                    ? this.first : totalPages + this.first + 1;
            int last = this.last >= 0
                    ? this.last : totalPages + this.last + 1;

            return new Bounded(first, last);
        }
    }
}
