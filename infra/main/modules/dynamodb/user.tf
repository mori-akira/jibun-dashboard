resource "aws_dynamodb_table" "user" {
  name         = "User"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "UserId"

  attribute {
    name = "UserId"
    type = "S"
  }

  tags = var.application_tag
}
