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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

public class AspectTest {

    public enum TestCase {
        SINGLE_NUMBER("0.75", 0.75f),
        RATIO("3:4", 0.75f),
        A4("a4", PaperSize.A4.aspectRatio()),
        A5("A5", PaperSize.A5.aspectRatio());

        final String value;
        final float expected;

        TestCase(String value, float expected) {
            this.value = value;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(TestCase.class)
    public void test(TestCase test) {
        Aspect aspect = Aspect.valueOf(test.value);
        assertThat(aspect.getValue()).isEqualTo(test.expected);
    }
}
