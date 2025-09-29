# E2E テストコード

## Playwright のインストール

※DevContainer から起動する場合は、自動インストールされるため対応不要

```sh
npx playwright install --with-deps
```

## 依存関係のインストール

```sh
npm install
```

## 設定ファイル

ローカルで動作確認する場合、 `.env.example` をコピーし、 `.env` にローカルの設定を記述する。

## テストの実行

```sh
# 未認証時のテスト
npm run test-no-auth
# セットアップ (認証処理) のテスト
npm run test-setup
# 認証済みのテスト
npm run test-auth
```

## テストレポートの確認

```sh
npm run report
```

## コードジェネレータを起動

```sh
npm run codegen
```
