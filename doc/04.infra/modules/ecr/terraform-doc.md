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
| [aws_ecr_lifecycle_policy.keep_latest](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_lifecycle_policy) | resource |
| [aws_ecr_repository.this](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | アプリ名 | `string` | n/a | yes |
| <a name="input_application_tag"></a> [application\_tag](#input\_application\_tag) | 自分ダッシュボード用のアプリケーションタグ | `map(string)` | n/a | yes |
| <a name="input_keep_count"></a> [keep\_count](#input\_keep\_count) | 保持するイメージの数 | `number` | `5` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_repository_url"></a> [repository\_url](#output\_repository\_url) | n/a |
<!-- END_TF_DOCS -->