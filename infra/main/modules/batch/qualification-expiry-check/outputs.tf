output "lambda_function_name" {
  description = "資格期限確認バッチ Lambda関数名"
  value       = aws_lambda_function.qualification_expiry_check_lambda.function_name
}
