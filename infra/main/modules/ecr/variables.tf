variable "app_name" {
  description = "アプリ名"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "keep_count" {
  description = "保持するイメージの数"
  type        = number
  default     = 5
}
