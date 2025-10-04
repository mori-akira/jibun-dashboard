variable "region" {
  description = "リージョン"
  type        = string
}

variable "project_name" {
  description = "プロジェクト名"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "github_url" {
  description = "GitHubリポジトリURL"
  type        = string
  validation {
    condition     = can(regex("^https://github.com/.+/.+\\.git$", var.github_url))
    error_message = "Invalid GitHub URL format."
  }
}

variable "build_timeout" {
  description = "ビルドタイムアウト"
  type        = number
  default     = 30
}

variable "queued_timeout" {
  description = "キューイングタイムアウト"
  type        = number
  default     = 30
}

variable "app_url" {
  description = "アプリのURL"
  type        = string
  validation {
    condition     = can(regex("^https?://", var.app_url))
    error_message = "Protocol must be http or https."
  }
}

variable "cognito_domain" {
  description = "Cognitoドメイン"
  type        = string
}

variable "username" {
  description = "E2Eテスト用のユーザ名"
  type        = string
}

variable "password" {
  description = "E2Eテスト用のパスワード"
  type        = string
  sensitive   = true
}

variable "enable_trigger_s3" {
  description = "S3更新→E2E起動の有効/無効"
  type        = bool
  default     = true
}

variable "enable_trigger_apprunner" {
  description = "App Runnerデプロイ成功→E2E起動の有効/無効"
  type        = bool
  default     = true
}

variable "s3_bucket_name" {
  description = "フロントエンドのS3バケット名"
  type        = string
}

variable "apprunner_service_arn" {
  description = "バックエンドのAppRunner Service ARN"
  type        = string
}

variable "alert_email" {
  description = "アラート通知先メールアドレス"
  type        = string
  validation {
    condition     = var.alert_email == null || can(regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", var.alert_email))
    error_message = "Invalid email address format."
  }
  default = null
}
