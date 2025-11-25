variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "app_name" {
  description = "アプリケーション名"
  type        = string
}

variable "env_name" {
  description = "環境名"
  type        = string
}

variable "apprunner_service_arn" {
  description = "App RunnerサービスARN"
  type        = string
}

variable "lambda_timeout_seconds" {
  description = "タイムアウト秒数"
  type        = number
  default     = 30
}

variable "lambda_memory_size" {
  description = "メモリサイズ(MB)"
  type        = number
  default     = 256
}

variable "timezone" {
  description = "EventBridge Scheduler のタイムゾーン"
  type        = string
  default     = "Asia/Tokyo"
}
