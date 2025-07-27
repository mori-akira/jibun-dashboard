resource "aws_cognito_user_pool" "user_pool" {
  name = "jibun-dashboard-user-pool"
  tags = var.application_tag
}

resource "aws_cognito_user_pool_client" "user_pool_client" {
  name                                 = "frontend-client"
  user_pool_id                         = aws_cognito_user_pool.user_pool.id
  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["openid"]

  callback_urls = [var.cognito_callback_url]
  logout_urls   = [var.cognito_logout_url]

  supported_identity_providers = ["COGNITO"]
  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
  ]
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = var.domain_name
  user_pool_id = aws_cognito_user_pool.user_pool.id
}

output "user_pool_id" {
  value = aws_cognito_user_pool.user_pool.id
}

output "user_pool_client_id" {
  value = aws_cognito_user_pool_client.user_pool_client.id
}

output "cognito_login_url" {
  value = "https://${aws_cognito_user_pool_domain.domain.domain}.auth.${var.region}.amazoncognito.com/login?response_type=token&client_id=${aws_cognito_user_pool_client.user_pool_client.id}&redirect_uri=http://localhost:3000/callback"
}
