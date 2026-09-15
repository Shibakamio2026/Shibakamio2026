# Shibakamio2026 — 学生向け家計簿アプリ

収入・支出・資産をまとめて管理できる、Spring Boot 製の Web 家計簿アプリです。
PayPay の取引履歴 CSV の取り込みや、固定費の自動登録、月次レポートにも対応しています。

## 主な機能

| 機能 | 内容 |
| --- | --- |
| ユーザー認証 | 新規登録 / ログイン / ログアウト（Spring Security）、アカウント設定・退会 |
| 収支の記録 | 収入・支出の登録、一覧、編集、削除 |
| 資産管理 | 現金・銀行口座・電子マネーなどの資産を登録し、残高を管理 |
| 資産間の振替 | 口座から財布へ、など資産間のお金の移動を記録 |
| カテゴリー管理 | 食費などのカテゴリーを追加・編集・無効化 |
| 自動登録（固定費） | 毎週 / 毎月 / 毎年の繰り返し取引を登録し、予定取引を自動生成 |
| 予定取引の確定 | 生成された予定を確認して「確定」または「キャンセル」 |
| PayPay CSV 取り込み | 取引履歴 CSV をアップロード → プレビュー → 確定して一括登録 |
| カレンダー | 日ごとの収支をカレンダー形式で表示 |
| レポート | 月次の収支・カテゴリー別集計、グラフ用 API（`/api/reports`） |

## 使用技術

- **言語**: Java 21
- **フレームワーク**: Spring Boot 4.1（Web MVC / Data JPA / Security / Validation）
- **テンプレート**: Thymeleaf（+ thymeleaf-extras-springsecurity6）
- **データベース**: PostgreSQL
- **ビルド**: Maven（Maven Wrapper 同梱）
- **その他**: Lombok, Spring Boot DevTools

## ディレクトリ構成

```
src/main/java/com/example/shibakamio2026/
├── config/       # Spring Security の設定
├── controller/   # 画面・API のコントローラー
├── service/      # 業務ロジック（CSV 解析、自動登録、レポート集計など）
├── repository/   # JPA リポジトリ
├── entity/       # エンティティ（User, Asset, Transaction など）
├── dto/          # フォーム・画面表示用オブジェクト
├── enums/        # 取引種別・資産種別・繰り返し間隔など
├── exception/    # 独自例外
└── web/          # ログインユーザー取得などの共通処理

src/main/resources/
├── templates/    # Thymeleaf テンプレート
├── static/css/   # スタイルシート
├── schema.sql    # テーブル定義
├── data.sql      # 初期データ（テストユーザーなど）
└── application.properties
```

## テーブル一覧

| テーブル | 内容 |
| --- | --- |
| `users` | ユーザー |
| `assets` | 資産（現金・銀行口座・電子マネー・その他） |
| `categories` | 収支カテゴリー |
| `transactions` | 収入・支出の記録 |
| `transfers` | 資産間の振替 |
| `auto_transactions` | 繰り返し取引の設定 |
| `scheduled_transactions` | 自動生成された予定取引 |

## 自分のパソコンで動かす手順

このアプリは、自分のパソコンの中でサーバーを起動し、ブラウザから開いて使います。
ここでは **Mac で、初めてこのアプリを動かす人** を想定して、手順を順番に説明します。

> コマンドはすべて Mac の「ターミナル」アプリに入力します。
> （Finder →「アプリケーション」→「ユーティリティ」→「ターミナル」）

### 手順 1. 必要なソフトを入れる

アプリを動かすには、次の 2 つが必要です。

| ソフト | 何に使うか |
| --- | --- |
| **JDK 21** | Java で書かれたこのアプリを動かすため |
| **PostgreSQL** | 家計簿のデータ（ユーザー、収支、資産など）を保存するデータベース |

[Homebrew](https://brew.sh/ja/) を使っている場合は、次のコマンドで入れられます。

```bash
brew install openjdk@21
brew install postgresql@16
brew services start postgresql@16   # PostgreSQL を起動する
```

入ったかどうかは、次のコマンドで確認できます。バージョン番号が表示されれば OK です。

```bash
java -version
psql --version
```

### 手順 2. ソースコードをダウンロードする

GitHub からこのリポジトリを自分のパソコンにコピー（クローン）します。

```bash
git clone git@github.com:Shibakamio2026/Shibakamio2026.git
cd Shibakamio2026
```

2 行目の `cd` で、ダウンロードしたフォルダの中に移動しています。以降のコマンドはこのフォルダの中で実行します。

### 手順 3. データを保存する場所（データベース）を作る

アプリがデータを保存するための、空のデータベースを 1 つ作ります。
名前は `shibakamio2026` にしてください（アプリがこの名前を探しに行きます）。

```bash
createdb shibakamio2026
```

作るのは「空の入れ物」だけで大丈夫です。
**テーブル（表）やテスト用のデータは、アプリの起動時に自動で作られます。**

> **うまくいかないとき**
> アプリは、ユーザー名 `postgres`・パスワード `postgres` でデータベースに接続する設定になっています。
> 自分の PostgreSQL の設定と違う場合は、`src/main/resources/application.properties` の次の行を書き換えてください。
>
> ```properties
> spring.datasource.username=postgres
> spring.datasource.password=postgres
> ```

### 手順 4. アプリを起動する

ターミナルで次のコマンドを実行します。

```bash
./mvnw spring-boot:run
```

初回は必要なライブラリのダウンロードがあるので、少し時間がかかります。
ログに `Started Shibakamio2026Application` と表示されたら起動完了です。

**Eclipse を使う場合**は、コマンドの代わりに
`Shibakamio2026Application.java` を右クリック →「実行」→「Spring Boot アプリケーション」でも起動できます。

止めるときは、ターミナルで `control` + `C` を押します。

### 手順 5. ブラウザで開いてログインする

ブラウザで次のアドレスを開くと、ログイン画面が表示されます。

👉 http://localhost:8080/login

すぐに動作を試せるように、**お試し用のアカウントが最初から用意されています**。
自分でユーザー登録しなくても、次の情報でログインできます。

| メールアドレス | パスワード |
| --- | --- |
| `test@example.com` | `password123` |

このアカウントには、カテゴリー「食費」と資産「財布（1 万円）」が登録済みです。
自分用のアカウントを作りたい場合は、ログイン画面から新規登録してください。

## PayPay CSV の取り込みについて

- 文字コードは UTF-8、1 行目はヘッダーとして読み飛ばします
- 取引日は `yyyy/MM/dd` 形式
- 内容に「チャージ」を含む行はチャージ、出金額がある行は支出、それ以外は収入の候補として判定します
- ファイルサイズの上限は 5MB です

## 作者

芝川雄也（[@Shibakamio2026](https://github.com/Shibakamio2026)）