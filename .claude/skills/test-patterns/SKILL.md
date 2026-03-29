---
name: test-patterns
description: >
  Koy プロジェクト（Kotlin / JUnit5 / Gradle）におけるテストの書き方・構造化パターン。
  テストを新規追加・レビュー・リファクタリングするとき、または「テストを直して」
  「テスト構造を見直して」「テストを整理して」と言われたときに使用すること。
---

# テストパターン — Kotlin / JUnit5

## 1. context / test 構造

`@Nested inner class` でテストを「前提条件（context）」と「期待する振る舞い（test）」に分ける。

```kotlin
class FooTest {
    @Nested
    inner class `when input is valid` {
        @Test
        fun `should return expected result`() { ... }

        @Test
        fun `should not throw`() { ... }
    }

    @Nested
    inner class `when input is invalid` {
        @Test
        fun `should throw IllegalArgumentException`() { ... }
    }
}
```

- クラス名: `when ...` — 前提条件・状態を表す
- テスト名: `should ...` — 期待する振る舞いを表す
- コンテキストが増えても構造が崩れず、失敗時のメッセージが読みやすくなる

---

## 2. ユーティリティとアサーションの分離

ヘルパー関数の責務は「セットアップ・操作の実行」のみ。アサーションをヘルパーに含めない。

```kotlin
// ❌ ヘルパーにアサーションを含める
private fun runAndAssert(source: String) {
    val result = run(source)
    assertEquals(42, result)  // アサーションがヘルパーに埋まっている
}

// ✅ ヘルパーは結果を返すだけ
private fun run(source: String): Interpreter {
    val interpreter = Interpreter()
    Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
    return interpreter
}

@Test
fun `should evaluate to 42`() {
    val interpreter = run("val x = 42;")
    assertEquals(42, interpreter.getValue("x")?.asInt()?.value)
}
```

アサーションがテスト本体にあることで、テストが失敗した理由とその文脈がひと目でわかる。

---

## 3. null チェックの順序

nullable な値を使う前に `assertNotNull` で存在を確認し、その後に値の内容をアサートする。

```kotlin
// ❌ !! で強制アンラップ — assertNotNull を飛ばしている
assertTrue(body.error!!.isNotBlank())

// ✅ 存在を確認してから内容を検証
assertNotNull(body.error)
assertTrue(body.error.isNotBlank())
```

`assertNotNull` が失敗した時点でテストが止まり、NullPointerException ではなく「null だった」という明確な失敗メッセージが出る。

---

## 4. セットアップの集約

### 基本: `@BeforeEach`

同じセットアップをコンテキスト内の全テストで共有する場合は `@BeforeEach` を使う。

```kotlin
@Nested
inner class `when foo is initialized` {
    private lateinit var foo: Foo

    @BeforeEach
    fun setup() {
        foo = Foo(value = 42)
    }

    @Test
    fun `should have correct value`() {
        assertEquals(42, foo.value)
    }
}
```

### Ktor 固有: wrapper 関数による集約

`testApplication` は呼び出しごとに新しいサーバーインスタンスを生成するため `@BeforeEach` と組み合わせられない。コンテキストクラスごとにプライベートな wrapper 関数を定義してセットアップを集約する。

```kotlin
@Nested
inner class `when runCode succeeds` {
    // セットアップはここに集約
    private fun test(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { configure(runCode = { RunResponse(output = "42") }) }
        block()
    }

    @Test
    fun `should respond with 200`() = test {
        val response = client.post("/run") { ... }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should return output in response body`() = test {
        val response = client.post("/run") { ... }
        val body = Json.decodeFromString<RunResponse>(response.bodyAsText())
        assertEquals("42", body.output)
    }
}
```

全コンテキストで同じセットアップを使う場合はクラスのトップレベルに定義する。

```kotlin
class PlaygroundIntegrationTest {
    private fun test(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { configure() }
        block()
    }
    ...
}
```

---

## 5. Ktor 固有: HTTP ステータスコードの検証

ステータスコードは常に明示的にアサートする。成功・失敗それぞれで期待するコードが異なることを示す。

```kotlin
// 成功レスポンス
assertEquals(HttpStatusCode.OK, response.status)

// クライアントエラー（例: コード実行エラー）
assertEquals(HttpStatusCode.BadRequest, response.status)
```

エラーを HTTP 200 + エラーボディで返す設計は避ける。クライアントエラーには 4xx を使い、エラーの種別がステータスコードだけで判断できるようにする。
