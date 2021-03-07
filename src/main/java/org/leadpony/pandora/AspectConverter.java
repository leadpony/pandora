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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.ITypeConverter;

/**
 * A converter for {@code --aspect} option.
 *
 * @author leadpony
 */
public class AspectConverter implements ITypeConverter<Float> {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)");
    private static final Pattern RATIO_PATTERN = Pattern.compile("(\\d+):(\\d+)");

    @Override
    public Float convert(String value) {
        Matcher m = NUMBER_PATTERN.matcher(value);
        if (m.matches()) {
            return Float.valueOf(value);
        }

        m = RATIO_PATTERN.matcher(value);
        if (m.matches()) {
            float w = Float.valueOf(m.group(1));
            float h = Float.valueOf(m.group(2));
            return w / h;
        }

        PaperSize paperSize = PaperSize.valueOf(value.toUpperCase());
        return paperSize.aspectRatio();
    }
}
