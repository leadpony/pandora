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

Assigns crop box to the PDF.

```shell
pandora crop [--even] [--odd] [--preserve-aspect] -m=<top,right,bottom,left> or "bbox" -o=<output> [--pages=<page|range(,page|range)*>] <input>
```
#### --even
This option causes only _even_ pages to be copped.

#### -m, --margin=\<top,right,bottom,left\> or "bbox"
Each margin value can be specified in 1/72 inch or in percentage followed by %.
Special value `bbox` means the bounding box of the page, which is automatically calculated.

#### -o, --output=\<output\>
This option specifies the path where the modified PDF will be outputted.

#### --odd
This option causes only _odd_ pages to be copped.

#### --pages=<page|range(,page|range)*>
This option limits the pages to be cropped using their page numbers. The page number starts from 1 and negative values start from the final page of the document. The page ranges can be presented in the form of `<start page>:<end page>` where both bounds are inclusive.

#### --preserve-aspect
This option preserves the original aspect ratio of each page.

#### Examples
##### Cropping with fixed margin

Cropping pages from the 2nd to the last with the magin of top, right, bottom specified in 1/72 inch. The omitted left margin will be the same as the right margin.
```shell
pandora crop --pages 2:-1 -m 50,40,30 -o Classic_Shell_Scripting.cropped.pdf Classic_Shell_Scripting.pdf
```

### help

Displays help information about the specified command.

#### Examples
##### Displaying help about the command
```shell
pandora help crop
```

## Copyright Notice
Copyright 2020 the original author or authors. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this product except in compliance with the License.
You may obtain a copy of the License at
<http://www.apache.org/licenses/LICENSE-2.0>

[Apache PDFBox]: https://pdfbox.apache.org
