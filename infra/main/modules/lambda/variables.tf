variable "region" {
  description = "リージョン"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "function_name" {
  description = "Lambda関数名"
  type        = string
}

variable "service_name" {
  description = "サービス名"
  type        = string
}

variable "runtime" {
  description = "Lambdaランタイム"
  type        = string
  default     = "java21"
}

variable "memory_size" {
  description = "メモリサイズ"
  type        = number
  default     = 2048
}

variable "timeout" {
  description = "タイムアウト時間 (秒)"
  type        = number
  default     = 60
}

variable "environment" {
  type    = map(string)
  default = {}
}

variable "artifacts_bucket_name" {
  description = "Lambdaアーティファクト用のS3バケット名"
  type        = string
}
