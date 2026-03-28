# プレイグラウンド仕様

Koy 言語をブラウザ上で手軽に試せる Web ベースの実行環境。

## 概要

言語機能の拡張（Array・Set・Object リテラルなど）に先行して、インタラクティブな動作確認環境を提供する。
REPL は状態管理・エラーリカバリが複雑になるため採用しない。

## システム構成

```
[ブラウザ]
  CodeMirror 6 エディタ + 実行ボタン + 出力エリア
        ↓ POST /run (JSON)
[Ktor embedded server]
        ↓ PrintStream 注入
[Interpreter]
        ↓ ByteArrayOutputStream でキャプチャ
[レスポンス返却]
```

### バックエンド

- Ktor embedded server（Kotlin）
- `POST /run` エンドポイントでコードを受け取り、インタープリタで評価する
- Ktor が静的ファイル配信（`resources/static/`）と API サーバを兼ねる

### フロントエンド

- Vite + TypeScript（React・Vue などのフレームワークは使用しない）
- コードエディタ: CodeMirror 6
- ビルド成果物を Ktor の `resources/static/` に配置する

## API インターフェース

### POST /run

**リクエスト**

```json
{ "code": "<Koy ソースコード>" }
```

**レスポンス**

```json
{ "output": "<標準出力文字列 | null>", "error": "<スタックトレース文字列 | null>" }
```

- 正常実行時: `output` に標準出力の内容、`error` は `null`
- 例外発生時: `output` は `null`、`error` にスタックトレース文字列

## フロントエンド機能要件

| 要素 | 内容 |
|---|---|
| コードエディタ | CodeMirror 6 を使用。Koy 言語のシンタックスハイライト対応 |
| 実行ボタン | クリックで `POST /run` を呼び出す |
| 標準出力表示エリア | レスポンスの `output` をそのまま表示する |
| エラー表示 | レスポンスの `error` が存在する場合、赤字でスタックトレースを表示する |

## 標準出力キャプチャの方式

`Interpreter` のコンストラクタに `PrintStream` を注入する方式を採用する。

```kotlin
val baos = ByteArrayOutputStream()
val result = Interpreter(out = PrintStream(baos)).interpret(code)
val output = baos.toString()
```

- リクエストごとに独立した `ByteArrayOutputStream` を生成するため、並行リクエストでも出力が混在しない
- `System.setOut` によるグローバル状態の書き換えは行わない

詳細な採用根拠は [ADR-0001](../docs/adr/0001-playground-architecture.md) を参照。
