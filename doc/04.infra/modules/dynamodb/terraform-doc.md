<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | n/a |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_dynamodb_table.this](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/dynamodb_table) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_application_tag"></a> [application\_tag](#input\_application\_tag) | 自分ダッシュボード用のアプリケーションタグ | `map(string)` | n/a | yes |
| <a name="input_billing_mode"></a> [billing\_mode](#input\_billing\_mode) | 課金モード | `string` | `"PAY_PER_REQUEST"` | no |
| <a name="input_global_secondary_indexes"></a> [global\_secondary\_indexes](#input\_global\_secondary\_indexes) | GSI | <pre>list(object({<br/>    name               = string<br/>    hash_key_name      = string<br/>    range_key_name     = string<br/>    projection_type    = string<br/>    non_key_attributes = optional(list(string))<br/>  }))</pre> | `[]` | no |
| <a name="input_hash_key"></a> [hash\_key](#input\_hash\_key) | ハッシュキー | <pre>object({<br/>    name = string<br/>    type = string<br/>  })</pre> | n/a | yes |
| <a name="input_sort_key"></a> [sort\_key](#input\_sort\_key) | ソートキー | <pre>object({<br/>    name = string<br/>    type = string<br/>  })</pre> | `null` | no |
| <a name="input_table_name"></a> [table\_name](#input\_table\_name) | テーブル名 | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_table_arn"></a> [table\_arn](#output\_table\_arn) | n/a |
| <a name="output_table_name"></a> [table\_name](#output\_table\_name) | n/a |
<!-- END_TF_DOCS -->