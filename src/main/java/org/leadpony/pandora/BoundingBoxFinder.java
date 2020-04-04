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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.fontbox.util.BoundingBox;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType3CharProc;
import org.apache.pdfbox.pdmodel.font.PDType3Font;
import org.apache.pdfbox.pdmodel.font.PDVectorFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

/**
 * A {@link PDFGraphicsStreamEngine} for detecting a bounding box of the given page.
 *
 * <a href="https://stackoverflow.com/questions/52821421/how-do-determine-location-of-actual-pdf-content-with-pdfbox">
 * How do determine location of actual PDF content with PDFBox?
 * </a>
 * <p>
 * This stream engine determines the bounding box of the static content
 * of a page. Beware, it is not very sophisticated; in particular it
 * does not ignore invisible content like a white background rectangle,
 * text drawn in rendering mode "invisible", arbitrary content covered
 * by a white filled path, white parts of bitmap images, ... Furthermore,
 * it ignores clip paths.
 * </p>
 *
 * @author mklink
 * @author leadpony
 */
class BoundingBoxFinder extends PDFGraphicsStreamEngine {

    private Rectangle2D boundingBox;
    private Rectangle2D pathRect;

    BoundingBoxFinder(PDPage page) {
        super(page);
    }

    /**
     * Returns the bounding box found.
     *
     * @return the bounding box found, can be {@code null}.
     */
    Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
        addToPath(p0, p1, p2, p3);
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {
        Matrix m = getGraphicsState().getCurrentTransformationMatrix();
        add(m.transformPoint(0.f, 0.f));
        add(m.transformPoint(0.f, 1.f));
        add(m.transformPoint(1.f, 0.f));
        add(m.transformPoint(1.f, 1.f));
    }

    @Override
    public void clip(int windingRule) throws IOException {
        // Do nothing
    }

    @Override
    public void moveTo(float x, float y) throws IOException {
        addToPath(x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException {
        addToPath(x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
        addToPath(x1, y1);
        addToPath(x2, y2);
        addToPath(x3, y3);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return null;
    }

    @Override
    public void closePath() throws IOException {
        // Do nothing
    }

    @Override
    public void endPath() throws IOException {
        pathRect = null;
    }

    @Override
    public void strokePath() throws IOException {
        finishPath();
    }

    @Override
    public void fillPath(int windingRule) throws IOException {
        finishPath();
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException {
        finishPath();
    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException {
        // Do nothing
    }

    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode, Vector displacement)
            throws IOException {
        super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);
        Shape shape = calculateGlyphBounds(textRenderingMatrix, font, code);
        if (shape != null) {
            add(shape.getBounds2D());
        }
    }

    /* helper */

    /**
     * Copy of <code>org.apache.pdfbox.examples.util.DrawPrintTextLocations.calculateGlyphBounds(Matrix, PDFont, int)</code>.
     * @throws IOException
     */
    private Shape calculateGlyphBounds(Matrix textRenderingMatrix, PDFont font, int code)
            throws IOException {
        AffineTransform transform = textRenderingMatrix.createAffineTransform();
        transform.concatenate(font.getFontMatrix().createAffineTransform());

        GeneralPath path = null;
        if (font instanceof PDType3Font) {
            path = calculateGlyphPath((PDType3Font) font, code);
        } else if (font instanceof PDVectorFont) {
            path = calculateGlyphPath((PDVectorFont) font, code);
            if (font instanceof PDTrueTypeFont) {
                PDTrueTypeFont ttFont = (PDTrueTypeFont) font;
                int unitsPerEm = ttFont.getTrueTypeFont().getHeader().getUnitsPerEm();
                transform.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
            }
            if (font instanceof PDType0Font) {
                PDType0Font t0font = (PDType0Font) font;
                if (t0font.getDescendantFont() instanceof PDCIDFontType2) {
                    PDCIDFontType2 dFont = (PDCIDFontType2) t0font.getDescendantFont();
                    int unitsPerEm = dFont.getTrueTypeFont().getHeader().getUnitsPerEm();
                    transform.scale(1000d / unitsPerEm, 1000d / unitsPerEm);
                }
            }
        } else if (font instanceof PDSimpleFont) {
            path = calculateGlyphPath((PDSimpleFont) font, code);
        } else {
            // shouldn't happen, please open issue in JIRA
            throw new IllegalStateException("Unknown font class: " + font.getClass());
        }

        if (path == null) {
            return null;
        }
        return transform.createTransformedShape(path.getBounds2D());
    }

    private GeneralPath calculateGlyphPath(PDType3Font font, int code) throws IOException {
        PDType3CharProc charProc = font.getCharProc(code);
        if (charProc == null) {
            return null;
        }

        PDRectangle glyphBBox = charProc.getGlyphBBox();
        if (glyphBBox == null) {
            return null;
        }

        BoundingBox fontBBox = font.getBoundingBox();
        glyphBBox.setLowerLeftX(Math.max(fontBBox.getLowerLeftX(), glyphBBox.getLowerLeftX()));
        glyphBBox.setLowerLeftY(Math.max(fontBBox.getLowerLeftY(), glyphBBox.getLowerLeftY()));
        glyphBBox.setUpperRightX(Math.min(fontBBox.getUpperRightX(), glyphBBox.getUpperRightX()));
        glyphBBox.setUpperRightY(Math.min(fontBBox.getUpperRightY(), glyphBBox.getUpperRightY()));

        return glyphBBox.toGeneralPath();
    }

    private GeneralPath calculateGlyphPath(PDVectorFont font, int code) throws IOException {
        return font.getPath(code);
    }

    private GeneralPath calculateGlyphPath(PDSimpleFont font, int code) throws IOException {
        // these two lines do not always work, e.g. for the TT fonts in file 032431.pdf
        // which is why PDVectorFont is tried first.
        String name = font.getEncoding().getName(code);
        return font.getPath(name);
    }

    private void addToPath(Point2D... points) {
         for (Point2D point : points) {
            addToPath(point.getX(), point.getY());
        }
    }

    private void addToPath(double x, double y) {
        if (pathRect == null) {
            pathRect = new Rectangle2D.Double(x, y, 0, 0);
        } else {
            pathRect.add(x, y);
        }
    }

    private void finishPath() {
        if (pathRect != null) {
            add(pathRect);
            pathRect = null;
        }
    }

    private void add(Rectangle2D rect) {
        if (boundingBox == null) {
            boundingBox = new Rectangle2D.Double();
            boundingBox.setRect(rect);
        } else {
            boundingBox.add(rect);
        }
    }

    private void add(Point2D point) {
        add(point.getX(), point.getY());
    }

    private void add(double x, double y) {
        if (boundingBox == null) {
            boundingBox = new Rectangle2D.Double(x, y, 0, 0);
        } else {
            boundingBox.add(x,  y);
        }
    }
}
