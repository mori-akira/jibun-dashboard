data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "qualification_expiry_check_lambda_role" {
  name               = "${var.app_name}-${var.env_name}-qualification-expiry-check-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = var.application_tag
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.qualification_expiry_check_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "aws_iam_policy_document" "qualification_expiry_check_lambda_policy_doc" {
  statement {
    sid = "DynamoDB"
    actions = [
      "dynamodb:Scan",
      "dynamodb:UpdateItem",
    ]
    resources = [var.qualifications_table_arn]
  }
}

resource "aws_iam_role_policy" "qualification_expiry_check_lambda_inline" {
  name   = "${var.app_name}-${var.env_name}-qualification-expiry-check-lambda-policy"
  role   = aws_iam_role.qualification_expiry_check_lambda_role.id
  policy = data.aws_iam_policy_document.qualification_expiry_check_lambda_policy_doc.json
}

data "archive_file" "qualification_expiry_check_lambda_mock" {
  type        = "zip"
  source_file = "${path.module}/index.py"
  output_path = "${path.module}/index.zip"
}

resource "aws_lambda_function" "qualification_expiry_check_lambda" {
  function_name    = "${var.app_name}-${var.env_name}-qualification-expiry-check"
  role             = aws_iam_role.qualification_expiry_check_lambda_role.arn
  runtime          = "python3.12"
  handler          = "index.lambda_handler"
  filename         = data.archive_file.qualification_expiry_check_lambda_mock.output_path
  source_code_hash = data.archive_file.qualification_expiry_check_lambda_mock.output_base64sha256
  timeout          = var.lambda_timeout_seconds
  memory_size      = var.lambda_memory_size
  tags             = var.application_tag

  environment {
    variables = {
      QUALIFICATIONS_TABLE_NAME = var.qualifications_table_name
    }
  }

  lifecycle {
    ignore_changes = [
      filename,
      source_code_hash,
    ]
  }
}
