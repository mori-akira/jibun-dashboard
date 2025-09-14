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
}

variable "access_token_validity" {
  description = "アクセストークンの有効期間（時間）"
  type        = number
  default     = 24
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
