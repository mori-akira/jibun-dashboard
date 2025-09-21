provider "aws" {
  region = var.region
}

resource "aws_s3_bucket" "tf_state" {
  bucket = var.tfstate_bucket_name
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
