# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.1] - 2026-02-24

### Added

- CORS configuration to allow requests from `https://ctasmokers.com`, `https://www.ctasmokers.com`, and local
development origins.

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

[Unreleased]: https://github.com/lbkulinski/cta-smokers-api/compare/v2.0.1...HEAD
[2.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.1...v2.0.0
[1.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.0...v1.0.1
