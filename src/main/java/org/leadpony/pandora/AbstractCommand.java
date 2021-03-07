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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.IntPredicate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A skeletal implementation of subcommands.
 *
 * @author leadpony
 */
abstract class AbstractCommand implements Callable<Integer> {

    private static final IntPredicate EVEN_ONLY = page -> (page % 2) == 0;
    private static final IntPredicate ODD_ONLY = page -> (page % 2) != 0;

    @Parameters(index = "0", description = "Path to the original PDF document.")
    private Path input;

    @Option(names = { "-o", "--output" }, description = "Path to the converted PDF document.")
    private Path output;

    @Option(names = "--pages",
            paramLabel = "<page|range(,page|range)*>",
            description = {
                "Pages or page ranges delimited by comma.",
                "Each range is in the form <first page>-<last page>, ",
                "both bounds are inclusive and one-indexed.",
                "Page numbers can be prefixed with 'b'",
                "when counted from the back cover."
            }
    )
    private Pages pages = Pages.all();

    @Option(names = "--even",
            description = "Process only even pages.")
    private boolean even = false;

    @Option(names = "--odd",
            description = "Process only odd pages.")
    private boolean odd = false;

    /**
     * Executes this command.
     *
     * @return the exit code of this command.
     * @exception Exception if this command fails.
     */
    @Override
    public Integer call() throws Exception {
        try (PDDocument doc = load(input)) {
            processDoc(doc);
            save(doc);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return 1;
        }
        return 0;
    }

    /**
     * Processes a PDF document.
     *
     * @param doc the PDF document to process, never be {@code null}.
     */
    protected void processDoc(PDDocument doc) {
        doc.setAllSecurityToBeRemoved(true);
        beginProcessing(doc);
        final int totalPages = doc.getNumberOfPages();
        IntPredicate predicate = getPagePredicate(totalPages);
        for (int i = 0; i < totalPages; i++) {
            int pageNo = i + 1;
            if (predicate.test(pageNo)) {
                processPage(doc.getPage(i), pageNo);
            }
        }
    }

    protected abstract void beginProcessing(PDDocument doc);

    /**
     * Processes a page of the PDF document.
     *
     * @param page the page to process, never be {@code null}.
     * @param pageNo the page number starting from 1.
     */
    protected abstract void processPage(PDPage page, int pageNo);

    protected String getDefaultOutputSuffix() {
        return "converted";
    }

    private static PDDocument load(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            return PDDocument.load(input);
        }
    }

    private void save(PDDocument doc) throws IOException {
        try (OutputStream output = Files.newOutputStream(getOutput())) {
            doc.save(output);
        }
    }

    private IntPredicate getPagePredicate(int totalPages) {
        IntPredicate p = pages.testing(totalPages);
        if (even) {
            p = p.and(EVEN_ONLY);
        }
        if (odd) {
            p = p.and(ODD_ONLY);
        }
        return p;
    }

    private Path getOutput() {
        if (this.output != null) {
            return this.output;
        }
        StringBuilder builder = new StringBuilder();
        String input = this.input.toString();
        int index = input.lastIndexOf('.');
        if (index < 0) {
            builder.append(input);
            builder.append('-');
            builder.append(getDefaultOutputSuffix());
        } else {
            builder.append(input, 0, index);
            builder.append('-');
            builder.append(getDefaultOutputSuffix());
            builder.append(input, index, input.length());
        }
        return Path.of(builder.toString());
    }
}
