resource "aws_s3_bucket" "artifacts" {
  bucket = var.artifact_bucket_name
  tags   = var.application_tag
}

resource "aws_s3_bucket_versioning" "this" {
  bucket = aws_s3_bucket.artifacts.id

  versioning_configuration {
    status = "Suspended"
  }
}

resource "aws_s3_bucket_public_access_block" "this" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "this" {
  bucket = aws_s3_bucket.artifacts.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

locals {
  bucket_arn = aws_s3_bucket.artifacts.arn

  base_statements = [
    # 非TLSアクセスの一括拒否
    {
      Sid       = "DenyInsecureTransport"
      Effect    = "Deny"
      Principal = "*"
      Action    = "s3:*"
      Resource  = [local.bucket_arn, "${local.bucket_arn}/*"]
      Condition = { Bool = { "aws:SecureTransport" = "false" } }
    }
  ]
}

resource "aws_s3_bucket_policy" "this" {
  bucket = aws_s3_bucket.artifacts.id
  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = local.base_statements
  })
}
