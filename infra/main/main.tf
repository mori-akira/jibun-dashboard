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

module "artifacts" {
  source               = "./modules/artifacts"
  region               = var.region
  application_tag      = module.application.application_tag
  artifact_bucket_name = "${var.app_name}-artifact-bucket"
}

module "dynamodb" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
}

module "lambda_resource" {
  source          = "./modules/lambda"
  region          = var.region
  application_tag = module.application.application_tag
  function_name   = "${var.app_name}-resource-${var.env_name}"
  service_name    = "resource"
  environment = {
    SERVER_SERVLET_CONTEXT_PATH = "/api/v1"
  }
  artifacts_bucket_name = module.artifacts.bucket_name
}

module "api_gateway" {
  source               = "./modules/api_gateway"
  region               = var.region
  application_tag      = module.application.application_tag
  frontend_bucket_name = module.frontend.bucket_name
  s3_bucket_arn        = module.frontend.bucket_arn
  user_pool_id         = module.cognito.user_pool_id
  user_pool_client_id  = module.cognito.user_pool_client_id
  resource_lambda_arn  = module.lambda_resource.function_arn
}

output "login_url" {
  value = module.cognito.cognito_login_url
}

output "api_gateway_url" {
  value = module.api_gateway.apigw_url
}
