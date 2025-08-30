resource "aws_dynamodb_table" "this" {
  name         = var.table_name
  billing_mode = var.billing_mode

  hash_key  = var.hash_key.name
  range_key = var.sort_key == null ? null : var.sort_key.name

  attribute {
    name = var.hash_key.name
    type = var.hash_key.type
  }

  dynamic "attribute" {
    for_each = var.sort_key == null ? [] : [var.sort_key]
    content {
      name = attribute.value.name
      type = attribute.value.type
    }
  }

  tags = var.application_tag
}

output "table_name" {
  value = aws_dynamodb_table.this.name
}

output "table_arn" {
  value = aws_dynamodb_table.this.arn
}
