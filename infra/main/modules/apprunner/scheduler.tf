data "aws_iam_policy_document" "lambda_trust" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "apprunner_lambda_role" {
  name               = "${var.app_name}-apprunner-ops-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "apprunner_lambda_policy_doc" {
  statement {
    sid = "Logs"
    actions = [
      "logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"
    ]
    resources = ["*"]
  }
  statement {
    sid       = "PauseResume"
    actions   = ["apprunner:PauseService", "apprunner:ResumeService"]
    resources = [aws_apprunner_service.this.arn]
  }
}

resource "aws_iam_policy" "apprunner_lambda_policy" {
  name   = "${var.app_name}-apprunner-ops-lambda-policy"
  policy = data.aws_iam_policy_document.apprunner_lambda_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "apprunner_lambda_attach" {
  role       = aws_iam_role.apprunner_lambda_role.name
  policy_arn = aws_iam_policy.apprunner_lambda_policy.arn
}

data "archive_file" "apprunner_lambda_zip" {
  type        = "zip"
  source_file = "${path.module}/apprunner_ops.py"
  output_path = "${path.module}/apprunner_ops.zip"
}

resource "aws_lambda_function" "apprunner_ops" {
  function_name = "${var.app_name}-${var.env_name}-apprunner-ops"
  role          = aws_iam_role.apprunner_lambda_role.arn
  runtime       = "python3.12"
  handler       = "apprunner_ops.handler"
  filename      = data.archive_file.apprunner_lambda_zip.output_path
  timeout       = 30
  environment {
    variables = {
      SERVICE_ARN = aws_apprunner_service.this.arn
      ACTION      = "pause"
    }
  }
  tags = var.application_tag
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

resource "aws_iam_role" "scheduler_role" {
  name               = "${var.app_name}-apprunner-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "scheduler_invoke_lambda_doc" {
  statement {
    sid     = "AllowInvokeLambda"
    actions = ["lambda:InvokeFunction"]
    resources = [
      aws_lambda_function.apprunner_ops.arn,
      "${aws_lambda_function.apprunner_ops.arn}:*"
    ]
  }
}

resource "aws_iam_policy" "scheduler_invoke_lambda" {
  name   = "${var.app_name}-apprunner-scheduler-invoke-lambda"
  policy = data.aws_iam_policy_document.scheduler_invoke_lambda_doc.json
}

resource "aws_iam_role_policy_attachment" "scheduler_invoke_lambda_attach" {
  role       = aws_iam_role.scheduler_role.name
  policy_arn = aws_iam_policy.scheduler_invoke_lambda.arn
}

resource "aws_scheduler_schedule_group" "apprunner" {
  name = "${var.app_name}-${var.env_name}-apprunner-ops"
  tags = var.application_tag
}

resource "aws_scheduler_schedule" "pause_nightly" {
  name                         = "apprunner-pause-01-00-jst"
  description                  = "Pause App Runner at 01:00 JST daily"
  schedule_expression          = "cron(0 1 * * ? *)"
  schedule_expression_timezone = var.timezone
  group_name                   = aws_scheduler_schedule_group.apprunner.name

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.apprunner_ops.arn
    role_arn = aws_iam_role.scheduler_role.arn
    input = jsonencode(
      { action = "pause" }
    )
  }
}

resource "aws_scheduler_schedule" "resume_morning" {
  name                         = "apprunner-resume-06-00-jst"
  description                  = "Resume App Runner at 06:00 JST daily"
  schedule_expression          = "cron(0 6 * * ? *)"
  schedule_expression_timezone = var.timezone
  group_name                   = aws_scheduler_schedule_group.apprunner.name

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.apprunner_ops.arn
    role_arn = aws_iam_role.scheduler_role.arn
    input = jsonencode(
      { action = "resume" }
    )
  }
}

resource "aws_lambda_permission" "allow_scheduler_pause" {
  statement_id  = "AllowSchedulerPause"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.apprunner_ops.function_name
  principal     = "scheduler.amazonaws.com"
  source_arn    = aws_scheduler_schedule.pause_nightly.arn
}

resource "aws_lambda_permission" "allow_scheduler_resume" {
  statement_id  = "AllowSchedulerResume"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.apprunner_ops.function_name
  principal     = "scheduler.amazonaws.com"
  source_arn    = aws_scheduler_schedule.resume_morning.arn
}

# 要import
resource "aws_cloudwatch_log_group" "apprunner_ops" {
  name              = "/aws/lambda/${aws_lambda_function.apprunner_ops.function_name}"
  retention_in_days = 7
  tags              = var.application_tag
}
