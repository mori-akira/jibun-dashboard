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

resource "aws_iam_role" "scheduler_role" {
  name               = "apprunner-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "scheduler_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "scheduler_policy_doc" {
  statement {
    sid = "AllowPauseResume"
    actions = [
      "apprunner:PauseService",
      "apprunner:ResumeService"
    ]
    resources = [aws_apprunner_service.this.arn]
  }
}

resource "aws_iam_policy" "scheduler_policy" {
  name   = "apprunner-scheduler-policy"
  policy = data.aws_iam_policy_document.scheduler_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "attach" {
  role       = aws_iam_role.scheduler_role.name
  policy_arn = aws_iam_policy.scheduler_policy.arn
}

resource "aws_scheduler_schedule" "pause_nightly" {
  name                         = "apprunner-pause-01-00-jst"
  description                  = "Pause App Runner at 01:00 JST daily"
  schedule_expression          = "cron(0 1 * * ? *)"
  schedule_expression_timezone = var.timezone
  flexible_time_window { mode = "OFF" }

  target {
    role_arn = aws_iam_role.scheduler_role.arn
    arn      = "arn:aws:scheduler:::aws-sdk:apprunner:PauseService"
    input = jsonencode(
      { ServiceArn = aws_apprunner_service.this.arn }
    )
  }
}

# 06:00 に Resume
resource "aws_scheduler_schedule" "resume_morning" {
  name                         = "apprunner-resume-06-00-jst"
  description                  = "Resume App Runner at 06:00 JST daily"
  schedule_expression          = "cron(0 6 * * ? *)"
  schedule_expression_timezone = var.timezone
  flexible_time_window { mode = "OFF" }

  target {
    role_arn = aws_iam_role.scheduler_role.arn
    arn      = "arn:aws:scheduler:::aws-sdk:apprunner:ResumeService"
    input = jsonencode(
      { ServiceArn = aws_apprunner_service.this.arn }
    )
  }
}

output "apprunner_service_arn" {
  value = aws_apprunner_service.this.arn
}

output "apprunner_service_url" {
  value = aws_apprunner_service.this.service_url
}
