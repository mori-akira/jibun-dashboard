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

module "uploads" {
  source          = "./modules/uploads"
  bucket_name     = "${var.app_name}-${var.env_name}-uploads-bucket"
  application_tag = module.application.application_tag
  allowed_origins = [
    "http://localhost:8080",
    var.api_gateway_origin,
  ]
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

module "batch_salary_ocr" {
  source              = "./modules/batch/salary-ocr"
  application_tag     = module.application.application_tag
  app_name            = var.app_name
  env_name            = var.env_name
  uploads_bucket_name = module.uploads.bucket_name
  uploads_bucket_arn  = module.uploads.bucket_arn
  salary_table_name   = module.dynamodb["salaries"].table_name
  salary_table_arn    = module.dynamodb["salaries"].table_arn
  ocr_task_table_name = module.dynamodb["salary_ocr_tasks"].table_name
  ocr_task_table_arn  = module.dynamodb["salary_ocr_tasks"].table_arn
  openai_api_key      = var.openai_api_key
}

module "apprunner" {
  source             = "./modules/apprunner"
  region             = var.region
  app_name           = var.app_name
  env_name           = var.env_name
  application_tag    = module.application.application_tag
  ecr_repository_url = module.ecr.repository_url
  ecr_repository_arn = module.ecr.repository_arn

  dynamodb_table_arns = [
    for k, m in module.dynamodb : m.table_arn
  ]

  cognito_user_pool_id = var.cognito_user_pool_id
  cognito_client_id    = var.cognito_client_id
  cognito_domain       = var.cognito_domain

  uploads_bucket_name = module.uploads.bucket_name
  uploads_bucket_arn  = module.uploads.bucket_arn

  sqs_queue_arns       = [module.batch_salary_ocr.sqs_queue_arn]
  salary_ocr_queue_url = module.batch_salary_ocr.sqs_queue_url
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
