terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.50" }
  }
}

provider "aws" {
  region                      = "ap-northeast-1"
  access_key                  = "dummy"
  secret_key                  = "dummy"
  skip_credentials_validation = true
  skip_requesting_account_id  = true
  endpoints { dynamodb = "http://localhost:8000" }
}

locals {
  app_name = "jibun-dashboard"
  env_name = "local"
  tag      = { app = local.app_name, env = local.env_name }
}

module "dynamodb_users" {
  source          = "../../../infra/main/modules/dynamodb"
  application_tag = local.tag
  table_name      = "${local.app_name}-${local.env_name}-users"
  hash_key = {
    name = "userId"
    type = "S"
  }
}

module "dynamodb_resources_i18n" {
  source          = "../../../infra/main/modules/dynamodb"
  application_tag = local.tag
  table_name      = "${local.app_name}-${local.env_name}-resources-i18n"
  hash_key = { name = "localeCode", type = "S" }
  sort_key = { name = "messageKey", type = "S" }
}

module "dynamodb_settings" {
  source          = "../../../infra/main/modules/dynamodb"
  application_tag = local.tag
  table_name      = "${local.app_name}-${local.env_name}-settings"
  hash_key = { name = "userId", type = "S" }
}

module "dynamodb_salaries" {
  source          = "../../../infra/main/modules/dynamodb"
  application_tag = local.tag
  table_name      = "${local.app_name}-${local.env_name}-salaries"
  hash_key = { name = "userId", type = "S" }
  sort_key = { name = "targetDate", type = "S" }
  global_secondary_indexes = [
    {
      name               = "gsi_salary_id"
      hash_key_name      = "salaryId"
      range_key_name     = null
      projection_type    = "ALL"
      non_key_attributes = null
    }
  ]
}

module "dynamodb_qualifications" {
  source          = "../../../infra/main/modules/dynamodb"
  application_tag = local.tag
  table_name      = "${local.app_name}-${local.env_name}-qualifications"
  hash_key = { name = "userId", type = "S" }
  global_secondary_indexes = [
    {
      name               = "gsi_qualification_id"
      hash_key_name      = "qualificationId"
      range_key_name     = null
      projection_type    = "ALL"
      non_key_attributes = null
    }
  ]
}

output "done" { value = "applied to dynamodb-local" }
