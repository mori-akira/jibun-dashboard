output "lambda_function_name" {
  description = "ボキャブラリーチェックバッチ Lambda関数名"
  value       = aws_lambda_function.vocabulary_check_lambda.function_name
}
