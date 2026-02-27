# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.0.1] - 2026-02-26

### Added

- Added https://cta-smokers-front-end.lagers-rancor-0o.workers.dev as an allowed origin in the CORS configuration to
allow requests from the deployed front-end application.

## [3.0.0] - 2026-02-26

### Changed

- Updated report IDs to use an underscore (`_`) instead of a hash symbol (`#`) to separate the timestamp and UUID to
avoid issues with URL encoding when retrieving specific reports by ID.
- Updated `line` field to require a valid CTA line name instead of freeform text to allow for better data consistency.
- Updated `destination` and `nextStop` fields to require numeric stations IDs instead of freeform text to allow for
better data consistency. The new fields are named `destinationId` and `nextStation` respectively.
  - The station IDs correspond to the official CTA station codes, which can be found on the
[Chicago Data Portal](https://data.cityofchicago.org/Transportation/CTA-System-Information-List-of-L-Stops/8pix-ypme/about_data).
- Updated `carNumber` and `runNumber` fields to require numeric values instead of freeform text to allow for better data
consistency.

### Added

- CORS configuration to allow requests from `https://ctasmokers.com` and `https://www.ctasmokers.com`.

## [2.0.0] - 2026-02-22

### Changed

- Changed API path from `/api/cta/smoking/reports` to `/api/cta/reports/smoking`

### Fixed

- OpenAPI spec now correctly specifies that the POST endpoint returns a 201 status code on success instead of 200.

## [1.0.1] - 2026-02-22

### Changed

- Allowed `/actuator/health` endpoint to bypass the request filter

## 1.0.0 - 2026-02-22

### Added

- Initial release of API.

[Unreleased]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.1...HEAD
[3.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v2.0.0...v3.0.0
[2.0.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.1...v2.0.0
[1.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.0...v1.0.1
