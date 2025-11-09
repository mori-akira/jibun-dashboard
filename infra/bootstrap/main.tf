provider "aws" {
  region = var.region
}

resource "aws_s3_bucket" "tf_state" {
  bucket        = var.tfstate_bucket_name
  force_destroy = false
  tags = {
    awsApplication = "arn:aws:resource-groups:ap-northeast-1:851725542725:group/jibun-dashboard/0b1mcm829tvccvg6lxmbxpevik"
  }
}

resource "aws_s3_bucket_public_access_block" "tf_state" {
  bucket                  = aws_s3_bucket.tf_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "tf_state_tls_only" {
  bucket = aws_s3_bucket.tf_state.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "DenyInsecureTransport",
        Effect    = "Deny",
        Principal = "*",
        Action    = "s3:*",
        Resource = [
          aws_s3_bucket.tf_state.arn,
          "${aws_s3_bucket.tf_state.arn}/*"
        ],
        Condition = {
          Bool = { "aws:SecureTransport" = "false" }
        }
      }
    ]
  })
}

resource "aws_dynamodb_table" "tf_lock" {
  name                        = var.tfstate_lock_table_name
  billing_mode                = "PAY_PER_REQUEST"
  deletion_protection_enabled = true

  hash_key = "LockID"
  attribute {
    name = "LockID"
    type = "S"
  }
}
