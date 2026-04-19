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
  name               = "${var.app_name}-${var.env_name}-qualification-expiry-check-scheduler-role"
  assume_role_policy = data.aws_iam_policy_document.scheduler_trust.json
  tags               = var.application_tag
}

data "aws_iam_policy_document" "scheduler_invoke_lambda_doc" {
  statement {
    sid     = "AllowInvokeLambda"
    actions = ["lambda:InvokeFunction"]
    resources = [
      aws_lambda_function.qualification_expiry_check_lambda.arn,
      "${aws_lambda_function.qualification_expiry_check_lambda.arn}:*"
    ]
  }
}

resource "aws_iam_role_policy" "scheduler_invoke_lambda" {
  name   = "${var.app_name}-${var.env_name}-qualification-expiry-check-scheduler-invoke-lambda"
  role   = aws_iam_role.scheduler_role.id
  policy = data.aws_iam_policy_document.scheduler_invoke_lambda_doc.json
}

resource "aws_scheduler_schedule_group" "qualification_expiry_check" {
  name = "${var.app_name}-${var.env_name}-qualification-expiry-check"
  tags = var.application_tag
}

resource "aws_scheduler_schedule" "qualification_expiry_check_daily" {
  name                         = "qualification-expiry-check-00-00"
  description                  = "Check qualification expiry dates and update status to expired at 00:00 daily"
  schedule_expression          = "cron(0 0 * * ? *)"
  schedule_expression_timezone = var.timezone
  group_name                   = aws_scheduler_schedule_group.qualification_expiry_check.name

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.qualification_expiry_check_lambda.arn
    role_arn = aws_iam_role.scheduler_role.arn
  }
}

resource "aws_lambda_permission" "allow_scheduler" {
  statement_id  = "AllowScheduler"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.qualification_expiry_check_lambda.function_name
  principal     = "scheduler.amazonaws.com"
  source_arn    = aws_scheduler_schedule.qualification_expiry_check_daily.arn
}
