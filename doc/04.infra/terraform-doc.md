<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.12.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_api_gateway"></a> [api\_gateway](#module\_api\_gateway) | ./modules/api_gateway | n/a |
| <a name="module_application"></a> [application](#module\_application) | ./modules/application | n/a |
| <a name="module_apprunner"></a> [apprunner](#module\_apprunner) | ./modules/apprunner | n/a |
| <a name="module_cognito"></a> [cognito](#module\_cognito) | ./modules/cognito | n/a |
| <a name="module_dynamodb_qualifications"></a> [dynamodb\_qualifications](#module\_dynamodb\_qualifications) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_resources_i18n"></a> [dynamodb\_resources\_i18n](#module\_dynamodb\_resources\_i18n) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_salaries"></a> [dynamodb\_salaries](#module\_dynamodb\_salaries) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_settings"></a> [dynamodb\_settings](#module\_dynamodb\_settings) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_users"></a> [dynamodb\_users](#module\_dynamodb\_users) | ./modules/dynamodb | n/a |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | ./modules/ecr | n/a |
| <a name="module_frontend"></a> [frontend](#module\_frontend) | ./modules/frontend | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_iam_policy.apprunner_dynamodb_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_policy) | resource |
| [aws_iam_role.apprunner_instance_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.apprunner_instance_attach](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/caller_identity) | data source |
| [aws_iam_policy_document.apprunner_dynamodb_policy_doc](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |
| [aws_iam_policy_document.apprunner_instance_trust](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/iam_policy_document) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | アプリケーション名 | `string` | n/a | yes |
| <a name="input_cognito_callback_url"></a> [cognito\_callback\_url](#input\_cognito\_callback\_url) | CognitoコールバックURL | `string` | `"http://localhost:3000/callback"` | no |
| <a name="input_cognito_client_id"></a> [cognito\_client\_id](#input\_cognito\_client\_id) | CognitoクライアントID | `string` | n/a | yes |
| <a name="input_cognito_logout_url"></a> [cognito\_logout\_url](#input\_cognito\_logout\_url) | CognitoログアウトURL | `string` | `"http://localhost:3000/logout"` | no |
| <a name="input_cognito_user_pool_id"></a> [cognito\_user\_pool\_id](#input\_cognito\_user\_pool\_id) | CognitoユーザープールID | `string` | n/a | yes |
| <a name="input_env_name"></a> [env\_name](#input\_env\_name) | 環境名 | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | リージョン | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_api_gateway_url"></a> [api\_gateway\_url](#output\_api\_gateway\_url) | n/a |
| <a name="output_apprunner_url"></a> [apprunner\_url](#output\_apprunner\_url) | n/a |
| <a name="output_login_url"></a> [login\_url](#output\_login\_url) | n/a |
<!-- END_TF_DOCS -->