# フロントモジュール

## 依存関係のインストール

以下のコマンドを手動実行

```sh
npm install
```

## AWS-CLI の認証設定

以下を手動実行する。

```sh
aws configure # 指示に従いアクセスキー情報を入力
aws sts get-caller-identity # 確認
```

## OpenAPI 定義から API クライアントを自動生成

```sh
cd ../openapi
openapi-generator-cli generate
```
