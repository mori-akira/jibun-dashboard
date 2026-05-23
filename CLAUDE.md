# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project Overview

**Jibun Dashboard** — a personal-data management web app (salary records, qualifications, vocabulary, etc.).
Monorepo containing frontend, backend, batch jobs, infrastructure, and supporting tools.

## Directory Structure

```
/
├ doc/      — Design docs (Markdown + draw.io)
├ infra/    — AWS infrastructure (Terraform); CLAUDE.md: see infra/README.md
├ openapi/  — API contract (OpenAPI YAML split by path/schema); source of truth for front & back
├ front/    — Nuxt 4 / Vue 3 / TypeScript SPA (SSR off); see front/CLAUDE.md
├ back/     — Kotlin / Spring Boot 3 REST API; see back/CLAUDE.md
├ batch/    — Python AWS Lambda functions
│   ├ salary-ocr/               — OCR processing for payslip images
│   ├ vocabulary-check/         — Vocabulary quiz batch
│   ├ qualification-expiry-check/ — Qualification expiry notifications
│   └ apprunner-ops/            — AppRunner start/stop operations
├ e2e/      — Playwright end-to-end tests (TypeScript)
└ tool/     — Go development utilities
    ├ dev-jwt/               — Generate local JWT for back development
    ├ payslip-data-generator/ — Generate test payslip data
    └ llmmd/                 — Aggregate source files for LLM prompts
```

## OpenAPI-First Contract

`openapi/openapi.yaml` is the single source of truth for the API.

- **back**: auto-generates API interfaces & models via `./gradlew openApiGenerate`
- **front**: auto-generates API client via `npm run generate:api`
- **e2e**: auto-generates API client via `npm run generate:api`
- Never edit generated files under `back/build/generated/` or `front/generated/api/`
- After changing the spec, regenerate in both `back/` and `front/`

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

| File | Trigger |
| --- | --- |
| `ci-back.yaml` | Push to back/** |
| `ci-front.yaml` | Push to front/** |
| `ci-batch.yaml` | Push to batch/** |
| `ci-infra.yaml` | Push to infra/** |
| `ci-openapi.yaml` | Push to openapi/** |
| `cd.yaml` | Manual / release |

## Per-Module Details

For module-specific commands, architecture, and conventions, see:
- [back/CLAUDE.md](back/CLAUDE.md)
- [front/CLAUDE.md](front/CLAUDE.md)
