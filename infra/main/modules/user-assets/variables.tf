variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "bucket_name" {
  description = "ユーザアセット管理用バケット名"
  type        = string
}

variable "allowed_origins" {
  description = "CORS許可オリジン（空ならCORS設定しない）"
  type        = list(string)
  default     = []
}

variable "enable_force_tls_policy" {
  description = "TLS強制ポリシーフラグ"
  type        = bool
  default     = true
}
