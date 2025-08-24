data "aws_iam_policy_document" "apprunner_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["build.apprunner.amazonaws.com", "tasks.apprunner.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "apprunner_ecr_access" {
  name               = "${var.app_name}-apprunner-ecr-role"
  assume_role_policy = data.aws_iam_policy_document.apprunner_trust.json
  tags               = var.application_tag
}

resource "aws_iam_role_policy" "apprunner_ecr_policy" {
  role = aws_iam_role.apprunner_ecr_access.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      { Effect = "Allow", Action = ["ecr:GetAuthorizationToken"], Resource = "*" },
      {
        Effect = "Allow",
        Action = [
          "ecr:BatchGetImage", "ecr:GetDownloadUrlForLayer", "ecr:DescribeImages"
        ],
        Resource = var.ecr_repository_arn
      }
    ]
  })
}

resource "aws_apprunner_service" "this" {
  service_name = "${var.app_name}-${var.env_name}"
  tags         = var.application_tag

  source_configuration {
    authentication_configuration {
      access_role_arn = aws_iam_role.apprunner_ecr_access.arn
    }

    image_repository {
      image_repository_type = "ECR"
      image_identifier      = "${var.ecr_repository_url}:${var.image_tag}"
      image_configuration {
        port = tostring(var.container_port)
        runtime_environment_variables = {
        }
      }
    }

    auto_deployments_enabled = var.auto_deploy
  }

  instance_configuration {
    cpu    = tostring(var.cpu)
    memory = tostring(var.memory)
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

output "apprunner_service_arn" {
  value = aws_apprunner_service.this.arn
}

output "apprunner_service_url" {
  value = aws_apprunner_service.this.service_url
}
