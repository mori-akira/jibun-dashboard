# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Project Overview

**Jibun Dashboard** — a Nuxt 3 (Vue 3 / TypeScript) single-page application (SSR disabled).
The app manages personal data such as salary records, qualifications, study plans, and vocabulary lists.
It supports both desktop and mobile layouts, and uses AWS Cognito for authentication.

## Common Commands

```sh
# Install dependencies
npm install

# Start development server (http://localhost:3333)
npm run dev

# Start mock API server (port 4011, requires ../openapi/openapi.yaml)
npm run mock

# Generate API client from OpenAPI spec
npm run generate:api

# Type-check without emitting
npm run typecheck

# Run unit tests
npx vitest run

# Build for production
npm run build
```

## Architecture & Directory Structure

```
app/            # Nuxt app config (i18n)
assets/css/     # Global CSS (Tailwind entry point)
components/
  app/          # Desktop header / navigation
  common/       # Shared UI components (Button, Dialog, DataTable, etc.)
  mobile/       # Mobile-specific header / navigation
  qualification/# Qualification feature components
  salary/       # Salary feature components
composables/    # Vue composables (auth, API client, dialogs, polling, etc.)
generated/api/  # Auto-generated API client (do NOT edit manually)
layouts/        # default.vue (desktop), mobile.vue (mobile)
middleware/     # Global route middleware (auth, i18n)
pages/          # File-based routing
  m/            # Mobile versions of pages (mirrors desktop structure)
plugins/        # Nuxt plugins (axios, chart.js, i18n, pinia, veeValidate)
server/         # Server middleware
stores/         # Pinia stores (common, qualification, salary, setting, user)
utils/          # Pure utility functions (+ *.test.ts co-located tests)
scripts/        # Build-time scripts (generate-api-client.mjs)
```

## Key Technologies

| Tech                       | Purpose                                         |
| -------------------------- | ----------------------------------------------- |
| Nuxt 4 / Vue 3             | Framework / SPA (SSR off)                       |
| TypeScript                 | Static typing throughout                        |
| Tailwind CSS v4 + @nuxt/ui | Styling & UI components                         |
| Pinia                      | Global state management                         |
| VeeValidate + Zod          | Form validation                                 |
| @nuxtjs/i18n               | Internationalisation (default locale: `en`)     |
| Axios                      | HTTP client (configured via `plugins/axios.ts`) |
| Chart.js + vue-chartjs     | Data visualisation                              |
| openapi-zod-client         | API client / schema generation from OpenAPI     |
| @stoplight/prism-cli       | Mock API server                                 |
| Vitest + @nuxt/test-utils  | Unit testing                                    |
| @vite-pwa/nuxt             | PWA support                                     |
| AWS Cognito                | Authentication & authorisation                  |

## Conventions

### Components

- Each component lives in the feature subfolder matching the page it belongs to.
- Shared components go in `components/common/`.
- Mobile-specific versions go in `components/mobile/`.
- Use `<script setup lang="ts">` for all components.

### API Client

- The API client in `generated/api/` is auto-generated — never edit it manually.
- Re-run `npm run generate:api` after any OpenAPI spec change.
- Call the API exclusively through composables (`useApiClient`) to keep pages clean.

### Stores (Pinia)

- One store per domain: `common`, `qualification`, `salary`, `setting`, `user`.
- Use the Options Store style consistent with existing stores.

### Forms

- Use VeeValidate with Zod schemas for all form validation.
- Helper in `utils/zod-to-vee-rules.ts` converts Zod schemas to VeeValidate rules.

### Testing

- Unit test files are co-located with source files as `*.test.ts`.
- Use Vitest (`describe` / `it` / `expect`) — no test-specific config file; tests run via `npx vitest run`.

### Routing

- Desktop pages live in `pages/` (e.g., `pages/salary/index.vue`).
- Mobile pages mirror the structure under `pages/m/` and use the `mobile` layout.
- Auth guard is enforced globally via `middleware/auth.global.ts`.

### Environment Variables

All public runtime config keys are prefixed `NUXT_PUBLIC_`:

- `NUXT_PUBLIC_BASE_URL` — base URL path
- `NUXT_PUBLIC_API_BASE_URL` — API base URL
- `NUXT_PUBLIC_API_MODE` — `default` or other modes to toggle mock/real API
- `NUXT_PUBLIC_REQUIRE_AUTH` — set to `on` to enforce login
- `NUXT_PUBLIC_REGION` — AWS region
- `NUXT_PUBLIC_COGNITO_CLIENT_ID` / `NUXT_PUBLIC_COGNITO_DOMAIN` — Cognito config
