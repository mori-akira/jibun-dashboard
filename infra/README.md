## AWS-CLIの認証設定

以下を手動実行する。

```sh
echo export AWS_ACCESS_KEY_ID=[発行したアクセスキー] >> ~/.bash_profile
echo export AWS_SECRET_ACCESS_KEY=[発行したシークレット] >> ~/.bash_profile
source ~/.bash_profile
aws sts get-caller-identity # 確認
```

## Terraform動作用の環境変数設定

```sh
echo export TF_VAR_region=[リージョン] >> ~/.bash_profile
source ~/.bash_profile
```

## Bootstrap適用

```sh
cd bootstrap
terraform init
terraform fmt *.tf
terraform plan
terraform apply
```
