provider "aws" {
  region = var.region
}

module "application" {
  source = "./modules/application"
}

module "dynamodb" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
}

module "frontend" {
  source               = "./modules/frontend"
  region               = var.region
  application_tag      = module.application.application_tag
  frontend_bucket_name = "${var.app_name}-frontend-bucket"
}

module "cognito" {
  source          = "./modules/cognito"
  region          = var.region
  application_tag = module.application.application_tag
  domain_name     = "${var.app_name}.${var.env_name}"
}

module "api_gateway" {
  source               = "./modules/api_gateway"
  region               = var.region
  application_tag      = module.application.application_tag
  frontend_bucket_name = module.frontend.bucket_name
  s3_bucket_arn        = module.frontend.bucket_arn
  user_pool_id         = module.cognito.user_pool_id
  user_pool_client_id  = module.cognito.user_pool_client_id
}

output "login_url" {
  value = module.cognito.cognito_login_url
}

output "api_gateway_url" {
  value = module.api_gateway.apigw_url
}
