variable "region" {
  description = "リージョン"
  type        = string
}

variable "app_name" {
  description = "アプリケーション名"
  type        = string
}

variable "env_name" {
  description = "環境名"
  type        = string
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_user_pool_id" {
  description = "CognitoユーザープールID"
  type        = string
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_client_id" {
  description = "CognitoクライアントID"
  type        = string
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_callback_url" {
  description = "CognitoコールバックURL"
  type        = string
  default     = "http://localhost:3000/callback"
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_logout_url" {
  description = "CognitoログアウトURL"
  type        = string
  default     = "http://localhost:3000/logout"
}
