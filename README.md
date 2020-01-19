# Pandora

## Requirements

* Java 9 or higher

## Usage

```
Usage: pandora [-hV] COMMAND
Pandora
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help  Displays help information about the specified command
  crop  Assigns crop box to the PDF
```

## Commands
#### crop

```
Usage: pandora crop -m=<top,right,bottom,left> -o=<output> <input>
Assigns crop box to the PDF
      <input>             input PDF
  -m, --margin=<top,right,bottom,left>
                          Specify margin in 1/72 inch or %
  -o, --output=<output>   output PDF
```

## Copyright Notice
Copyright 2020 the original author or authors. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this product except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
