output "project_name" {
  value = aws_codebuild_project.this.name
}

output "artifacts_bucket" {
  value = aws_s3_bucket.artifacts.bucket
}
