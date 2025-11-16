resource "aws_sqs_queue" "salary_ocr_dlq" {
  name                       = "${var.app_name}-${var.env_name}-salary-ocr-dlq"
  message_retention_seconds  = 1209600 # 14日
  visibility_timeout_seconds = 60
  tags                       = var.application_tag
}

resource "aws_sqs_queue" "salary_ocr_queue" {
  name                       = "${var.app_name}-${var.env_name}-salary-ocr-queue"
  visibility_timeout_seconds = var.lambda_timeout_seconds + 60
  tags                       = var.application_tag

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.salary_ocr_dlq.arn
    maxReceiveCount     = 3
  })

}

resource "aws_lambda_event_source_mapping" "salary_ocr_mapping" {
  event_source_arn                   = aws_sqs_queue.salary_ocr_queue.arn
  function_name                      = aws_lambda_function.salary_ocr_lambda.arn
  batch_size                         = 1
  maximum_batching_window_in_seconds = 0
  enabled                            = true
}
