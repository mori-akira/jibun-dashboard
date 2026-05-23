# CLAUDE.md — batch

Python AWS Lambda functions. Each job is self-contained under its own subdirectory.

## Jobs

| Directory | Trigger | Summary |
| --- | --- | --- |
| `salary-ocr/` | SQS (ReportBatchItemFailures) | Download payslip PDF from S3 → OCR via Bedrock Converse API → save to `salaries` DynamoDB table; track task status in `salary_ocr_tasks` |
| `vocabulary-check/` | Scheduled (EventBridge) | Scan all users' vocabularies → check quality via Bedrock → save results to `vocabulary_check_results` |
| `qualification-expiry-check/` | Scheduled (EventBridge) | Scan `qualifications` table for `expirationDate < today` → set `status = "expired"` |
| `apprunner-ops/` | Manual / EventBridge | Pause or resume the AppRunner service; `action` from event or `ACTION` env var |

## Per-Job Structure

```
<job>/
  index.py          # lambda_handler entry point
  test_index.py     # unit tests (pytest)
  requirements.txt  # boto3>=1.34, python-dotenv>=1.0.0
  prompts/          # Bedrock prompt files (salary-ocr, vocabulary-check only)
```

## Common Commands

```sh
# Install dependencies (run inside each job directory)
pip install -r requirements.txt

# Run tests
pytest test_index.py
```

## Key Conventions

- Each job's `lambda_handler` uses `get_env_or_raise()` to enforce required environment variables at startup.
- Bedrock calls use `converse()` with exponential backoff retry.
- `salary-ocr` uses `ReportBatchItemFailures` response format — partial batch failures re-queue only failed messages.
- Prompt files (`prompts/system.md`, `prompts/user.md`) are loaded at runtime from the Lambda package.
