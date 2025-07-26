resource "aws_apigatewayv2_api" "http_api" {
  name          = "jibun-dashboard-http-api"
  protocol_type = "HTTP"
  tags          = var.application_tag
}

resource "aws_apigatewayv2_integration" "frontend_integration" {
  api_id             = aws_apigatewayv2_api.http_api.id
  integration_type   = "HTTP_PROXY"
  integration_method = "GET"
  integration_uri    = "http://${var.frontend_bucket_name}.s3-website-${var.region}.amazonaws.com"
}

resource "aws_apigatewayv2_route" "default_route" {
  api_id             = aws_apigatewayv2_api.http_api.id
  route_key          = "$default"
  target             = "integrations/${aws_apigatewayv2_integration.frontend_integration.id}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito_auth.id
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true

  default_route_settings {
    logging_level = "OFF"
  }
}

resource "aws_apigatewayv2_authorizer" "cognito_auth" {
  name             = "CognitoAuth"
  api_id           = aws_apigatewayv2_api.http_api.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  jwt_configuration {
    audience = [var.user_pool_client_id]
    issuer   = "https://cognito-idp.${var.region}.amazonaws.com/${var.user_pool_id}"
  }
}

output "apigw_url" {
  value = aws_apigatewayv2_api.http_api.api_endpoint
}
