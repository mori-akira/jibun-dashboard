# CLAUDE.md — e2e

Playwright end-to-end tests (TypeScript). Tests run against a live frontend URL.

## Common Commands

```sh
# Install dependencies
npm install

# Install Playwright browsers
npx playwright install --with-deps

# Generate API client from OpenAPI spec
npm run generate:api

# Run all tests (no-auth → auth → teardown)
npm run test

# Run specific projects
npm run test:no-auth
npm run test:setup     # creates auth session (.playwright/auth/user.json)
npm run test:seed      # seeds test data (depends on setup)
npm run test:auth      # main authenticated test suite
npm run test:teardown  # cleanup after auth tests

# View last test report
npm run report

# Record new test with codegen
npm run codegen
```

## Configuration

- Copy `.env.example` → `.env` for local runs
- Key env vars: `E2E_APP_URL` (default: `http://localhost:3000`), `E2E_CI=true` for headless parallel mode

## Directory Structure

```
tests/
  no-auth/    # Tests that don't require login
  setup/      # Creates auth session (storageState)
  seed/       # Seeds DynamoDB test data
  auth/       # Authenticated feature tests
  teardown/   # Cleanup after auth tests
helpers/      # Shared test utilities
generated/    # Auto-generated API client (do NOT edit manually)
scripts/      # generate-api-client.mjs
```

## Project Dependencies

```
no-auth  (independent)
setup    (independent)
seed     → depends on setup
auth     → depends on setup
teardown → depends on auth
```

Auth state is stored in `.playwright/auth/user.json` and reused across authenticated projects.
