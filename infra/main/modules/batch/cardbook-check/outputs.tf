output "lambda_function_name" {
  description = "カードチェックバッチ Lambda関数名"
  value       = aws_lambda_function.cardbook_check_lambda.function_name
}
