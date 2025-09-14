variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "table_name" {
  description = "テーブル名"
  type        = string
}

variable "hash_key" {
  description = "ハッシュキー"
  type = object({
    name = string
    type = string
  })
}

variable "sort_key" {
  description = "ソートキー"
  type = object({
    name = string
    type = string
  })
  default = null
}

variable "global_secondary_indexes" {
  description = "GSI"
  type = list(object({
    name               = string
    hash_key_name      = string
    hash_key_type      = string
    range_key_name     = optional(string)
    range_key_type     = optional(string)
    projection_type    = string
    non_key_attributes = optional(list(string))
  }))
  default = []
}

variable "local_secondary_indexes" {
  description = "LSI"
  type = list(object({
    name               = string
    range_key_name     = string
    range_key_type     = string
    projection_type    = string
    non_key_attributes = optional(list(string))
  }))
  default = []
}

variable "billing_mode" {
  description = "課金モード"
  type        = string
  default     = "PAY_PER_REQUEST"
}
