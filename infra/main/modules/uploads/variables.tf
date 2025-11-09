variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "bucket_name" {
  description = "一時アップロード用バケット名"
  type        = string
}

variable "expire_after_days" {
  description = "一時ファイルの保持日数"
  type        = number
  default     = 7
  validation {
    condition     = var.expire_after_days >= 1 && floor(var.expire_after_days) == var.expire_after_days
    error_message = "expire_after_days must be an integer >= 1."
  }
}

variable "allowed_origins" {
  description = "CORS許可オリジン"
  type        = list(string)
}

variable "enable_force_tls_policy" {
  description = "TLS強制ポリシーフラグ"
  type        = bool
  default     = true
}
