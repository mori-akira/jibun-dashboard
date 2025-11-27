data "aws_iam_policy_document" "codebuild_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["codebuild.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "codebuild" {
  name               = "${var.project_name}-codebuild-role"
  assume_role_policy = data.aws_iam_policy_document.codebuild_trust.json
  tags               = var.application_tag
}

data "aws_caller_identity" "current" {}

data "aws_iam_policy_document" "codebuild_policy" {
  statement {
    sid    = "Logs"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
    resources = ["arn:aws:logs:*:*:*"]
  }

  statement {
    sid       = "S3ListBucket"
    effect    = "Allow"
    actions   = ["s3:ListBucket", "s3:GetBucketLocation"]
    resources = [aws_s3_bucket.artifacts.arn]
  }

  statement {
    sid       = "S3Objects"
    effect    = "Allow"
    actions   = ["s3:GetObject", "s3:PutObject"]
    resources = ["${aws_s3_bucket.artifacts.arn}/*"]
  }

  statement {
    sid    = "SSMParameters"
    effect = "Allow"
    actions = [
      "ssm:GetParameters",
      "ssm:GetParameter",
    ]
    resources = ["arn:aws:ssm:${var.region}:${data.aws_caller_identity.current.account_id}:parameter/${var.project_name}/e2e/*"]
  }

  statement {
    sid    = "CodeBuild"
    effect = "Allow"
    actions = [
      "codebuild:CreateReportGroup",
      "codebuild:CreateReport",
      "codebuild:UpdateReport",
      "codebuild:BatchPutTestCases",
      "codebuild:BatchPutCodeCoverages",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "codebuild" {
  name   = "${var.project_name}-codebuild-policy"
  policy = data.aws_iam_policy_document.codebuild_policy.json
  tags   = var.application_tag
}

resource "aws_iam_role_policy_attachment" "codebuild" {
  role       = aws_iam_role.codebuild.name
  policy_arn = aws_iam_policy.codebuild.arn
}

resource "aws_s3_bucket" "artifacts" {
  bucket        = "${var.project_name}-codebuild-artifacts"
  force_destroy = true
  tags          = var.application_tag
}

resource "aws_s3_bucket_lifecycle_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  rule {
    id     = "expire-old-artifacts"
    status = "Enabled"
    filter {}
    expiration {
      days = 7
    }
  }
}

resource "aws_cloudwatch_log_group" "this" {
  name              = "/aws/codebuild/${var.project_name}"
  retention_in_days = 7
  tags              = var.application_tag
}

resource "aws_codebuild_project" "this" {
  name         = var.project_name
  service_role = aws_iam_role.codebuild.arn
  tags         = var.application_tag

  build_timeout          = var.build_timeout
  queued_timeout         = var.queued_timeout
  concurrent_build_limit = 1

  artifacts {
    type                   = "S3"
    location               = aws_s3_bucket.artifacts.bucket
    packaging              = "ZIP"
    name                   = "playwright-report.zip"
    override_artifact_name = true
  }

  environment {
    compute_type    = "BUILD_GENERAL1_SMALL"
    image           = "aws/codebuild/standard:7.0"
    type            = "LINUX_CONTAINER"
    privileged_mode = false

    environment_variable {
      name  = "E2E_APP_URL"
      value = var.app_url
      type  = "PLAINTEXT"
    }

    environment_variable {
      name  = "E2E_COGNITO_DOMAIN"
      value = var.cognito_domain
      type  = "PLAINTEXT"
    }

    environment_variable {
      name  = "E2E_USERNAME"
      value = var.username
      type  = "PLAINTEXT"
    }

    environment_variable {
      name  = "E2E_PASSWORD"
      value = "/${var.project_name}/e2e/password"
      type  = "PARAMETER_STORE"
    }
  }

  logs_config {
    cloudwatch_logs {
      group_name  = aws_cloudwatch_log_group.this.name
      stream_name = "build"
    }
  }

  source {
    type            = "GITHUB"
    location        = var.github_url
    buildspec       = "e2e/buildspec.yml"
    git_clone_depth = 1
  }
}
