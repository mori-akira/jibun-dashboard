variable "region" {
  description = "リージョン"
  type        = string
  default     = "ap-northeast-1"
}

variable "tfstate_bucket_name" {
  description = "Terraformのステート管理用のバケット名"
  type        = string
  default     = "jibun-dashboard-tf-state"
}

variable "tfstate_lock_table_name" {
  description = "Terraformステートロック用のDynamoDBテーブル名"
  type        = string
  default     = "jibun-dashboard-tf-lock"
}
