<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_archive"></a> [archive](#provider\_archive) | n/a |
| <a name="provider_aws"></a> [aws](#provider\_aws) | n/a |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_apprunner_service.this](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/apprunner_service) | resource |
| [aws_cloudwatch_log_group.apprunner_application_log](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group) | resource |
| [aws_cloudwatch_log_group.apprunner_ops](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group) | resource |
| [aws_cloudwatch_log_group.apprunner_service_log](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudwatch_log_group) | resource |
| [aws_iam_policy.apprunner_lambda_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.apprunner_s3_uploads_bucket](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.dynamodb](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_policy.scheduler_invoke_lambda](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.apprunner_lambda_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.instance](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role.scheduler_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy.apprunner_ecr_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy) | resource |
| [aws_iam_role_policy_attachment.apprunner_lambda_attach](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.attach_dynamodb](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.attach_s3_uploads_bucket](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_iam_role_policy_attachment.scheduler_invoke_lambda_attach](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_lambda_function.apprunner_ops](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_function) | resource |
| [aws_lambda_permission.allow_scheduler_pause](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission) | resource |
| [aws_lambda_permission.allow_scheduler_resume](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lambda_permission) | resource |
| [aws_scheduler_schedule.pause_nightly](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/scheduler_schedule) | resource |
| [aws_scheduler_schedule.resume_morning](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/scheduler_schedule) | resource |
| [aws_scheduler_schedule_group.apprunner](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/scheduler_schedule_group) | resource |
| [archive_file.apprunner_lambda_zip](https://registry.terraform.io/providers/hashicorp/archive/latest/docs/data-sources/file) | data source |
| [aws_iam_policy_document.apprunner_dynamodb](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.apprunner_lambda_policy_doc](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.apprunner_s3_uploads_bucket](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.apprunner_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.lambda_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.scheduler_invoke_lambda_doc](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.scheduler_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | アプリケーション名 | `string` | n/a | yes |
| <a name="input_application_tag"></a> [application\_tag](#input\_application\_tag) | 自分ダッシュボード用のアプリケーションタグ | `map(string)` | n/a | yes |
| <a name="input_auto_deploy"></a> [auto\_deploy](#input\_auto\_deploy) | 自動デプロイ | `bool` | `true` | no |
| <a name="input_cognito_client_id"></a> [cognito\_client\_id](#input\_cognito\_client\_id) | CognitoクライアントID | `string` | n/a | yes |
| <a name="input_cognito_domain"></a> [cognito\_domain](#input\_cognito\_domain) | Cognitoドメイン | `string` | n/a | yes |
| <a name="input_cognito_user_pool_id"></a> [cognito\_user\_pool\_id](#input\_cognito\_user\_pool\_id) | CognitoユーザープールID | `string` | n/a | yes |
| <a name="input_container_port"></a> [container\_port](#input\_container\_port) | コンテナのポート | `number` | `8080` | no |
| <a name="input_cpu"></a> [cpu](#input\_cpu) | CPU | `number` | `256` | no |
| <a name="input_dynamodb_table_arns"></a> [dynamodb\_table\_arns](#input\_dynamodb\_table\_arns) | DynamoDBテーブルARNのリスト | `list(string)` | `[]` | no |
| <a name="input_ecr_repository_arn"></a> [ecr\_repository\_arn](#input\_ecr\_repository\_arn) | ECRリポジトリのARN | `string` | n/a | yes |
| <a name="input_ecr_repository_url"></a> [ecr\_repository\_url](#input\_ecr\_repository\_url) | ECRリポジトリのURL | `string` | n/a | yes |
| <a name="input_env_name"></a> [env\_name](#input\_env\_name) | 環境名 | `string` | n/a | yes |
| <a name="input_health_check_path"></a> [health\_check\_path](#input\_health\_check\_path) | ヘルスチェックのパス | `string` | `"/api/v1/actuator/health"` | no |
| <a name="input_image_tag"></a> [image\_tag](#input\_image\_tag) | ECRイメージのタグ | `string` | `"latest"` | no |
| <a name="input_memory"></a> [memory](#input\_memory) | メモリ | `number` | `512` | no |
| <a name="input_region"></a> [region](#input\_region) | AWSリージョン | `string` | n/a | yes |
| <a name="input_runtime_env"></a> [runtime\_env](#input\_runtime\_env) | 環境変数 | `map(string)` | `{}` | no |
| <a name="input_server_servlet_context_path"></a> [server\_servlet\_context\_path](#input\_server\_servlet\_context\_path) | サーブレットのコンテキストパス | `string` | `"/api/v1"` | no |
| <a name="input_timezone"></a> [timezone](#input\_timezone) | タイムゾーン | `string` | `"Asia/Tokyo"` | no |
| <a name="input_uploads_bucket_arn"></a> [uploads\_bucket\_arn](#input\_uploads\_bucket\_arn) | アップロード用S3バケットARN | `string` | n/a | yes |
| <a name="input_uploads_bucket_name"></a> [uploads\_bucket\_name](#input\_uploads\_bucket\_name) | アップロード用S3バケット名 | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_apprunner_service_arn"></a> [apprunner\_service\_arn](#output\_apprunner\_service\_arn) | n/a |
| <a name="output_apprunner_service_url"></a> [apprunner\_service\_url](#output\_apprunner\_service\_url) | n/a |
<!-- END_TF_DOCS -->