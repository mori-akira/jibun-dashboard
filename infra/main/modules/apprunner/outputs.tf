output "apprunner_service_arn" {
  value = aws_apprunner_service.this.arn
}

output "apprunner_service_url" {
  value = aws_apprunner_service.this.service_url
}

output "origin_verify_secret" {
  value     = random_password.origin_verify.result
  sensitive = true
}
