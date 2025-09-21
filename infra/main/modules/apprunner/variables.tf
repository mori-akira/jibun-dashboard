variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "region" {
  description = "AWSリージョン"
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
  validation {
    condition     = var.container_port >= 1 && var.container_port <= 65535 && floor(var.container_port) == var.container_port
    error_message = "container_port must be an integer between 1 and 65535."
  }
}

variable "server_servlet_context_path" {
  description = "サーブレットのコンテキストパス"
  type        = string
  default     = "/api/v1"
  validation {
    condition     = can(regex("^/", var.server_servlet_context_path))
    error_message = "server_servlet_context_path must start with '/'."
  }
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
  validation {
    condition     = can(regex("^/", var.health_check_path))
    error_message = "health_check_path must start with '/'."
  }
}

variable "auto_deploy" {
  description = "自動デプロイ"
  type        = bool
  default     = true
}

variable "runtime_env" {
  description = "環境変数"
  type        = map(string)
  default     = {}
}

variable "timezone" {
  description = "タイムゾーン"
  type        = string
  default     = "Asia/Tokyo"
}

variable "dynamodb_table_arns" {
  type        = list(string)
  description = "DynamoDBテーブルARNのリスト"
  default     = []
}
