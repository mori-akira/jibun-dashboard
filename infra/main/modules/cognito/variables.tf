variable "region" {
  description = "リージョン"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "domain_name" {
  description = "ドメイン名"
  type        = string
}

variable "id_token_validity" {
  description = "IDトークンの有効期間（時間）"
  type        = number
  default     = 24
  validation {
    condition     = var.id_token_validity >= 1 && floor(var.id_token_validity) == var.id_token_validity
    error_message = "id_token_validity must be an integer greater than or equal to 1."
  }
}

variable "access_token_validity" {
  description = "アクセストークンの有効期間（時間）"
  type        = number
  default     = 24
  validation {
    condition     = var.access_token_validity >= 1 && floor(var.access_token_validity) == var.access_token_validity
    error_message = "access_token_validity must be an integer greater than or equal to 1."
  }
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
    condition     = can(regex("^https?://", var.cognito_logout_url))
    error_message = "Protocol must be http or https."
  }
}
