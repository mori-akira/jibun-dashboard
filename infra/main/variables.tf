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
  validation {
    condition     = contains(["dev", "stg", "prod"], var.env_name)
    error_message = "env_name must be one of 'dev', 'stg', or 'prod'."
  }
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
variable "cognito_domain" {
  description = "Cognitoドメイン"
  type        = string
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_callback_url" {
  description = "CognitoコールバックURL"
  type        = string
  default     = "http://localhost:3000/callback"
  validation {
    condition     = can(regex("^https?://", var.cognito_callback_url))
    error_message = "Protocol must be http or https."
  }
}

# 循環参照を避けるため、直接指定が必要
variable "cognito_logout_url" {
  description = "CognitoログアウトURL"
  type        = string
  default     = "http://localhost:3000/logout"
  validation {
    condition     = can(regex("^https?://", var.cognito_callback_url))
    error_message = "Protocol must be http or https."
  }
}
