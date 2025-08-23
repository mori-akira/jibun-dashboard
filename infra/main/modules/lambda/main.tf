data "aws_iam_policy_document" "assume" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "lambda_exec" {
  name               = "${var.function_name}-exec"
  assume_role_policy = data.aws_iam_policy_document.assume.json
  tags               = var.application_tag
}

resource "aws_iam_role_policy_attachment" "basic_logs" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "this" {
  function_name = var.function_name
  role          = aws_iam_role.lambda_exec.arn

  s3_bucket = var.artifacts_bucket_name
  s3_key    = "${var.service_name}.jar"

  runtime     = var.runtime
  handler     = "com.github.moriakira.jibundashboard.${var.service_name}.StreamLambdaHandler::handleRequest"
  memory_size = var.memory_size
  timeout     = var.timeout

  environment {
    variables = merge(var.environment, {
      MAIN_CLASS                       = "com.github.moriakira.jibundashboard.${var.service_name}.LambdaApplication",
      JAVA_TOOL_OPTIONS                = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1",
      SPRING_MAIN_WEB_APPLICATION_TYPE = "servlet"
    })
  }
  lifecycle {
    # コード関連の変更はTerraformで無視
    ignore_changes = [
      source_code_hash,
      filename,
      s3_key,
      s3_object_version,
      image_uri
    ]
  }

  tags = var.application_tag
}

resource "aws_cloudwatch_log_group" "lambda_log" {
  name              = "/aws/lambda/${aws_lambda_function.this.function_name}"
  retention_in_days = 14
  tags              = var.application_tag
}

output "function_arn" {
  value = aws_lambda_function.this.arn
}

output "function_name" {
  value = aws_lambda_function.this.function_name
}
