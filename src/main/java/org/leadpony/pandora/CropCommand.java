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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author leadpony
 */
@Command(name = "crop", description = "Assigns crop box to the PDF")
class CropCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "input PDF")
    private Path input;

    @Option(names = {"-o", "--output"},
            description = "output PDF",
            required = true)
    private Path output;

    @Option(names = {"-m", "--margin"},
            paramLabel = "<top,right,bottom,left>",
            description = "Specify margin in 1/72 inch or %%",
            required = true
            )
    private Margin margin;

    CropCommand() {
    }

    @Override
    public Integer call() throws Exception {
        PDDocument doc = load(input);
        cropAllPages(doc);
        save(doc, output);
        return 0;
    }

    private void cropAllPages(PDDocument doc) {
        for (PDPage page : doc.getPages()) {
            cropPage(page);
        }
    }

    private void cropPage(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        PDRectangle cropBox = margin.getRectangle(mediaBox);
        page.setCropBox(cropBox);
    }

    private static PDDocument load(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return PDDocument.load(input);
        }
    }

    private static void save(PDDocument doc, Path path) throws IOException {
        try (OutputStream output = Files.newOutputStream(path)) {
            doc.save(output);
        }
    }
}
