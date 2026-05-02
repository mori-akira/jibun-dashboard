data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "vocabulary_check_lambda_role" {
  name               = "${var.app_name}-${var.env_name}-vocabulary-check-lambda-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = var.application_tag
}

resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.vocabulary_check_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "aws_iam_policy_document" "vocabulary_check_lambda_policy_doc" {
  statement {
    sid = "ScanUsers"
    actions = [
      "dynamodb:Scan",
    ]
    resources = [var.users_table_arn]
  }

  statement {
    sid = "ReadVocabularies"
    actions = [
      "dynamodb:Query",
    ]
    resources = [
      var.vocabularies_table_arn,
      "${var.vocabularies_table_arn}/index/*",
    ]
  }

  statement {
    sid = "ReadVocabularyTags"
    actions = [
      "dynamodb:Query",
    ]
    resources = [
      var.vocabulary_tags_table_arn,
      "${var.vocabulary_tags_table_arn}/index/*",
    ]
  }

  statement {
    sid = "ReadWriteVocabularyCheckResults"
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:Query",
    ]
    resources = [
      var.vocabulary_check_results_table_arn,
      "${var.vocabulary_check_results_table_arn}/index/*",
    ]
  }

  statement {
    sid    = "AllowBedrockInvokeModel"
    effect = "Allow"
    actions = [
      "bedrock:InvokeModel"
    ]
    resources = [
      "arn:aws:bedrock:*::foundation-model/${var.bedrock_foundation_model_id}",
      "arn:aws:bedrock:*:*:inference-profile/${var.bedrock_inference_profile_id}",
    ]
  }
}

resource "aws_iam_role_policy" "vocabulary_check_lambda_inline" {
  name   = "${var.app_name}-${var.env_name}-vocabulary-check-lambda-policy"
  role   = aws_iam_role.vocabulary_check_lambda_role.id
  policy = data.aws_iam_policy_document.vocabulary_check_lambda_policy_doc.json
}

data "archive_file" "vocabulary_check_lambda_mock" {
  type        = "zip"
  source_file = "${path.module}/index.py"
  output_path = "${path.module}/index.zip"
}

resource "aws_lambda_function" "vocabulary_check_lambda" {
  function_name    = "${var.app_name}-${var.env_name}-vocabulary-check"
  role             = aws_iam_role.vocabulary_check_lambda_role.arn
  runtime          = "python3.12"
  handler          = "index.lambda_handler"
  filename         = data.archive_file.vocabulary_check_lambda_mock.output_path
  source_code_hash = data.archive_file.vocabulary_check_lambda_mock.output_base64sha256
  timeout          = var.lambda_timeout_seconds
  memory_size      = var.lambda_memory_size
  tags             = var.application_tag

  environment {
    variables = {
      USERS_TABLE_NAME                    = var.users_table_name
      VOCABULARIES_TABLE_NAME             = var.vocabularies_table_name
      VOCABULARY_TAGS_TABLE_NAME          = var.vocabulary_tags_table_name
      VOCABULARY_CHECK_RESULTS_TABLE_NAME = var.vocabulary_check_results_table_name
      BEDROCK_MODEL_ID                    = var.bedrock_inference_profile_id
    }
  }

  lifecycle {
    ignore_changes = [
      filename,
      source_code_hash,
    ]
  }
}
