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

import static org.assertj.core.api.Assertions.*;
import java.util.function.IntPredicate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * @author leadpony
 */
public class PagesTest {

    public enum PagesCase {
        SINGLE_PAGE("42", 42, 100, true),
        NOT_SINGLE_PAGE("42", 27, 100, false),
        LAST_PAGE("-1", 100, 100, true),
        LAST_BUT_ONE("-2", 99, 100, true),

        MULTIPLE_PAGES("1,2,4", 2, 100, true),
        NOT_IN_MULTIPLE_PAGES("1,2,4", 3, 100, false),

        START_OF_RANGE("3:8", 3, 100, true),
        MIDDLE_OF_RANGE("3:8", 5, 100, true),
        END_OF_RANGE("3:8", 8, 100, true),
        BEFORE_RANGE("3:8", 2, 100, false),
        AFTER_RANGE("3:8", 9, 100, false),
        END_OF_RANGE_WITH_NEGATIVE("3,-1", 100, 100, true),
        BEFORE_RANGE_WITH_NEGATIVE("3,-1", 2, 100, false),

        MULTIPLE_RANGES("11:20,31:40", 31, 100, true),
        NOT_IN_MULTIPLE_RANGES("11:20,31:40", 30, 100, false);

        final String value;
        final int pageIndex;
        final int totalPages;
        final boolean result;

        PagesCase(String value, int pageIndex, int totalPages, boolean result) {
            this.value = value;
            this.pageIndex = pageIndex;
            this.totalPages = totalPages;
            this.result = result;
        }
    };

    @ParameterizedTest
    @EnumSource(PagesCase.class)
    public void test(PagesCase test) {
        Pages pages = Pages.valueOf(test.value);
        IntPredicate predicate = pages.testing(test.totalPages);
        boolean actual = predicate.test(test.pageIndex);
        assertThat(actual).isEqualTo(test.result);
    }
}
