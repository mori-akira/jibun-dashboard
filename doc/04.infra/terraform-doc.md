<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.11.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_api_gateway"></a> [api\_gateway](#module\_api\_gateway) | ./modules/api_gateway | n/a |
| <a name="module_application"></a> [application](#module\_application) | ./modules/application | n/a |
| <a name="module_apprunner"></a> [apprunner](#module\_apprunner) | ./modules/apprunner | n/a |
| <a name="module_cognito"></a> [cognito](#module\_cognito) | ./modules/cognito | n/a |
| <a name="module_dynamodb_qualifications"></a> [dynamodb\_qualifications](#module\_dynamodb\_qualifications) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_resource_i18n"></a> [dynamodb\_resource\_i18n](#module\_dynamodb\_resource\_i18n) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_salaries"></a> [dynamodb\_salaries](#module\_dynamodb\_salaries) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_setting"></a> [dynamodb\_setting](#module\_dynamodb\_setting) | ./modules/dynamodb | n/a |
| <a name="module_dynamodb_user"></a> [dynamodb\_user](#module\_dynamodb\_user) | ./modules/dynamodb | n/a |
| <a name="module_ecr"></a> [ecr](#module\_ecr) | ./modules/ecr | n/a |
| <a name="module_frontend"></a> [frontend](#module\_frontend) | ./modules/frontend | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_caller_identity.current](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/caller_identity) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_name"></a> [app\_name](#input\_app\_name) | アプリケーション名 | `string` | n/a | yes |
| <a name="input_cognito_callback_url"></a> [cognito\_callback\_url](#input\_cognito\_callback\_url) | CognitoコールバックURL | `string` | `"http://localhost:3000/callback"` | no |
| <a name="input_cognito_logout_url"></a> [cognito\_logout\_url](#input\_cognito\_logout\_url) | CognitoログアウトURL | `string` | `"http://localhost:3000/logout"` | no |
| <a name="input_env_name"></a> [env\_name](#input\_env\_name) | 環境名 | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | リージョン | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_api_gateway_url"></a> [api\_gateway\_url](#output\_api\_gateway\_url) | n/a |
| <a name="output_apprunner_url"></a> [apprunner\_url](#output\_apprunner\_url) | n/a |
| <a name="output_login_url"></a> [login\_url](#output\_login\_url) | n/a |
<!-- END_TF_DOCS -->