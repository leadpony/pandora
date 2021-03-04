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
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author leadpony
 */
@Command(name = "crop", description = "Assigns crop box to the PDF")
class CropCommand extends AbstractCommand {

    @Option(names = { "-m", "--margin" }, paramLabel = "<top,right,bottom,left> or \"bbox\"", description = {
            "margin each specified by 1/72 inch or %%", "\"bbox\" means bounding box of the page",
            "\"text-bbox\" means bounding box of the texts in the page", }, required = true)
    private List<Margin> margin;

    @Option(names = "--preserve-aspect", description = "preserve the aspect ratio of the page")
    private boolean preserveAspect;

    @Option(names = "--flip", description = "flip the margin, page by page")
    private boolean flip;

    @Option(names = "--padding", description = "padding in 1/72, default is 0", defaultValue = "0")
    private int padding = 0;

    private CropStrategy strategy;

    @Override
    protected void beginProcessing(PDDocument doc) {
        List<Margin> margins = new ArrayList<>(this.margin);
        if (margins.size() == 1 && this.flip) {
            margins.add(margins.get(0).flip());
        }

        List<CropStrategy> strategies = margins.stream()
                .map(margin -> margin.createStrategy(doc))
                .collect(Collectors.toList());

        if (strategies.size() == 1) {
            this.strategy = strategies.get(0);
        } else {
            this.strategy = new FlippingCropStrategy(strategies);
        }
    }

    @Override
    protected void processPage(PDPage page, int pageNo) {
        PDRectangle mediaBox = page.getMediaBox();
        PDRectangle cropBox = strategy.getCropBox(page, pageNo);
        pad(cropBox);
        if (preserveAspect) {
            cropBox = adjustBox(cropBox, mediaBox);
        }
        clip(cropBox, mediaBox);
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

    private void pad(PDRectangle rect) {
        final int padding = this.padding;
        if (padding > 0) {
            rect.setLowerLeftX(rect.getLowerLeftX() - padding);
            rect.setUpperRightX(rect.getUpperRightX() + padding);
            rect.setLowerLeftY(rect.getLowerLeftY() - padding);
            rect.setUpperRightY(rect.getUpperRightY() + padding);
        }
    }

    private static void clip(PDRectangle rect, PDRectangle mediaBox) {
        float minX = rect.getLowerLeftX();
        float maxX = rect.getUpperRightX();
        float minY = rect.getLowerLeftY();
        float maxY = rect.getUpperRightY();

        if (minX < mediaBox.getLowerLeftX()) {
            rect.setLowerLeftX(mediaBox.getLowerLeftX());
        }
        if (maxX > mediaBox.getUpperRightX()) {
            rect.setUpperRightX(mediaBox.getUpperRightX());
        }
        if (minY < mediaBox.getLowerLeftY()) {
            rect.setLowerLeftY(mediaBox.getLowerLeftY());
        }
        if (maxY > mediaBox.getUpperRightY()) {
            rect.setUpperRightY(mediaBox.getUpperRightY());
        }
    }
}
