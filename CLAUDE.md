# CLAUDE.md

## Project Overview

Spring Boot REST API for crowdsourced smoking reports on Chicago CTA trains. Backed by DynamoDB on AWS, deployed via Docker to ECR.

## Architecture

- **Controllers** — HTTP concerns only (build `ResponseEntity`, `Location` headers, path/query binding, map models to DTOs for the response body)
- **Services** — business logic, return models directly, throw typed exceptions on not-found or invalid input
- **Repositories** — DynamoDB access via the Enhanced Client (`DynamoDbEnhancedClient`)
- **`@ConfigurationProperties` records** — one record per config namespace, validated with JSR-303

Package layout: feature (`smoking.report`, `smoking.aggregate`) then layer (`controller`, `service`, `repository`, `dto`, `model`, `exception`, plus `config` and `cursor` where needed). Shared types live in `smoking.common`; cross-cutting config and `GlobalExceptionHandler` live in `common`.

## Annotation Ordering

Always apply annotations in this order: **Spring → OpenAPI/other → JSpecify**

```java
@RestController
@RequestMapping("/api/...")
@Tag(name = "...", description = "...")
@NullMarked
public final class MyController { }
```

## Null Safety

- All new classes, records, interfaces, and enums in `src/main` must have `@NullMarked` (from `org.jspecify.annotations`).
- Test classes, exception classes, and `@Configuration` classes (e.g. `WebConfig`) do not use `@NullMarked`.
- Use `@Nullable` on parameters or return types that can be null.

## Configuration Properties

All `@ConfigurationProperties` classes must be records annotated with `@Validated`. Use JSR-303 constraints directly on fields:

```java
@ConfigurationProperties(prefix = "app.example")
@Validated
@NullMarked
public record ExampleProperties(
    @NotBlank String name,
    @Min(1) @Max(100) int pageSize
) {}
```

Register each record in `@EnableConfigurationProperties` on `Application.java`.

## DynamoDB Conventions

Table: `cta-smoking-reports`
- PK: `carNumberLine` (e.g. `2435#RED`, built with `SmokingReportSchema.carNumberLine`), no sort key
- GSI `gsiPk-expiresAt-index`: PK `gsiPk` (constant `SmokingReportSchema.GSI_PK_VALUE`), SK `expiresAt` (epoch seconds), projection ALL
- Attribute names, the index name, and key formats live in `SmokingReportSchema`; use them instead of string literals (the repository's condition expression is the one exception)
- Submit is a single conditional `putItem` (`attribute_not_exists` or expired), so a car and line has at most one item
- Expired items can linger until TTL deletes them, so active reads use an `expiresAt` condition at query time, never TTL
- Paging cursors are opaque base64url tokens: `SmokingReportCursorCodec` encodes a DynamoDB `lastEvaluatedKey` and decodes a token to a `ReportPageCursor`, and `ReportPageCursor.toKey()` builds the `exclusiveStartKey`
- Cursors are not signed, so a well-formed cursor built by a client is accepted; it only picks a start position in already public data
- Malformed cursors, including one whose `gsiPk` is not `GSI_PK_VALUE`, throw `InvalidCursorException` (400). A cursor whose `expiresAt` is at or before now returns an empty page without querying, because DynamoDB rejects a start key outside the query's `expiresAt` condition
- `limit` is 1 to `SmokingReportLimits.MAX_LIMIT`; the controller validates it and the repository re-checks it

Table: `cta-smoking-report-aggregates`
- PK: `LINE#<TrainLine>` (e.g. `LINE#RED`)
- SK patterns:
  - `DAY#2026-05-10`
  - `WEEK#2026-W13`
  - `MONTH#2026-03`
  - `YEAR#2026`
  - `ALL_TIME`

## Stations

Table: `cta-train-stations` (configured as `app.aws.dynamodb.tables.stations`)
- PK: `mapId`, SK: `stationId`
- `StationRepository.existsByMapId` queries the `mapId` partition with a limit of 1 and checks the returned items, not the pages, since an empty result still comes back as a page
- Results are cached in Caffeine (`stationMapIds`, `expireAfterWrite` 24 hours). Only `true` is cached (`unless = "!#result"`), so a station added later is found on the next request
- Unknown destination or next-station IDs throw `InvalidStationIdException` (400)
- Destination `0` is the Loop: it is accepted without a lookup on Brown, Orange, Pink, and Purple, and rejected on other lines. `nextStationId` is always looked up

## Error Handling

- `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`, so errors are `application/problem+json` (`ProblemDetail`)
- Do not override Spring's inherited handlers (for example for `@Min`/`@Max` on request params); map only project exceptions
- Custom handlers use the exception message as `detail`, which includes the offending value (for example the cursor or station ID)
- Unexpected exceptions are reported to Rollbar and returned as 500; `409` conflicts are reported at warning level

## Testing

- Unit tests use `@ExtendWith(MockitoExtension.class)` — no Spring context
- Controller tests use `@WebMvcTest` with `@MockitoBean` for services
- Use a small `@SpringJUnitConfig` (with `@DirtiesContext`) only to test Spring behavior such as caching, as in `StationRepositoryCachingTest`
- Assertions use AssertJ (`assertThat`, `assertThatThrownBy`)
- Use `ArgumentCaptor` to verify what was passed to mocks, not just that a method was called
- Raw `DynamoDbTable<T>` mocks require `@SuppressWarnings("rawtypes")` on the field and `@SuppressWarnings("unchecked")` on methods that use them
- `ApplicationContextTest` is the only `@SpringBootTest` — it mocks `SecretsClient` via `@TestConfiguration`

## Code Style

- No `ResponseEntity` in service methods
- No comments unless the why is non-obvious
- Prefer `Objects.requireNonNull` for null guards in public service/repository methods
- `final` on all classes that are not designed for extension (except `@Configuration` classes, which stay non-final)
- Lombok `@Builder` + `@DynamoDbImmutable` for DynamoDB model records
