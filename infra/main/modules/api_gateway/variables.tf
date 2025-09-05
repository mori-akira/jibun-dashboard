variable "region" {
  description = "リージョン"
  type        = string
}

variable "application_tag" {
  description = "自分ダッシュボード用のアプリケーションタグ"
  type        = map(string)
}

variable "frontend_bucket_name" {
  description = "フロントエンドのS3バケット名"
  type        = string
}

variable "user_pool_id" {
  description = "CognitoユーザープールID"
  type        = string
}

variable "user_pool_client_id" {
  description = "CognitoユーザープールクライアントID"
  type        = string
}

variable "apprunner_url" {
  description = "App RunnerのURL"
  type        = string
}
