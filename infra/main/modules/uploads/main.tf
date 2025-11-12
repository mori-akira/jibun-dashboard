resource "aws_s3_bucket" "uploads" {
  bucket = var.bucket_name
  tags   = var.application_tag
}

# バケットの公開ブロック
resource "aws_s3_bucket_public_access_block" "uploads" {
  bucket                  = aws_s3_bucket.uploads.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 暗号化
resource "aws_s3_bucket_server_side_encryption_configuration" "uploads" {
  bucket = aws_s3_bucket.uploads.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# TLS以外のアクセス拒否
resource "aws_s3_bucket_policy" "force_tls" {
  count  = var.enable_force_tls_policy ? 1 : 0
  bucket = aws_s3_bucket.uploads.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "DenyInsecureTransport",
        Effect    = "Deny",
        Principal = "*",
        Action    = "s3:*",
        Resource = [
          aws_s3_bucket.uploads.arn,
          "${aws_s3_bucket.uploads.arn}/*"
        ],
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })
}

# 自動削除
resource "aws_s3_bucket_lifecycle_configuration" "uploads" {
  count  = var.expire_after_days > 0 ? 1 : 0
  bucket = aws_s3_bucket.uploads.id
  rule {
    id     = "expire-temporary-objects"
    status = "Enabled"
    filter {}
    expiration {
      days = var.expire_after_days
    }
  }
}

# CORS設定
resource "aws_s3_bucket_cors_configuration" "uploads" {
  bucket = aws_s3_bucket.uploads.id
  cors_rule {
    allowed_methods = ["PUT", "HEAD", "GET"]
    allowed_origins = var.allowed_origins
    allowed_headers = ["*"]
    expose_headers  = []
    max_age_seconds = 3000
  }
}
