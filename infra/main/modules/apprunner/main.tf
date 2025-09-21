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

resource "aws_iam_role_policy" "apprunner_ecr_policy" {
  role = aws_iam_role.instance.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["ecr:GetAuthorizationToken"],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchCheckLayerAvailability",
          "ecr:DescribeImages",
        ],
        Resource = var.ecr_repository_arn
      }
    ]
  })
}

data "aws_iam_policy_document" "apprunner_dynamodb" {
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
}

resource "aws_iam_policy" "dynamodb" {
  name   = "${var.app_name}-apprunner-dynamodb-policy"
  policy = data.aws_iam_policy_document.apprunner_dynamodb.json
}

resource "aws_iam_role_policy_attachment" "attach_dynamodb" {
  role       = aws_iam_role.instance.name
  policy_arn = aws_iam_policy.dynamodb.arn
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
