resource "aws_cognito_user_pool" "user_pool" {
  name = "jibun-dashboard-user-pool"

  tags = var.application_tag
}

resource "aws_cognito_user_pool_client" "user_pool_client" {
  name                                 = "frontend-client"
  user_pool_id                         = aws_cognito_user_pool.user_pool.id
  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH",
  ]
}

output "user_pool_id" {
  value = aws_cognito_user_pool.user_pool.id
}

output "user_pool_client_id" {
  value = aws_cognito_user_pool_client.user_pool_client_id
}
