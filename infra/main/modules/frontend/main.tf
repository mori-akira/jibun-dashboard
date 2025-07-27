resource "aws_s3_bucket" "frontend" {
  bucket = var.frontend_bucket_name
  tags   = var.application_tag
}

resource "aws_s3_bucket_policy" "frontend_policy" {
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid : "AllowAPIGatewayInvokeViaRole",
      Effect : "Allow",
      Principal : { AWS = aws_iam_role.apigw_s3_invoke_role_default.arn },
      Action : ["s3:GetObject", "s3:ListBucket"],
      Resource = [
        aws_s3_bucket.frontend.arn,
        "${aws_s3_bucket.frontend.arn}/*"
      ]
    }]
  })
}

resource "aws_s3_bucket_website_configuration" "frontend_website" {
  bucket = aws_s3_bucket.frontend.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "index.html"
  }
}

resource "aws_s3_bucket_versioning" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  versioning_configuration {
    status = "Suspended"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = aws_s3_bucket.frontend.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

output "bucket_name" {
  value = aws_s3_bucket.frontend.id
}

output "bucket_arn" {
  value = aws_s3_bucket.frontend.arn
}
