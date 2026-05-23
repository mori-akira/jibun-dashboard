# CLAUDE.md — tool

Go development utilities. Each tool is a standalone `go run .` command.

## Tools

### `dev-jwt/`
Generates a local JWT for authenticating against the backend in local development.
The backend's `local` profile accepts HS256 tokens — use this to create one.
```sh
cd dev-jwt && go run .
```

### `payslip-data-generator/`
Generates test payslip data and seeds it into a local DynamoDB (LocalStack).
Useful for populating salary records without uploading real PDFs.
```sh
cd payslip-data-generator && go run .
```

### `llmmd/`
Aggregates source files across the monorepo into a single Markdown document optimised for LLM prompts.
```sh
cd llmmd && go run . -profile [プロフィール名]
```

## Common Commands

```sh
# Run any tool
cd <tool-dir> && go run .

# Update dependencies
go mod tidy
```
