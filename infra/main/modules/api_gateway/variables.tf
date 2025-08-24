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

variable "s3_bucket_arn" {
  description = "フロントエンドのS3 ARN"
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

# variable "resource_lambda_arn" {
#   description = "Resourceサービス用LambdaのARN"
#   type        = string
# }

# variable "timeout_lambda_call" {
#   description = "Lambda呼び出しのタイムアウト時間 (ミリ秒)"
#   type        = number
#   default     = 30000
# }
