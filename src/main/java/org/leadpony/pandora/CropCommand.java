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
import java.util.function.IntPredicate;

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

    @Parameters(index = "0",
            description = "path to the original PDF")
    private Path input;

    @Option(names = {"-o", "--output"},
            description = "path to the cropped PDF",
            required = true)
    private Path output;

    @Option(names = {"-m", "--margin"},
            paramLabel = "<top,right,bottom,left>",
            description = "margin in 1/72 inch or %%",
            required = true
            )
    private Margin margin;

    @Option(names = "--pages",
            paramLabel = "<page|range(,page|range)*>",
            description = "pages or page ranges")
    private Pages pages = Pages.all();

    @Option(names = "--preserve-aspect",
            description = "preserve aspect ratio")
    private boolean preserveAspect;

    CropCommand() {
    }

    @Override
    public Integer call() throws Exception {
        try (PDDocument doc = load(input)) {
            cropAllPages(doc);
            save(doc, output);
        }
        return 0;
    }

    private void cropAllPages(PDDocument doc) {
        final int totalPages = doc.getNumberOfPages();
        IntPredicate predicate = pages.testing(totalPages);
        for (int i = 0; i < totalPages; i++) {
            if (predicate.test(i + 1)) {
                cropPage(doc.getPage(i));
            }
        }
    }

    private void cropPage(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        PDRectangle cropBox = margin.getRectangle(mediaBox);
        if (preserveAspect) {
            cropBox = adjustBox(cropBox, mediaBox);
        }
        page.setCropBox(cropBox);
    }

    private static PDRectangle adjustBox(PDRectangle box, PDRectangle page) {
        float aspectRatio = page.getWidth() / page.getHeight();
        return adjustBox(box, aspectRatio);
    }

    private static PDRectangle adjustBox(PDRectangle box, float aspectRatio) {
        final float newAspectRatio = box.getWidth() / box.getHeight();
        if (newAspectRatio < aspectRatio) {
            float width = box.getHeight() * aspectRatio;
            float x = box.getLowerLeftX() - 0.5f * (width - box.getWidth());
            return new PDRectangle(x, box.getLowerLeftY(), width, box.getHeight());
        } else if (newAspectRatio > aspectRatio) {
            float height = box.getWidth() / aspectRatio;
            float y = box.getLowerLeftY() - 0.5f * (height - box.getHeight());
            return new PDRectangle(box.getLowerLeftX(), y, box.getWidth(), height);
        }
        return box;
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
