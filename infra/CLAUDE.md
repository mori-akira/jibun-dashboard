# CLAUDE.md — infra

AWS infrastructure defined with Terraform. Split into `bootstrap/` (one-time setup) and `main/` (application resources).

## Common Commands

```sh
# Bootstrap (first time only — creates S3 backend for Terraform state)
cd bootstrap
terraform init && terraform plan && terraform apply -auto-approve

# Main infrastructure
cd main
terraform init
terraform fmt *.tf
terraform plan
terraform apply -auto-approve
terraform destroy -auto-approve  # teardown
```

## Directory Structure

```
bootstrap/   # S3 remote state backend setup (apply once)
main/
  main.tf            # Module composition
  variables.tf       # Input variables (app_name, env_name, region, etc.)
  terraform.tfvars   # Variable values (not committed for prod secrets)
  dynamodb_tables.tf # DynamoDB table definitions
  modules/
    apigateway/    # API Gateway (HTTP API)
    application/   # Application tag / metadata
    apprunner/     # AppRunner service for back
    batch/         # Lambda functions for batch jobs
    codebuild/     # CodeBuild for CI/CD
    cognito/       # Cognito User Pool & App Client
    dynamodb/      # DynamoDB tables
    ecr/           # ECR repository for Docker images
    frontend/      # S3 + CloudFront for front
    uploads/       # S3 bucket for payslip uploads
    user-assets/   # S3 bucket for user assets
```

## Key Notes

- AppRunner default log groups must be imported manually on first deploy (see `infra/README.md`).
- Terraform state is stored remotely in S3 (configured in `bootstrap/`).
- Required env var: `TF_VAR_region`
