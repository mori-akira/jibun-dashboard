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
  validation {
    condition     = var.keep_count >= 1 && floor(var.keep_count) == var.keep_count
    error_message = "keep_count must be an integer greater than or equal to 1."
  }
}
