output "lambda_function_name" {
  description = "App Runner Ops Lambda関数名"
  value       = aws_lambda_function.apprunner_ops_lambda.function_name
}
