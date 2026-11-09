# バックエンドモジュール

## 実行環境 (開発時のバージョン)
- Java: openjdk 21.0.8
- Gradle: 8.14.3
- Kotlin: 2.3.21
- Spring Boot: 4.1.2

## ローカル実行手順

### 事前準備
以下のCLIが要インストール
- [AWS CLI](https://docs.aws.amazon.com/ja_jp/cli/latest/userguide/getting-started-install.html)
- [Terraform](https://developer.hashicorp.com/terraform/install)

### ローカル環境（DynamoDB Local / LocalStack）を起動
Docker コンテナの起動、Terraform によるテーブル・バケット・キューの作成、シードデータの投入を行う
```
./gradlew localEnvUp
```

### ローカル環境を終了
コンテナを停止し、Terraform の state を削除する（DynamoDB Local はインメモリのため、データは破棄される）
```
./gradlew localEnvDown
```

### バックエンドの起動
```
export SPRING_PROFILES_ACTIVE=local
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
export UPLOADS_BUCKET_NAME=jibun-dashboard-local-uploads-bucket
export USER_ASSETS_BUCKET_NAME=jibun-dashboard-local-user-assets-bucket
./gradlew clean bootRun
```
