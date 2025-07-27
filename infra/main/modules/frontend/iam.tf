resource "aws_iam_role" "apigw_s3_invoke_role_default" {
  name = "apigw-s3-invoke-role-default"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "apigateway.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "apigw_s3_policy_default" {
  name = "apigw-s3-access-default"
  role = aws_iam_role.apigw_s3_invoke_role_default.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.frontend.arn,
          "${aws_s3_bucket.frontend.arn}/*"
        ]
      }
    ]
  })
}

output "apigw_invoke_role_arn" {
  value = aws_iam_role.apigw_s3_invoke_role_default.arn
}
