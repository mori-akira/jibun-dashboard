data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "salary_ocr_lambda_role" {
  name               = "${var.app_name}-${var.env_name}-salary-ocr-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = var.application_tag
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.salary_ocr_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "aws_iam_policy_document" "salary_ocr_lambda_policy" {
  statement {
    sid    = "AllowReadFromSqs"
    effect = "Allow"
    actions = [
      "sqs:ReceiveMessage",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ChangeMessageVisibility"
    ]
    resources = [
      aws_sqs_queue.salary_ocr_queue.arn
    ]
  }

  statement {
    sid    = "AllowReadFromUploadsBucket"
    effect = "Allow"
    actions = [
      "s3:GetObject"
    ]
    resources = [
      "${var.uploads_bucket_arn}/*"
    ]
  }

  statement {
    sid    = "AllowUpdateSalaryTables"
    effect = "Allow"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]
    resources = [
      var.salaries_table_arn,
      var.salary_ocr_tasks_table_arn
    ]
  }

  statement {
    sid    = "AllowGetParameters"
    effect = "Allow"
    actions = [
      "ssm:GetParameter",
      "ssm:GetParameters"
    ]
    resources = [
      aws_ssm_parameter.openai_api_key.arn
    ]
  }
}

resource "aws_iam_role_policy" "salary_ocr_lambda_inline" {
  name   = "${var.app_name}-${var.env_name}-salary-ocr-lambda-policy"
  role   = aws_iam_role.salary_ocr_lambda_role.id
  policy = data.aws_iam_policy_document.salary_ocr_lambda_policy.json
}

data "archive_file" "salary_ocr_lambda_mock" {
  type        = "zip"
  source_file = "${path.module}/handler.py"
  output_path = "${path.module}/salary_ocr_lambda.zip"
}

resource "aws_lambda_function" "salary_ocr_lambda" {
  function_name = "${var.app_name}-${var.env_name}-salary-ocr"

  role             = aws_iam_role.salary_ocr_lambda_role.arn
  runtime          = "python3.12"
  handler          = "main.main"
  filename         = data.archive_file.salary_ocr_lambda_mock.output_path
  source_code_hash = data.archive_file.salary_ocr_lambda_mock.output_base64sha256
  timeout          = var.lambda_timeout_seconds
  memory_size      = var.lambda_memory_size
  tags             = var.application_tag

  environment {
    variables = {
      SALARY_OCR_QUEUE_URL        = aws_sqs_queue.salary_ocr_queue.id
      UPLOADS_BUCKET_NAME         = var.uploads_bucket_name
      SALARIES_TABLE_NAME         = var.salaries_table_name
      SALARY_OCR_TASKS_TABLE_NAME = var.salary_ocr_tasks_table_name
      OPENAI_API_KEY_SSM_NAME     = aws_ssm_parameter.openai_api_key.name
      OPENAI_MODEL                = var.openai_model
      OPENAI_OCR_MAX_ATTEMPTS     = var.openai_ocr_max_attempts
      SQS_WAIT_TIME_SECONDS       = var.sqs_wait_time_seconds
      SQS_VISIBILITY_TIMEOUT      = var.lambda_timeout_seconds + 60
    }
  }

  lifecycle {
    ignore_changes = [
      filename,
      source_code_hash,
    ]
  }
}
