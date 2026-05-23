# CLAUDE.md — openapi

OpenAPI 3.0 spec — the single source of truth for the API contract between `front/` and `back/`.

## Structure

```
openapi.yaml        # Root spec (references paths/ and schemas/ via $ref)
paths/              # One file per resource group
  files/
  qualifications/
  resources/
  salaries/
  settings/
  share/
  shared-links/
  users/
  vocabularies/
  vocabulary-check-results/
  vocabulary-quiz-histories/
  vocabulary-tags/
schemas/            # Reusable schema objects
  error/
  file/
  qualification/
  resource/
  salary/
  setting/
  shared-link/
  user/
  vocabulary/
```

## How Consumers Use This Spec

| Consumer | Command | Output |
| --- | --- | --- |
| `back/` | `./gradlew openApiGenerate` | `back/build/generated/` — Kotlin API interfaces & models |
| `front/` | `npm run generate:api` | `front/generated/api/` — TypeScript API client |
| `e2e/` | `npm run generate:api` | `e2e/generated/` — TypeScript API client |

**After editing the spec, regenerate in all consumers.**

## Viewing

Open `openapi.yaml` in VSCode → `Alt + Shift + P` (Swagger Viewer).

## Conventions

- Add new endpoints by creating a file under `paths/<resource>/` and `$ref`-ing it from `openapi.yaml`.
- Add new models under `schemas/<model>/` and `$ref`-ing them from path files.
- Security: all endpoints use `CognitoJwtAuth` by default (defined at the root level).
