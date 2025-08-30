provider "aws" {
  region = var.region
}

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

module "dynamodb_user" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-users"
  hash_key = {
    name = "userId"
    type = "S"
  }
}

module "dynamodb_resource_i18n" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-resource-i18n"
  hash_key = {
    name = "localeCode"
    type = "S"
  }
  sort_key = {
    name = "messageKey"
    type = "S"
  }
}

data "aws_caller_identity" "current" {}

module "apprunner" {
  source             = "./modules/apprunner"
  app_name           = var.app_name
  env_name           = var.env_name
  application_tag    = module.application.application_tag
  ecr_repository_url = module.ecr.repository_url
  ecr_repository_arn = "arn:aws:ecr:${var.region}:${data.aws_caller_identity.current.account_id}:repository/${var.app_name}"
}

module "api_gateway" {
  source               = "./modules/api_gateway"
  region               = var.region
  application_tag      = module.application.application_tag
  frontend_bucket_name = module.frontend.bucket_name
  s3_bucket_arn        = module.frontend.bucket_arn
  user_pool_id         = module.cognito.user_pool_id
  user_pool_client_id  = module.cognito.user_pool_client_id
  apprunner_url        = module.apprunner.apprunner_service_url
}

output "login_url" {
  value = module.cognito.cognito_login_url
}

output "api_gateway_url" {
  value = module.api_gateway.apigw_url
}

output "apprunner_url" {
  value = module.apprunner.apprunner_service_url
}
