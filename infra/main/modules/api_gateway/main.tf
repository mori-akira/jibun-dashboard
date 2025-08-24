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

# resource "aws_apigatewayv2_integration" "resource_lambda_integration" {
#   api_id                 = aws_apigatewayv2_api.http_api.id
#   integration_type       = "AWS_PROXY"
#   integration_uri        = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${var.resource_lambda_arn}/invocations"
#   payload_format_version = "2.0"
#   timeout_milliseconds   = var.timeout_lambda_call
# }

# resource "aws_apigatewayv2_authorizer" "cognito_auth" {
#   name             = "CognitoAuth"
#   api_id           = aws_apigatewayv2_api.http_api.id
#   authorizer_type  = "JWT"
#   identity_sources = ["$request.header.Authorization"]
#   jwt_configuration {
#     audience = [var.user_pool_client_id]
#     issuer   = "https://cognito-idp.${var.region}.amazonaws.com/${var.user_pool_id}"
#   }
# }

resource "aws_apigatewayv2_route" "default_route" {
  api_id             = aws_apigatewayv2_api.http_api.id
  route_key          = "$default"
  target             = "integrations/${aws_apigatewayv2_integration.frontend_integration.id}"
  authorization_type = "NONE"
}

# resource "aws_apigatewayv2_route" "resource_root" {
#   api_id    = aws_apigatewayv2_api.http_api.id
#   route_key = "ANY /api/v1/resource"
#   target    = "integrations/${aws_apigatewayv2_integration.resource_lambda_integration.id}"
# }

# resource "aws_apigatewayv2_route" "resource_proxy" {
#   api_id    = aws_apigatewayv2_api.http_api.id
#   route_key = "ANY /api/v1/resource/{proxy+}"
#   target    = "integrations/${aws_apigatewayv2_integration.resource_lambda_integration.id}"
# }

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http_api.id
  name        = "$default"
  auto_deploy = true

  default_route_settings {
    logging_level          = "OFF"
    throttling_rate_limit  = 1000
    throttling_burst_limit = 500
  }
}

# resource "aws_lambda_permission" "allow_apigw_invoke_resource" {
#   statement_id  = "AllowAPIGatewayInvokeResource"
#   action        = "lambda:InvokeFunction"
#   function_name = var.resource_lambda_arn
#   principal     = "apigateway.amazonaws.com"
#   source_arn    = "${aws_apigatewayv2_api.http_api.execution_arn}/*"
# }

output "apigw_url" {
  value = aws_apigatewayv2_api.http_api.api_endpoint
}
