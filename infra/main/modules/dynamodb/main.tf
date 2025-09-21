resource "aws_dynamodb_table" "this" {
  name                        = var.table_name
  billing_mode                = var.billing_mode
  deletion_protection_enabled = true

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

  dynamic "attribute" {
    for_each = var.global_secondary_indexes
    content {
      name = attribute.value.hash_key_name
      type = attribute.value.hash_key_type
    }
  }

  dynamic "attribute" {
    for_each = [
      for g in var.global_secondary_indexes : g
      if try(g.range_key_name, null) != null
    ]
    content {
      name = attribute.value.range_key_name
      type = attribute.value.range_key_type
    }
  }

  dynamic "attribute" {
    for_each = var.local_secondary_indexes
    content {
      name = attribute.value.range_key_name
      type = attribute.value.range_key_type
    }
  }

  dynamic "global_secondary_index" {
    for_each = var.global_secondary_indexes
    content {
      name               = global_secondary_index.value.name
      hash_key           = global_secondary_index.value.hash_key_name
      range_key          = try(global_secondary_index.value.range_key_name, null)
      projection_type    = global_secondary_index.value.projection_type
      non_key_attributes = try(global_secondary_index.value.non_key_attributes, null)
    }
  }

  dynamic "local_secondary_index" {
    for_each = var.local_secondary_indexes
    content {
      name               = local_secondary_index.value.name
      range_key          = local_secondary_index.value.range_key_name
      projection_type    = local_secondary_index.value.projection_type
      non_key_attributes = try(local_secondary_index.value.non_key_attributes, null)
    }
  }

  tags = var.application_tag
}
