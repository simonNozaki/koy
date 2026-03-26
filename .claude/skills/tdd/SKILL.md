---
name: tdd
description: >
  Koy言語インタプリタプロジェクト（Kotlin / JUnit5 / Gradle）でのテスト駆動開発
  （Red → Green → Refactor）をガイドするスキル。
  ユーザーが「TDDで進めたい」「テストから書いて」「Red-Green-Refactorで」
  「テスト駆動」「failing testから始めて」と言ったとき、またはバグ修正や
  新機能追加をテストから始めたいときに必ず使用すること。
  インタプリタからJava例外（ClassCastException、ArithmeticException、
  IndexOutOfBoundsException）が漏れている問題の修正依頼でも使用すること。
---

# TDD ワークフロー — Koy インタプリタ

## 3フェーズのサイクル

### 🔴 Red — 先に失敗するテストを書く

実装には一切触れず、期待する振る舞いを表すテストを先に書く。

**テストを追加するファイル**

| テスト対象 | ファイル |
|---|---|
| インタプリタの振る舞い（評価・エラー） | `src/test/kotlin/io/github/simonnozaki/koy/InterpreterTests.kt` |
| リテラル・式のパース | `src/test/kotlin/io/github/simonnozaki/koy/LiteralParserSpecs.kt` / `ExpressionLinesParserSpecs.kt` |
| 関数のパース | `src/test/kotlin/io/github/simonnozaki/koy/FunctionParserSpecs.kt` |

新しいファイルを作るより既存ファイルへの追加を優先する。テストスイートが見渡しやすくなる。

**ASTファクトリ関数（`ast.kt` より）**

```kotlin
// リテラル
integer(n)           str("...")        bool(true)

// 算術演算
add(l, r)            subtract(l, r)    multiply(l, r)
divide(l, r)         remain(l, r)

// 比較・論理演算
lessThan(l, r)       lessThanEqual(l, r)
greaterThan(l, r)    greaterThanEqual(l, r)
equal(l, r)          notEqual(l, r)
logicalAnd(l, r)     logicalOr(l, r)

// 変数・制御フロー
identifier("name")   assign("name", expr)
Block(expr...)       If(cond, then, Optional.of(else))

// 関数
call("name", args...)
defineFunction("name", listOf("x", "y"), Block(...))
```

**ランタイムエラーのアサーション**

```kotlin
@Test
fun `should throw on ...`() {
    assertThrows<KoyLangRuntimeException> {
        Interpreter().interpret(/* 式 */)
    }
}
```

**テストが失敗することを確認する（コンパイルエラーではなくテスト失敗）**

```bash
./gradlew test --tests "io.github.simonnozaki.koy.InterpreterTests.テスト名"
```

`BUILD FAILED` でテスト失敗が出ればOK。コンパイルエラーが出た場合はテストコードを先に修正する。

---

### 🟢 Green — 最小限の実装でパスさせる

失敗しているテストをパスさせるために必要な最小限のコードだけを書く。この段階では汎用化や整理はしない。

全テストを実行してリグレッションがないことを確認する。

```bash
./gradlew test
```

全テストがグリーンになってから次のフェーズへ進む。

---

### 🔵 Refactor — テストを盾にコードを整理する

テストがグリーンの状態で、振る舞いを変えずにコードを改善する。

- 繰り返しのロジックをprivateヘルパーに抽出する（例: `requireBothInt(lhs, rhs, op)`）
- テストは関連する既存ファイルにまとめる。別ファイルに分けたまま残さない
- 命名を明確にする

変更のたびに `./gradlew test` を実行してグリーンを維持する。

---

## このコードベース固有のパターン

### KoyLangRuntimeException が契約

Javaの組み込み例外（`ClassCastException`、`ArithmeticException`、`IndexOutOfBoundsException`）がインタプリタから漏れるのはバグ。テストでは必ず `KoyLangRuntimeException` をアサートする。

### バリデーションは実行より前に行う

型チェック・引数数チェック・ゼロ除算チェックは、処理を実行する**前**に行う。後で検査すると、エラー前に副作用が発生してしまう。

```kotlin
// ✅ 先にチェック、その後に引数を評価
if (actualParams.size != formalParams.size) {
    throw KoyLangRuntimeException("${formalParams.size}個の引数を期待しましたが${actualParams.size}個渡されました")
}
val values = actualParams.map { interpret(it) }

// ❌ 先に評価 — エラー前に副作用が走る
val values = actualParams.map { interpret(it) }
if (actualParams.size != formalParams.size) { ... }
```

---

## Gitワークフロー

1. **ブランチを切ってから始める** — `git checkout -b fix/<topic>` または `add/<topic>`
2. Green後（Refactorが大きければRefactor後も）コミットする
   ```
   FIX: <何を・なぜ直したか>
   ADD: <何の振る舞いを追加したか>
   REFACTOR: <何を整理したか>
   ```
3. サイクルが完了したらプッシュしてPRを作成する
