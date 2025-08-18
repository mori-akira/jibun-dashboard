# インフラ定義

## AWS-CLIの認証設定

以下を手動実行する。

```sh
aws configure # 指示に従いアクセスキー情報を入力
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
terraform apply -auto-approve
```

## 動作確認

```sh
cd main
terraform init
terraform fmt *.tf
terraform plan
terraform apply -auto-approve
terraform destroy -auto-approve # 削除
```
