variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "table_name" {
  description = "テーブル名"
  type        = string
}

variable "hash_key" {
  description = "ハッシュキー定義"
  type = object({
    name = string
    type = string
  })
}

variable "sort_key" {
  description = "ソートキー定義"
  type = object({
    name = string
    type = string
  })
  default = null
}

variable "billing_mode" {
  description = "課金モード"
  type        = string
  default     = "PAY_PER_REQUEST"
}
