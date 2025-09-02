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

module "dynamodb_users" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-users"
  hash_key = {
    name = "userId"
    type = "S"
  }
}

module "dynamodb_resources_i18n" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-resources-i18n"
  hash_key = {
    name = "localeCode"
    type = "S"
  }
  sort_key = {
    name = "messageKey"
    type = "S"
  }
}

module "dynamodb_settings" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-settings"
  hash_key = {
    name = "userId"
    type = "S"
  }
}

module "dynamodb_salaries" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-salaries"
  hash_key = {
    name = "userId",
    type = "S"
  }
  sort_key = {
    name = "targetDate",
    type = "S"
  }
  global_secondary_indexes = [
    {
      name            = "gsi_salary_id"
      hash_key_name   = "salaryId"
      range_key_name  = null
      projection_type = "ALL"
    }
  ]
}

module "dynamodb_qualifications" {
  source          = "./modules/dynamodb"
  application_tag = module.application.application_tag
  table_name      = "${var.app_name}-${var.env_name}-qualifications"
  hash_key = {
    name = "userId",
    type = "S"
  }
  sort_key = {
    name = "qualificationId",
    type = "S"
  }
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "apprunner_instance_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"] # 実行系
    }
  }
}

resource "aws_iam_role" "apprunner_instance_role" {
  name               = "${var.app_name}-apprunner-instance-role"
  assume_role_policy = data.aws_iam_policy_document.apprunner_instance_trust.json
  tags               = module.application.application_tag
}

data "aws_iam_policy_document" "apprunner_dynamodb_policy_doc" {
  statement {
    sid    = "DynamoDbReadWrite"
    effect = "Allow"
    actions = [
      "dynamodb:BatchWriteItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:DeleteItem",
      "dynamodb:GetItem",
      "dynamodb:BatchGetItem",
      "dynamodb:Query",
      "dynamodb:Scan",
      "dynamodb:DescribeTable",
    ]
    resources = [
      module.dynamodb_users.table_arn,
      module.dynamodb_resources_i18n.table_arn,
      module.dynamodb_settings.table_arn,
      module.dynamodb_salaries.table_arn,
      module.dynamodb_qualifications.table_arn,

      // セカンダリインデックス
      "${module.dynamodb_salaries.table_arn}/index/*",
    ]
  }
}

resource "aws_iam_policy" "apprunner_dynamodb_policy" {
  name   = "${var.app_name}-apprunner-dynamodb-policy"
  policy = data.aws_iam_policy_document.apprunner_dynamodb_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "apprunner_instance_attach" {
  role       = aws_iam_role.apprunner_instance_role.name
  policy_arn = aws_iam_policy.apprunner_dynamodb_policy.arn
}

module "apprunner" {
  source             = "./modules/apprunner"
  region             = var.region
  app_name           = var.app_name
  env_name           = var.env_name
  instance_role_arn  = aws_iam_role.apprunner_instance_role.arn
  application_tag    = module.application.application_tag
  ecr_repository_url = module.ecr.repository_url
  ecr_repository_arn = "arn:aws:ecr:${var.region}:${data.aws_caller_identity.current.account_id}:repository/${var.app_name}"
  runtime_env = {
    COGNITO_USER_POOL_ID = var.cognito_user_pool_id,
    COGNITO_CLIENT_ID    = var.cognito_client_id
  }
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
