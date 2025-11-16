output "queue_url" {
  description = "給与OCRバッチ SQS URL"
  value       = aws_sqs_queue.salary_ocr_queue.id
}

output "queue_arn" {
  description = "給与OCRバッチ SQS ARN"
  value       = aws_sqs_queue.salary_ocr_queue.arn
}

output "lambda_function_name" {
  description = "給与OCRバッチ Lambda関数名"
  value       = aws_lambda_function.salary_ocr_lambda.function_name
}
