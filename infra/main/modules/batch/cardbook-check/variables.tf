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

variable "users_table_name" {
  description = "ユーザテーブル名"
  type        = string
}

variable "users_table_arn" {
  description = "ユーザテーブルARN"
  type        = string
}

variable "cardbook_cards_table_name" {
  description = "カードテーブル名"
  type        = string
}

variable "cardbook_cards_table_arn" {
  description = "カードテーブルARN"
  type        = string
}

variable "cardbook_check_results_table_name" {
  description = "カードチェック結果テーブル名"
  type        = string
}

variable "cardbook_check_results_table_arn" {
  description = "カードチェック結果テーブルARN"
  type        = string
}

variable "lambda_timeout_seconds" {
  description = "タイムアウト秒数"
  type        = number
  default     = 900
}

variable "lambda_memory_size" {
  description = "メモリサイズ(MB)"
  type        = number
  default     = 512
}

variable "bedrock_inference_profile_id" {
  description = "使用するBedrock推論プロファイルID"
  type        = string
  default     = "global.anthropic.claude-haiku-4-5-20251001-v1:0"
}

variable "bedrock_foundation_model_id" {
  description = "使用するBedrockファウンデーションモデルID"
  type        = string
  default     = "anthropic.claude-haiku-4-5-20251001-v1:0"
}

variable "timezone" {
  type    = string
  default = "Asia/Tokyo"
}
