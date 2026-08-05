# CLAUDE.md

## Project Overview

Spring Boot REST API for crowdsourced smoking reports on Chicago CTA trains. Backed by DynamoDB on AWS, deployed via Docker to ECR.

## Architecture

- **Controllers** — HTTP concerns only (build `ResponseEntity`, `Location` headers, path/query binding, map models to DTOs for the response body)
- **Services** — business logic, return models directly, throw typed exceptions on not-found
- **Repositories** — DynamoDB access via the Enhanced Client (`DynamoDbEnhancedClient`)
- **`@ConfigurationProperties` records** — one record per config namespace, validated with JSR-303

Package layout: feature (`smoking.report`, `smoking.aggregate`) then layer (`controller`, `service`, `repository`, `dto`, `model`, `exception`). Shared types live in `smoking.common`.

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
- Test classes do not use `@NullMarked`.
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
- PK: `date` (e.g. `2026-05-10`)
- SK: `reportId` (e.g. `1771710202399_uuid`)

Table: `cta-smoking-report-aggregates`
- PK: `LINE#<TrainLine>` (e.g. `LINE#RED`)
- SK patterns:
  - `DAY#2026-05-10`
  - `WEEK#2026-W13`
  - `MONTH#2026-03`
  - `YEAR#2026`
  - `ALL_TIME`

## Testing

- Unit tests use `@ExtendWith(MockitoExtension.class)` — no Spring context
- Assertions use AssertJ (`assertThat`, `assertThatThrownBy`)
- Use `ArgumentCaptor` to verify what was passed to mocks, not just that a method was called
- Raw `DynamoDbTable<T>` mocks require `@SuppressWarnings("rawtypes")` on the field and `@SuppressWarnings("unchecked")` on methods that use them
- `ApplicationContextTest` is the only `@SpringBootTest` — it mocks `AwsSecretsClient` via `@TestConfiguration`

## Code Style

- No `ResponseEntity` in service methods
- No comments unless the why is non-obvious
- Prefer `Objects.requireNonNull` for null guards in public service/repository methods
- `final` on all classes that are not designed for extension
- Lombok `@Builder` + `@DynamoDbImmutable` for DynamoDB model records
