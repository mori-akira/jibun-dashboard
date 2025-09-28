output "login_url" {
  value = module.cognito.cognito_login_url
}

output "apigateway_url" {
  value = module.apigateway.apigw_url
}

output "apprunner_url" {
  value = module.apprunner.apprunner_service_url
}
