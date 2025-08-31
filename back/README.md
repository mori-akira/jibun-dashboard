# バックエンドモジュール

## 実行環境 (開発時のバージョン)
- Java: openjdk 21.0.8
- Gradle: 8.14.3
- Kotlin: 2.0.0
- Spring Boot: 3.3.1

## ローカル実行手順

### 事前準備
以下のCLIが要インストール
- [AWS CLI](https://docs.aws.amazon.com/ja_jp/cli/latest/userguide/getting-started-install.html)
- [Terraform](https://developer.hashicorp.com/terraform/install)

### ローカルでDynamoDBエミュレータを起動
```
./gradlew dynamodbUp
```

### ローカルでDynamoDBエミュレータを終了
```
./gradlew dynamodbDown
```

### バックエンドの起動
```
SPRING_PROFILES_ACTIVE=local
AWS_ACCESS_KEY_ID=dymmy
AWS_SECRET_ACCESS_KEY=dymmy
./gradlew clean bootRun
```
