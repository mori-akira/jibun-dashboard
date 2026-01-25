resource "aws_s3_bucket" "user_assets" {
  bucket = var.bucket_name
  tags   = var.application_tag
}

# バケットの公開ブロック
resource "aws_s3_bucket_public_access_block" "user_assets" {
  bucket                  = aws_s3_bucket.user_assets.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 暗号化
resource "aws_s3_bucket_server_side_encryption_configuration" "user_assets" {
  bucket = aws_s3_bucket.user_assets.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# TLS以外のアクセス拒否
resource "aws_s3_bucket_policy" "force_tls" {
  count  = var.enable_force_tls_policy ? 1 : 0
  bucket = aws_s3_bucket.user_assets.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "DenyInsecureTransport",
        Effect    = "Deny",
        Principal = "*",
        Action    = "s3:*",
        Resource = [
          aws_s3_bucket.user_assets.arn,
          "${aws_s3_bucket.user_assets.arn}/*"
        ],
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })
}

# CORS設定（必要な場合のみ）
resource "aws_s3_bucket_cors_configuration" "user_assets" {
  count  = length(var.allowed_origins) > 0 ? 1 : 0
  bucket = aws_s3_bucket.user_assets.id
  cors_rule {
    allowed_methods = ["PUT", "HEAD", "GET"]
    allowed_origins = var.allowed_origins
    allowed_headers = ["*"]
    expose_headers  = []
    max_age_seconds = 3000
  }
}
