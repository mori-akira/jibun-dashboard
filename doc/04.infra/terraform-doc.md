<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | ~> 1.11.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | ~> 6.5 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.15.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_apigateway"></a> [apigateway](#module\_apigateway) | ./modules/apigateway | n/a |
| <a name="module_application"></a> [application](#module\_application) | ./modules/application | n/a |
| <a name="module_apprunner"></a> [apprunner](#module\_apprunner) | ./modules/apprunner | n/a |
| <a name="module_codebuild"></a> [codebuild](#module\_codebuild) | ./modules/codebuild | n/a |
| <a name="module_cognito"></a> [cognito](#module\_cognito) | ./modules/cognito | n/a |
| <a name="module_dynamodb"></a> [dynamodb](#module\_dynamodb) | ./modules/dynamodb | n/a |
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
| <a name="input_cognito_client_id"></a> [cognito\_client\_id](#input\_cognito\_client\_id) | CognitoクライアントID | `string` | n/a | yes |
| <a name="input_cognito_domain"></a> [cognito\_domain](#input\_cognito\_domain) | Cognitoドメイン | `string` | n/a | yes |
| <a name="input_cognito_logout_url"></a> [cognito\_logout\_url](#input\_cognito\_logout\_url) | CognitoログアウトURL | `string` | `"http://localhost:3000/logout"` | no |
| <a name="input_cognito_user_pool_id"></a> [cognito\_user\_pool\_id](#input\_cognito\_user\_pool\_id) | CognitoユーザープールID | `string` | n/a | yes |
| <a name="input_e2e_alert_email"></a> [e2e\_alert\_email](#input\_e2e\_alert\_email) | E2Eテスト エラー通知先メールアドレス | `string` | `null` | no |
| <a name="input_e2e_password"></a> [e2e\_password](#input\_e2e\_password) | E2Eテスト用のパスワード | `string` | n/a | yes |
| <a name="input_e2e_username"></a> [e2e\_username](#input\_e2e\_username) | E2Eテスト用のユーザ名 | `string` | n/a | yes |
| <a name="input_env_name"></a> [env\_name](#input\_env\_name) | 環境名 | `string` | n/a | yes |
| <a name="input_github_url"></a> [github\_url](#input\_github\_url) | GitHubリポジトリURL | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | リージョン | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_apigateway_url"></a> [apigateway\_url](#output\_apigateway\_url) | n/a |
| <a name="output_apprunner_url"></a> [apprunner\_url](#output\_apprunner\_url) | n/a |
| <a name="output_login_url"></a> [login\_url](#output\_login\_url) | n/a |
<!-- END_TF_DOCS -->