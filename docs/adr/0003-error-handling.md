# ADR-0003: エラーハンドリングの設計

## ステータス

Draft

## コンテキスト

Koy 言語にエラーハンドリングの仕組みを導入するにあたり、言語思想（式ベース・イミュータブル）と実用性（書きやすさ・レイヤーをまたぐ伝播）を両立する設計が求められる。

### 検討した方式

| 方式 | 代表的な言語 | 概要 |
|---|---|---|
| Result 型のみ | Rust, Elm | 失敗を値として表現し、match で処理する。堅牢だが冗長になりがち |
| 例外スローのみ | Ruby, Python | throw/raise で制御フローを中断する。書きやすいが関数シグネチャに失敗が見えない |
| 検査例外 | Java | シグネチャに `throws` を宣言し、呼び出し側に処理を強制する。形骸化した歴史がある |
| 順序対 (value, error) | Go | 関数が値とエラーのタプルを返す。チェック忘れが頻発する |

## 決定の方向性

**二層構造**を採用する方向で検討を進める。

### 第一層: Result 型（業務エラー）

予期される失敗を sealed 型の値として表現する。関数シグネチャに `Result` が現れることで、呼び出し側が失敗を認識できる。

```koy
sealed Result {
  Ok { value },
  Err { message: String },
}

fn find_user(id: Int): Result {
  if (id <= 0) {
    Err("invalid id");
  } else {
    Ok(db.lookup(id));
  };
}
```

冗長さを緩和するシンタックスシュガーを併せて導入する。

#### `or` 演算子

Result から値を取り出し、Err の場合はフォールバック値を返す。

```koy
val name = find_user(42) |> map(|u| { u.name; }) |> or("anonymous");
```

#### `?` 演算子

Result を返す関数内で、Err をそのまま呼び出し元に伝播する。レイヤーをまたぐ業務エラーの伝播はこれで解決する。

```koy
fn get_user_profile(id: Int): Result {
  val user = find_user(id)?;          // Err なら即 return Err
  val profile = fetch_profile(user)?; // 同上
  Ok(profile);
}
```

### 第二層: 例外（システムエラー）

プログラマのミスやシステムの異常事態は throw + try-catch で処理する。try-catch は式であり値を返す。

```koy
// throw
fn get(arr, index) {
  if (index < 0) {
    throw "index out of bounds";
  };
  arr->index;
}

// try-catch（式）
val item = try { get(arr, 99); } catch(e) { nil; };
```

### 二層構造の判断基準

| 層 | 手段 | 対象 | シグネチャ |
|---|---|---|---|
| 業務エラー | Result + `?` + `or` | ユーザー検索失敗、バリデーション不正等 | 戻り値が `Result` → 失敗しうることが見える |
| システムエラー | throw + try-catch | ゼロ除算、範囲外アクセス、バグ | 戻り値に現れない → 正常に使えば起きない前提 |

### sealed によるエラー種別の型定義

業務エラーの種類が多い場合は sealed でエラー型を定義し、match で網羅的にハンドリングする。

```koy
sealed AppError {
  NotFound { resource: String, id: Int },
  Validation { field: String, message: String },
  Unauthorized { reason: String },
}

fn handle_request(id: Int) {
  match(get_user_profile(id)) {
    Ok(res) => send(200, res);
    Err(NotFound(r, id)) => send(404, r + " not found");
    Err(Validation(f, m)) => send(400, f + ": " + m);
    Err(Unauthorized(r)) => send(401, r);
  };
}
```

## 未決事項

- Result 型を言語組み込みの sealed として提供するか、ユーザー定義に委ねるか
- `?` 演算子の詳細な構文規則（連鎖、ネスト時の挙動）
- try-catch の構文詳細（catch の引数の型、複数 catch 節の要否）
- Result に対する標準的な高階関数（`map`, `flatmap`, `recover` 等）の仕様

## 採用しなかった案

| 案 | 不採用の理由 |
|---|---|
| 例外のみ | 関数シグネチャから失敗が見えない。呼び出し側が例外の存在を知るにはドキュメントかソースを読む必要がある |
| Result のみ（シュガーなし） | 堅牢だが match のネストが深くなり、日常的なコードが冗長になる |
| 検査例外 | Java での形骸化の歴史を踏まえ、不採用 |
| 順序対 (value, error) | Go スタイル。エラーチェック忘れが頻発し、自然な書き味を追究する Koy の方向性と合わない |
