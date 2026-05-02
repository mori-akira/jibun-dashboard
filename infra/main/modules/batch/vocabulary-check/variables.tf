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

variable "vocabularies_table_name" {
  description = "ボキャブラリーテーブル名"
  type        = string
}

variable "vocabularies_table_arn" {
  description = "ボキャブラリーテーブルARN"
  type        = string
}

variable "vocabulary_tags_table_name" {
  description = "ボキャブラリータグテーブル名"
  type        = string
}

variable "vocabulary_tags_table_arn" {
  description = "ボキャブラリータグテーブルARN"
  type        = string
}

variable "vocabulary_check_results_table_name" {
  description = "ボキャブラリーチェック結果テーブル名"
  type        = string
}

variable "vocabulary_check_results_table_arn" {
  description = "ボキャブラリーチェック結果テーブルARN"
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
  default     = "global.anthropic.claude-sonnet-4-6"
}

variable "bedrock_foundation_model_id" {
  description = "使用するBedrockファウンデーションモデルID"
  type        = string
  default     = "anthropic.claude-sonnet-4-6"
}

variable "timezone" {
  type    = string
  default = "Asia/Tokyo"
}
