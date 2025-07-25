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

## OpenAPI 定義から API クライアント、スキーマを自動生成

```sh
cd ../openapi
openapi-generator-cli generate
npx openapi-zod-client  openapi.yaml -o ../front/api/client/schemas.ts --export-schemas
```

## 開発用にサーバーを起動

```sh
npm run dev
```

## Mock サーバーを起動

```sh
npm run mock
```
