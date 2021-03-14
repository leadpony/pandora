# Pandora

Pandora is a command-line utility for processing PDF files, powered by [Apache PDFBox].

## Requirements

* Java 11 or higher

## Basic Usage

```shell
pandora <command>
```

## Commands
### crop

Assigns a crop box to the PDF document.

```shell
pandora crop [--even] [--flip] [--odd] [--preserve-aspect] [-a=<numeric value or paper size name>] [-o=<output>] [--padding=<padding>] [--pages=<page|range(,page|range)*>] [-m=<top,right,bottom,left>, "bbox", or "text-bbox"]... <input>
```
#### \<input\>
Path to the original PDF document.

#### -a, --aspect=\<numeric value or paper size name\>
Page aspect ratio to be forced. e.g. `0.75`, `3:4`, `a4`

#### --even
Process only even pages.

#### --flip
Flip the margin, page by page.

#### -m, --margin=\<top,right,bottom,left\>, `bbox`, or `text-bbox`

Each margin can be specified in 1/72 inch or % unit.
Special value `bbox` means calculated bounding box of the page. Another value `text-bbox` means bounding box of the texts in the page. (default value: `bbox`)

#### -o, --output=\<output\>
Path to the converted PDF document.

#### --odd
Process only odd pages.

#### --padding=\<padding\>
Padding size in 1/72 inch added to bounding boxes.
(default value: `5`)

#### --pages=\<page|range(,page|range)*\>
Pages or page ranges delimited by comma.
Each range is in the form `<first page>-<last page>`,
both bounds are inclusive and one-indexed.
Page numbers can be prefixed with `b` when counted from the back cover.

#### --preserve-aspect
Preserve the original aspect ratio of pages.

#### Examples
##### **Cropping a PDF with the specified margin**

Cropping pages from the 2nd to the last with the magin of top, right, bottom specified in 1/72 inch. The omitted left margin will be the same as the right margin.

```shell
pandora crop --pages 2-b1 -m 50,40,30 -o Classic_Shell_Scripting.cropped.pdf Classic_Shell_Scripting.pdf
```
##### **Cropping a PDF with automatically calculated bounding boxes**

```shell
pandora crop "Zero Trust Networks.pdf"
```

### help
Displays help information about the specified command.

#### Examples
##### **Displaying help about a command**
```shell
pandora help crop
```

## Copyright Notice
Copyright 2020-2021 the original author or authors. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this product except in compliance with the License.
You may obtain a copy of the License at
<http://www.apache.org/licenses/LICENSE-2.0>

[Apache PDFBox]: https://pdfbox.apache.org
