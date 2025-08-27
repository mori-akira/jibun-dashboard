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

variable "ecr_repository_url" {
  description = "ECRリポジトリのURL"
  type        = string
}

variable "ecr_repository_arn" {
  description = "ECRリポジトリのARN"
  type        = string
}

variable "image_tag" {
  description = "ECRイメージのタグ"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "コンテナのポート"
  type        = number
  default     = 8080
}

variable "cpu" {
  description = "CPU"
  type        = number
  default     = 256
}

variable "memory" {
  description = "メモリ"
  type        = number
  default     = 512
}

variable "health_check_path" {
  description = "ヘルスチェックのパス"
  type        = string
  default     = "/api/v1/actuator/health"
}

variable "auto_deploy" {
  description = "自動デプロイ"
  type        = bool
  default     = true
}

variable "timezone" {
  description = "タイムゾーン"
  type        = string
  default     = "Asia/Tokyo"
}
