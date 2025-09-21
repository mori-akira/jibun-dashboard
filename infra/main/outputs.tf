output "login_url" {
  value = module.cognito.cognito_login_url
}

output "api_gateway_url" {
  value = module.api_gateway.apigw_url
}

output "apprunner_url" {
  value = module.apprunner.apprunner_service_url
}
