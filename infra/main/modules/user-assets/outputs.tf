output "bucket_name" {
  value = aws_s3_bucket.user_assets.id
}

output "bucket_arn" {
  value = aws_s3_bucket.user_assets.arn
}
