# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.4.0] - 2026-05-13

### Added

- `GET /api/cta/reports/smoking/aggregates/{line}/month/{yearMonth}/days` — returns daily smoking report counts for a given month; only days with at least one report are included (days with no reports are omitted), replacing up to 31 individual day aggregate calls with a single request.

## [3.3.1] - 2026-05-11

### Fixed

- Removed `@EnableWebMvc` from `WebConfig`, which was suppressing Spring Boot's MVC auto-configuration and preventing `StringToYearWeekConverter` from being registered — causing the week aggregate endpoint to return 500.
- Refactored `WebConfig` to register CORS configuration via a `@Bean` method returning `WebMvcConfigurer` rather than implementing the interface directly on the class.

## [3.3.0] - 2026-05-11

### Added

- Aggregate example to README.

### Changed

- `DynamoDbTableProperties` fields now validated against the DynamoDB table name character set at startup to catch misconfigured deployments early.
- Bump `software.amazon.awssdk:bom` from `2.42.41` to `2.44.4`

## [3.2.0] - 2026-05-11

### Added

- Smoking report aggregate API with endpoints for day, week, month, year, and all-time report counts per train line.
- `YearWeek` type for ISO week-based lookups (e.g. `2026-W13`).
- `DynamoDbTableProperties` configuration properties record for table name binding.
- Unit tests for all controllers, repositories, services, and model classes.

### Changed

- Refactored `SmokingReportService` to return DTOs directly; HTTP response construction moved to `SmokingReportController`.
- Migrated `CorsProperties` and `OpenAPIProperties` from Lombok `@Data` classes to records with JSR-303 validation.
- Added `@Validated` constraints to all `@ConfigurationProperties` records.
- Fixed `SmokingReportController` location header to use `UriComponentsBuilder.fromUriString` for correct absolute URI construction.

## [3.1.0] - 2026-04-06

### Changed

- `carNumber` now requires exactly 4 digits instead of 1 to 10 digits.
- `runNumber` now requires exactly 3 digits instead of 1 to 10 digits.
- Bump `org.springframework.boot:spring-boot-starter-parent` from `4.0.3` to `4.0.5`
- Bump `org.springdoc:springdoc-openapi-starter-webmvc-ui` from `3.0.1` to `3.0.2`
- Bump `com.amazonaws.secretsmanager:aws-secretsmanager-caching-java` from `2.1.1` to `2.2.0`
- Bump `com.rollbar:rollbar-java` from `2.1.0` to `2.2.0`
- Bump `software.amazon.awssdk:bom` from `2.42.4` to `2.42.23`

## [3.0.3] - 2026-03-03

### Changed

- Cleaned up `GlobalExceptionHandler` by extending `ResponseEntityExceptionHandler` to leverage Spring's built-in
exception handling capabilities and reduce boilerplate code.
- Bump `software.amazon.awssdk:bom` from `2.41.34` to `2.42.4`

## [3.0.2] - 2026-02-27

### Changed

- API errors now use the `ProblemDetail` format with appropriate HTTP status codes for better client error handling and
debugging.
- Updated OpenAPI spec to reflect the new error response format and status codes, along with additional descriptions
for each endpoint and field to improve documentation clarity.
- Reports now expire after 30 minutes

### Added

- `expiresAt` field is now correctly shown in the README data model
- Added support for Rollbar logging when exceptions occur in the API


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

[Unreleased]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.4.0...HEAD
[3.4.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.3.1...v3.4.0
[3.3.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.3.0...v3.3.1
[3.3.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.2.0...v3.3.0
[3.2.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.1.0...v3.2.0
[3.1.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.3...v3.1.0
[3.0.3]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.2...v3.0.3
[3.0.2]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.1...v3.0.2
[3.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v3.0.0...v3.0.1
[3.0.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v2.0.0...v3.0.0
[2.0.0]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.1...v2.0.0
[1.0.1]: https://github.com/lbkulinski/cta-smokers-api/compare/v1.0.0...v1.0.1
