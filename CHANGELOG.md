# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 0.4.0 - 2021-03-14
### Added
- `--aspect` option to specify the aspect ratio of the pages.
- `--padding` option to add padding to the calculated bounding boxes.

### Changed
- Page ranges now use `<first>-<last>` form.

### Fixed
- The calculation of bounding box now ignores the areas outside of the page.

## 0.3.0 - 2020-10-17
### Added
- `text-bbox` is now available as a `--margin` option argument, which will calculate the bounding box containing all texts in the page.
- `--flip` option which will flip the left and the right margin, page by page.
- Specifying multiple margins is now supported. When using two margins, the first margin will be applied to the odd pages and the second margin will be applied to the even pages.

### Changed
- Now `setAllSecurityToBeRemove()` method on `PDDocument` will be always called to remove security.

### Fixed
- A bug in parsing page range with the last page omitted.

## 0.2.0 - 2020-04-04
- First release.
