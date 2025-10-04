data "aws_caller_identity" "current" {}

module "application" {
  source = "./modules/application"
}

module "frontend" {
  source               = "./modules/frontend"
  region               = var.region
  application_tag      = module.application.application_tag
  frontend_bucket_name = "${var.app_name}-frontend-bucket"
}

module "cognito" {
  source               = "./modules/cognito"
  region               = var.region
  application_tag      = module.application.application_tag
  domain_name          = "${var.app_name}-${var.env_name}"
  cognito_callback_url = var.cognito_callback_url
  cognito_logout_url   = var.cognito_logout_url
}

module "ecr" {
  source          = "./modules/ecr"
  app_name        = var.app_name
  application_tag = module.application.application_tag
}

locals {
  dynamodb_specs = {
    users = {
      table_name = "${var.app_name}-${var.env_name}-users"
      hash_key   = { name = "userId", type = "S" }
    }
    resources_i18n = {
      table_name = "${var.app_name}-${var.env_name}-resources-i18n"
      hash_key   = { name = "localeCode", type = "S" }
      sort_key   = { name = "messageKey", type = "S" }
    }
    settings = {
      table_name = "${var.app_name}-${var.env_name}-settings"
      hash_key   = { name = "userId", type = "S" }
    }
    salaries = {
      table_name = "${var.app_name}-${var.env_name}-salaries"
      hash_key   = { name = "userId", type = "S" }
      sort_key   = { name = "targetDate", type = "S" }
      gsi = [
        {
          name            = "gsi_salary_id",
          hash_key_name   = "salaryId",
          hash_key_type   = "S",
          projection_type = "ALL"
        }
      ]
    }
    qualifications = {
      table_name = "${var.app_name}-${var.env_name}-qualifications"
      hash_key   = { name = "userId", type = "S" }
      sort_key   = { name = "qualificationId", type = "S" }
      gsi = [
        {
          name            = "gsi_qualification_id",
          hash_key_name   = "qualificationId",
          hash_key_type   = "S",
          projection_type = "ALL"
        }
      ]
      lsi = [
        {
          name            = "lsi_order",
          range_key_name  = "order",
          range_key_type  = "N",
          projection_type = "ALL"
        }
      ]
    }
  }
}

module "dynamodb" {
  source   = "./modules/dynamodb"
  for_each = local.dynamodb_specs

  application_tag          = module.application.application_tag
  table_name               = each.value.table_name
  hash_key                 = each.value.hash_key
  sort_key                 = try(each.value.sort_key, null)
  global_secondary_indexes = try(each.value.gsi, [])
  local_secondary_indexes  = try(each.value.lsi, [])
}

module "apprunner" {
  source             = "./modules/apprunner"
  region             = var.region
  app_name           = var.app_name
  env_name           = var.env_name
  application_tag    = module.application.application_tag
  ecr_repository_url = module.ecr.repository_url
  ecr_repository_arn = module.ecr.repository_arn

  runtime_env = {
    COGNITO_USER_POOL_ID = var.cognito_user_pool_id,
    COGNITO_CLIENT_ID    = var.cognito_client_id
    COGNITO_DOMAIN       = var.cognito_domain
  }

  dynamodb_table_arns = [
    for k, m in module.dynamodb : m.table_arn
  ]
}

module "apigateway" {
  source                 = "./modules/apigateway"
  region                 = var.region
  application_tag        = module.application.application_tag
  frontend_bucket_name   = module.frontend.bucket_name
  user_pool_id           = module.cognito.user_pool_id
  user_pool_client_id    = module.cognito.user_pool_client_id
  apprunner_url          = "https://${module.apprunner.apprunner_service_url}"
  apprunner_context_path = "/api/v1"
}

module "codebuild" {
  region                = var.region
  source                = "./modules/codebuild"
  project_name          = var.app_name
  application_tag       = module.application.application_tag
  github_url            = var.github_url
  app_url               = module.apigateway.apigw_url
  cognito_domain        = var.cognito_domain
  username              = var.e2e_username
  password              = var.e2e_password
  s3_bucket_name        = module.frontend.bucket_name
  apprunner_service_arn = module.apprunner.apprunner_service_arn
  alert_email           = var.e2e_alert_email
}
