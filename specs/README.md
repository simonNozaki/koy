# Koy 言語仕様一覧

このディレクトリには Koy 言語の機能仕様書を格納する。spec-kit のフォーマットに準拠。

## 既存機能

| 仕様書 | 対象機能 | ステータス |
|---|---|---|
| [literals.md](literals.md) | Int・Bool・String・Array・Set・Object・Nil リテラル | 安定 |
| [variables.md](variables.md) | `val` / `mutable val` 宣言と代入 | 安定 |
| [operators.md](operators.md) | 算術・比較・論理・単項・コレクション演算子 | 安定 |
| [control-flow.md](control-flow.md) | `if`・`while`・`for-in` 式 | 安定 |
| [functions.md](functions.md) | トップレベル関数・関数リテラル・ラベル付き呼び出し・メソッド呼び出し | 安定 |

## 新機能追加のワークフロー

1. `specs/<機能名>.md` を仕様テンプレートに従って作成
2. アクセプタンスシナリオに対応するテストを `InterpreterTests.kt` / パーサーテストに追加（失敗することを確認）
3. `Expression.kt` → `Parsers.kt` → `Interpreter.kt` の順に実装
4. 全テストがパスしたら PR を作成
