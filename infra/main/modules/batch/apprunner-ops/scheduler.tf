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
  name               = "${var.app_name}-${var.env_name}-apprunner-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "scheduler_invoke_lambda_doc" {
  statement {
    sid     = "AllowInvokeLambda"
    actions = ["lambda:InvokeFunction"]
    resources = [
      aws_lambda_function.apprunner_ops_lambda.arn,
      "${aws_lambda_function.apprunner_ops_lambda.arn}:*"
    ]
  }
}

resource "aws_iam_role_policy" "scheduler_invoke_lambda" {
  name   = "${var.app_name}-${var.env_name}-apprunner-scheduler-invoke-lambda"
  role   = aws_iam_role.scheduler_role.id
  policy = data.aws_iam_policy_document.scheduler_invoke_lambda_doc.json
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
  name              = "/aws/lambda/${aws_lambda_function.apprunner_ops_lambda.function_name}"
  retention_in_days = 7
  tags              = var.application_tag
}
