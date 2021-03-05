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

    @Option(names = { "-m", "--margin" }, paramLabel = "<top,right,bottom,left>, \"bbox\", or \"text-bbox\"", description = {
            "margin each specified by 1/72 inch or %%", "\"bbox\" means bounding box of the page",
            "\"text-bbox\" means bounding box of the texts in the page", }, required = true)
    private List<Margin> margin;

    @Option(names = "--preserve-aspect", description = "preserve the original aspect ratio of the page")
    private boolean preserveAspect;

    @Option(names = "--flip", description = "flip the margin, page by page")
    private boolean flip;

    @Option(names = "--padding", description = "padding size in 1/72 inch, default is 0", defaultValue = "0")
    private int padding = 0;

    @Option(names = "--aspect", paramLabel = "<numeric value or paper size name>", description = "page aspect ratio")
    private Aspect aspect;

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
        addPaddingToBox(cropBox);
        if (preserveAspect) {
            cropBox = adjustBoxAspect(cropBox, mediaBox);
        } else if (aspect != null) {
            cropBox = adjustBoxAspect(cropBox, aspect.getValue());
        }
        clipBox(cropBox, mediaBox);
        page.setCropBox(cropBox);
    }

    private static PDRectangle adjustBoxAspect(PDRectangle box, PDRectangle page) {
        float aspectRatio = page.getWidth() / page.getHeight();
        return adjustBoxAspect(box, aspectRatio);
    }

    private static PDRectangle adjustBoxAspect(PDRectangle box, float aspectRatio) {
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

    private void addPaddingToBox(PDRectangle box) {
        final int padding = this.padding;
        if (padding > 0) {
            box.setLowerLeftX(box.getLowerLeftX() - padding);
            box.setUpperRightX(box.getUpperRightX() + padding);
            box.setLowerLeftY(box.getLowerLeftY() - padding);
            box.setUpperRightY(box.getUpperRightY() + padding);
        }
    }

    private static void clipBox(PDRectangle box, PDRectangle mediaBox) {
        float minX = box.getLowerLeftX();
        float maxX = box.getUpperRightX();
        float minY = box.getLowerLeftY();
        float maxY = box.getUpperRightY();

        if (minX < mediaBox.getLowerLeftX()) {
            box.setLowerLeftX(mediaBox.getLowerLeftX());
        }
        if (maxX > mediaBox.getUpperRightX()) {
            box.setUpperRightX(mediaBox.getUpperRightX());
        }
        if (minY < mediaBox.getLowerLeftY()) {
            box.setLowerLeftY(mediaBox.getLowerLeftY());
        }
        if (maxY > mediaBox.getUpperRightY()) {
            box.setUpperRightY(mediaBox.getUpperRightY());
        }
    }
}
