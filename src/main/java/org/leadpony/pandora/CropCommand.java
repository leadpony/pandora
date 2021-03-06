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
class CropCommand extends AbstractCommand implements CroppingContext {

    @Option(names = { "-m", "--margin" },
            paramLabel = "<top,right,bottom,left>, \"bbox\", \"fast-bbox\", or \"text-bbox\"",
            description = {
                "Each margin can be specified in 1/72 inch or %% unit.",
                "Special value \"bbox\" calculates bounding box for each page.",
                "\"fast-bbox\" produces approximate bounding box for each page.",
                "\"text-bbox\" produces bounding box only from texts in each page.",
                "(default value: \"bbox\")"
            },
            defaultValue = "bbox")
    private List<Margin> margin;

    @Option(names = "--preserve-aspect",
            description = "Preserve the original aspect ratio of pages.")
    private boolean preserveAspect;

    @Option(names = "--flip",
            description = "Flip the margin, page by page.")
    private boolean flip;

    @Option(names = "--padding",
            description = {
                    "Padding size in 1/72 inch added to bounding boxes.",
                    "(default value: 5)"
            },
            defaultValue = "5")
    private int padding = 5;

    @Option(names = {"-a", "--aspect"},
            paramLabel = "<numeric value or paper size name>",
            description = {
                    "Page aspect ratio to be forced.",
                    "e.g. 0.75, \"3:4\", \"a4\""
            },
            converter = AspectConverter.class)
    private Float aspect;

    private CropStrategy strategy;

    @Override
    public int getPadding() {
        return padding;
    }

    @Override
    protected void beginProcessing(PDDocument doc) {
        List<Margin> margins = new ArrayList<>(this.margin);
        if (margins.size() == 1 && this.flip) {
            margins.add(margins.get(0).flip());
        }

        List<CropStrategy> strategies = margins.stream()
                .map(margin -> margin.createStrategy(doc, this))
                .collect(Collectors.toList());

        if (strategies.size() == 1) {
            this.strategy = strategies.get(0);
        } else {
            this.strategy = new FlippingCropStrategy(strategies);
        }
    }

    @Override
    protected void processPage(PDDocument doc, int pageIndex) {
        PDPage page = doc.getPage(pageIndex);
        resetCropBox(page);
        PDRectangle cropBox = strategy.getCropBox(doc, pageIndex);
        updateCropBox(page, cropBox);
    }

    private void resetCropBox(PDPage page) {
        page.setCropBox(page.getMediaBox());
    }

    private void updateCropBox(PDPage page, PDRectangle cropBox) {
        final PDRectangle mediaBox = page.getMediaBox();
        if (preserveAspect) {
            cropBox = adjustBoxAspect(cropBox, mediaBox);
        } else if (aspect != null) {
            cropBox = adjustBoxAspect(cropBox, aspect);
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
            float newWidth = box.getHeight() * aspectRatio;
            float x = box.getLowerLeftX() - 0.5f * (newWidth - box.getWidth());
            return new PDRectangle(x, box.getLowerLeftY(), newWidth, box.getHeight());
        } else if (newAspectRatio > aspectRatio) {
            float newHeight = box.getWidth() / aspectRatio;
            float y = box.getUpperRightY() - newHeight;
            return new PDRectangle(box.getLowerLeftX(), y, box.getWidth(), newHeight);
        }
        return box;
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
