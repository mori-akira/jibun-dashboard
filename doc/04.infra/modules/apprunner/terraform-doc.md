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
| [aws_apprunner_service.this](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/apprunner_service) | resource |
| [aws_iam_policy.scheduler_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.apprunner_ecr_access](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.scheduler_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.apprunner_ecr_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy) | resource |
| [aws_iam_role_policy_attachment.attach](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_scheduler_schedule.pause_nightly](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/scheduler_schedule) | resource |
| [aws_scheduler_schedule.resume_morning](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/scheduler_schedule) | resource |
| [aws_iam_policy_document.apprunner_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.scheduler_policy_doc](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.scheduler_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | アプリケーション名 | `string` | n/a | yes |
| <a name="input_application_tag"></a> [application\_tag](#input\_application\_tag) | 自分ダッシュボード用のアプリケーションタグ | `map(string)` | n/a | yes |
| <a name="input_auto_deploy"></a> [auto\_deploy](#input\_auto\_deploy) | 自動デプロイ | `bool` | `true` | no |
| <a name="input_container_port"></a> [container\_port](#input\_container\_port) | コンテナのポート | `number` | `8080` | no |
| <a name="input_cpu"></a> [cpu](#input\_cpu) | CPU | `number` | `512` | no |
| <a name="input_ecr_repository_arn"></a> [ecr\_repository\_arn](#input\_ecr\_repository\_arn) | ECRリポジトリのARN | `string` | n/a | yes |
| <a name="input_ecr_repository_url"></a> [ecr\_repository\_url](#input\_ecr\_repository\_url) | ECRリポジトリのURL | `string` | n/a | yes |
| <a name="input_env_name"></a> [env\_name](#input\_env\_name) | 環境名 | `string` | n/a | yes |
| <a name="input_health_check_path"></a> [health\_check\_path](#input\_health\_check\_path) | ヘルスチェックのパス | `string` | `"/api/v1/actuator/health"` | no |
| <a name="input_image_tag"></a> [image\_tag](#input\_image\_tag) | ECRイメージのタグ | `string` | `"latest"` | no |
| <a name="input_memory"></a> [memory](#input\_memory) | メモリ | `number` | `1024` | no |
| <a name="input_timezone"></a> [timezone](#input\_timezone) | タイムゾーン | `string` | `"Asia/Tokyo"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_apprunner_service_arn"></a> [apprunner\_service\_arn](#output\_apprunner\_service\_arn) | n/a |
| <a name="output_apprunner_service_url"></a> [apprunner\_service\_url](#output\_apprunner\_service\_url) | n/a |
<!-- END_TF_DOCS -->