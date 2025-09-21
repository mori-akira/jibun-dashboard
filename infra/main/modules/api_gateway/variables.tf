variable "region" {
  description = "リージョン"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "apigateway_logging_enabled" {
  type    = bool
  default = false
}

variable "frontend_bucket_name" {
  description = "フロントエンドのS3バケット名"
  type        = string
}

variable "user_pool_id" {
  description = "CognitoユーザープールID"
  type        = string
}

variable "user_pool_client_id" {
  description = "CognitoユーザープールクライアントID"
  type        = string
}

variable "apprunner_url" {
  description = "App RunnerのURL"
  type        = string
  validation {
    condition     = can(regex("^https?://", var.apprunner_url))
    error_message = "Protocol must be http or https."
  }
}

variable "apprunner_context_path" {
  description = "App Runnerのコンテキストパス"
  type        = string
  default     = "/api/v1"
  validation {
    condition     = can(regex("^/", var.apprunner_context_path))
    error_message = "apprunner_context_path must start with '/'."
  }
}
