# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project Overview

**Jibun Dashboard — back** — Kotlin / Spring Boot 3 REST API.
Provides endpoints for managing salary records, qualifications, vocabulary, user settings, and file assets.
Deployed on AWS AppRunner. Uses DynamoDB (Enhanced Client) as the database and S3 / SQS for file storage and async processing.

## Common Commands

```sh
# Build (also runs OpenAPI code generation)
./gradlew build

# Compile only (faster feedback)
./gradlew compileKotlin

# Run tests
./gradlew test

# Run tests with coverage report (build/reports/jacoco/)
./gradlew jacocoTestReport

# Lint check (detekt + spotless)
./gradlew check

# Auto-format source files
./gradlew spotlessApply

# OpenAPI code generation only
./gradlew openApiGenerate

# Run locally (requires local DynamoDB / S3 / SQS via LocalStack)
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Architecture & Directory Structure

```
src/main/kotlin/.../
  advice/           # Global exception handler (ApiExceptionHandler)
  component/        # Cross-cutting beans (CurrentAuth, RequestLoggingFilter)
  configuration/    # AWS client config (DynamoDB, S3, SQS, Security)
  controller/       # REST controllers — implements generated VocabularyApi etc.
  exception/        # Custom exception classes
  repository/       # DynamoDB access + DynamoDbBean item classes
  service/          # Business logic + domain model data classes (FooModel)
src/main/resources/
  application.yaml          # Common config
  application-local.yaml    # Local profile (LocalStack endpoints, HS256 JWT)
  application-dev.yaml      # Dev/prod profile (Cognito JWT)
build/generated/            # Auto-generated code from OpenAPI spec (do NOT edit)
```

## Key Technologies

| Tech | Purpose |
| --- | --- |
| Kotlin 2.0 / JVM 21 | Language & runtime |
| Spring Boot 3.3 | Web / Security / Actuator |
| Spring Security OAuth2 Resource Server | JWT validation (Cognito or HS256 for local) |
| AWS SDK v2 DynamoDB Enhanced Client | DynamoDB access with `@DynamoDbBean` annotations |
| AWS SDK v2 S3 / SQS | File storage & async OCR job dispatch |
| OpenAPI Generator (`kotlin-spring`) | Generate API interfaces & model classes from spec |
| Kotest + MockK + SpringMockK | Unit & integration testing |
| detekt | Static analysis |
| spotless + ktlint | Code formatting (LF line endings enforced) |
| Jacoco | Test coverage reports |

## OpenAPI Code Generation

The source of truth for the API is `../openapi/openapi.yaml`.

**Build flow:**
1. `bundleOpenApi` — bundles the split OpenAPI spec into `build/openapi-bundled.yaml` via `@redocly/cli`
2. `openApiGenerate` — generates interfaces (`generated.api.*`) and models (`generated.model.*`) into `build/generated/`
3. `spotlessKotlinGeneratedApply` — formats generated code
4. `compileKotlin` — compiles everything

**Rules:**
- Never edit files under `build/generated/` — they are overwritten on every build.
- After changing `../openapi/openapi.yaml`, run `./gradlew openApiGenerate` (or just `compileKotlin`) to pick up changes.
- Controllers implement the generated `*Api` interface (e.g., `VocabularyApi`).

## Conventions

### Layer Responsibilities

| Layer | Package | Responsibility |
| --- | --- | --- |
| Controller | `controller` | HTTP in/out, converts between generated API models ↔ domain models (`FooModel`) |
| Service | `service` | Business logic; owns domain model (`data class FooModel`); calls repositories |
| Repository | `repository` | DynamoDB access; owns `@DynamoDbBean` item classes (`FooItem`) |

- Controllers are thin: no business logic, only mapping and HTTP status decisions.
- Services must not import generated model classes (keep domain model independent).
- Repositories must not import service or controller classes.

### Domain Model Pattern

Each feature defines a domain model (e.g., `VocabularyModel`) in the service file, separate from:
- Generated API models (`com.github.moriakira.jibundashboard.generated.model.*`)
- DynamoDB item classes (`*Item` in the repository file)

Conversion between layers is done via private extension functions (`fun FooItem.toDomain()`, `fun FooModel.toApi()`).

### DynamoDB

- Use `@DynamoDbBean` / `@DynamoDbPartitionKey` / `@DynamoDbSortKey` / `@DynamoDbAttribute` annotations on item classes.
- GSI access via `table().index("gsi_name")`.
- Batch reads via `DynamoDbEnhancedClient.batchGetItem` with `ReadBatch`.
- Table names are configured via `app.dynamodb.tables.*` in `application.yaml` and injected with `@Value`.

### Testing

- Test files mirror the source structure under `src/test/`.
- Use **Kotest** (`describe` / `it` / `shouldBe`) for all tests.
- Use **MockK** for mocking; **SpringMockK** (`@MockkBean`) for Spring context tests.
- Controller tests use `@WebMvcTest` + `@MockkBean` for the service.
- Service tests use plain unit tests with mocked repositories.
- Repository tests use `@SpringBootTest` with a real LocalStack DynamoDB (when run in CI).

### Spring Profiles

| Profile | JWT | DynamoDB / S3 / SQS |
| --- | --- | --- |
| `local` | HS256 (hardcoded secret) | LocalStack (`http://localhost:4566` / `http://localhost:8000`) |
| `dev` / `prod` | Cognito (JWKS) | AWS |

### Coding Philosophy

- **YAGNI / KISS** — implement only what is needed now. Do not add abstractions, helpers, or flexibility for hypothetical future requirements.
- **Comments** — keep to a minimum. Add comments only to explain *why*, never *what*. Do not comment code whose intent is obvious from context.
- **Tests** — keep to a minimum. Redundant or overlapping test cases reduce clarity and increase maintenance cost. Cover meaningful scenarios; skip cases that duplicate existing coverage.

### Code Style

- Formatting is enforced by **spotless + ktlint** (`./gradlew spotlessApply` to fix).
- Static analysis is enforced by **detekt** (config: `detekt.yml` at repo root).
- Line endings must be **LF** (`lineEndings = LineEnding.UNIX` in spotless config).
- Wildcard imports are allowed; max line length warnings are disabled.
