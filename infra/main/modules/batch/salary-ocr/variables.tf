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

variable "uploads_bucket_name" {
  description = "アップロード用S3バケット名"
  type        = string
}

variable "uploads_bucket_arn" {
  description = "アップロード用S3バケットARN"
  type        = string
}

variable "salaries_table_name" {
  description = "給与テーブル名"
  type        = string
}

variable "salaries_table_arn" {
  description = "給与テーブルARN"
  type        = string
}

variable "salary_ocr_tasks_table_name" {
  description = "給与OCRタスクテーブル名"
  type        = string
}

variable "salary_ocr_tasks_table_arn" {
  description = "給与OCRタスクテーブルARN"
  type        = string
}

variable "lambda_timeout_seconds" {
  description = "タイムアウト秒数"
  type        = number
  default     = 300
}

variable "lambda_memory_size" {
  description = "メモリサイズ(MB)"
  type        = number
  default     = 1024
}

variable "openai_api_key" {
  description = "OpenAIのAPIキー"
  type        = string
  sensitive   = true
}

variable "openai_model" {
  description = "使用するOpenAIモデル"
  type        = string
  default     = "gpt-5.1"
}

variable "openai_ocr_max_attempts" {
  description = "OpenAI OCRの最大試行回数"
  type        = number
  default     = 3
}
