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

/**
 * @author leadpony
 */
module org.leadpony.pandora {

    requires java.desktop;
    requires java.logging;

    requires info.picocli;
    requires org.apache.pdfbox;
    requires org.apache.fontbox;

    opens org.leadpony.pandora to info.picocli;

    exports org.leadpony.pandora;

    provides java.util.spi.ToolProvider
        with org.leadpony.pandora.PandoraToolProvider;
}