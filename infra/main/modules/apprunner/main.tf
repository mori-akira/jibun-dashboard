data "aws_iam_policy_document" "apprunner_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["build.apprunner.amazonaws.com", "tasks.apprunner.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "instance" {
  name               = "${var.app_name}-apprunner-instance-role"
  assume_role_policy = data.aws_iam_policy_document.apprunner_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "apprunner_instance" {
  # ECR: 認証トークン
  statement {
    sid    = "EcrGetAuthorizationToken"
    effect = "Allow"
    actions = [
      "ecr:GetAuthorizationToken",
    ]
    resources = ["*"]
  }

  # ECR: イメージ取得
  statement {
    sid    = "EcrPullImage"
    effect = "Allow"
    actions = [
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchCheckLayerAvailability",
      "ecr:DescribeImages",
    ]
    resources = [var.ecr_repository_arn]
  }

  # DynamoDB
  statement {
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
    resources = concat(
      var.dynamodb_table_arns,
      [for arn in var.dynamodb_table_arns : "${arn}/index/*"]
    )
  }

  # アップロードS3バケット: オブジェクト操作
  statement {
    sid    = "AllowObjectAccessToUploads"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:AbortMultipartUpload",
    ]
    resources = [
      "${var.uploads_bucket_arn}/*"
    ]
  }

  # アップロードS3バケット: マルチパート一覧
  statement {
    sid    = "AllowListMultipartUploads"
    effect = "Allow"
    actions = [
      "s3:ListBucketMultipartUploads",
    ]
    resources = [
      var.uploads_bucket_arn
    ]
  }

  # SQS
  dynamic "statement" {
    for_each = length(var.sqs_queue_arns) == 0 ? [] : [1]
    content {
      sid    = "AllowSendMessageToSqs"
      effect = "Allow"
      actions = [
        "sqs:SendMessage",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl"
      ]
      resources = var.sqs_queue_arns
    }
  }
}

resource "aws_iam_role_policy" "apprunner_instance_policy" {
  name   = "${var.app_name}-${var.env_name}-apprunner-instance-policy"
  role   = aws_iam_role.instance.id
  policy = data.aws_iam_policy_document.apprunner_instance.json
}

resource "aws_apprunner_service" "this" {
  service_name = "${var.app_name}-${var.env_name}"
  tags         = var.application_tag

  source_configuration {
    authentication_configuration {
      access_role_arn = aws_iam_role.instance.arn
    }

    image_repository {
      image_repository_type = "ECR"
      image_identifier      = "${var.ecr_repository_url}:${var.image_tag}"
      image_configuration {
        port = tostring(var.container_port)

        runtime_environment_variables = merge({
          APP_NAME                    = var.app_name
          ENV_NAME                    = var.env_name
          AWS_REGION                  = var.region
          SPRING_PROFILES_ACTIVE      = var.env_name
          SERVER_PORT                 = tostring(var.container_port)
          SERVER_SERVLET_CONTEXT_PATH = var.server_servlet_context_path
          COGNITO_USER_POOL_ID        = var.cognito_user_pool_id
          COGNITO_CLIENT_ID           = var.cognito_client_id
          COGNITO_DOMAIN              = var.cognito_domain
          UPLOADS_BUCKET_NAME         = var.uploads_bucket_name
        }, var.runtime_env)
      }
    }

    auto_deployments_enabled = var.auto_deploy
  }

  instance_configuration {
    cpu               = tostring(var.cpu)
    memory            = tostring(var.memory)
    instance_role_arn = aws_iam_role.instance.arn
  }

  health_check_configuration {
    protocol            = "HTTP"
    path                = var.health_check_path
    healthy_threshold   = 1
    unhealthy_threshold = 3
    interval            = 20
    timeout             = 10
  }
}

locals {
  apprunner_arn_parts    = split("/", aws_apprunner_service.this.arn)
  apprunner_service_name = local.apprunner_arn_parts[length(local.apprunner_arn_parts) - 2]
  apprunner_service_id   = local.apprunner_arn_parts[length(local.apprunner_arn_parts) - 1]
}

# 要import
resource "aws_cloudwatch_log_group" "apprunner_service_log" {
  name              = "/aws/apprunner/${local.apprunner_service_name}/${local.apprunner_service_id}/service"
  retention_in_days = 7
  tags              = var.application_tag
}

# 要import
resource "aws_cloudwatch_log_group" "apprunner_application_log" {
  name              = "/aws/apprunner/${local.apprunner_service_name}/${local.apprunner_service_id}/application"
  retention_in_days = 7
  tags              = var.application_tag
}
