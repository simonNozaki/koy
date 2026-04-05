# 構想: Result / or / try によるエラーハンドリング

## 概要

Koy のエラーハンドリングは二層構造を取る。

- **業務エラー**: Result 型（sealed）+ `?` 演算子 + `or` 演算子
- **システムエラー**: throw + try-catch（式）

## Result 型

sealed として定義される。言語組み込みか、ユーザー定義かは未決。

```koy
sealed Result {
  Ok { value },
  Err { message: String },
}
```

### 基本的な使い方

```koy
fn find_user(id: Int): Result {
  if (id <= 0) {
    Err("invalid id");
  } else {
    Ok(db.lookup(id));
  };
}
```

戻り値が `Result` であること自体が「この関数は失敗しうる」という契約になる。

## or 演算子

Result から値を取り出し、Err の場合はフォールバック値を返す。Kotlin の `?:` (elvis) に相当するが、英語として自然に読める。

```koy
val user = find_user(42) or default_user;
val name = find_user(42) |> map(|u| { u.name; }) |> or("anonymous");
```

## ? 演算子

Result を返す関数の中で、Err をそのまま呼び出し元に伝播する。レイヤーをまたぐ業務エラーの伝播を簡潔に書ける。

```koy
fn get_user_profile(id: Int): Result {
  val user = find_user(id)?;          // Err なら即 return Err
  val profile = fetch_profile(user)?; // 同上
  Ok(profile);
}
```

`?` がない場合の等価コード:

```koy
fn get_user_profile(id: Int): Result {
  match(find_user(id)) {
    Ok(user) => match(fetch_profile(user)) {
      Ok(profile) => Ok(profile);
      Err(e) => Err(e);
    };
    Err(e) => Err(e);
  };
}
```

## throw / try-catch

システムの異常事態（バグ、範囲外アクセス等）を扱う。try-catch は式であり、値を返す。

```koy
// throw
fn get(arr, index) {
  if (index < 0) {
    throw "index out of bounds";
  };
  arr->index;
}

// try-catch は式
val item = try { get(arr, 99); } catch(e) { nil; };
```

## パイプラインとの連携

Result はパイプライン演算子と組み合わせて使うことを想定する。

```koy
val name = find_user(42)
  |> map(|u| { u.name; })
  |> or("anonymous");

val result = fetch_data(url)
  |> flatmap(|d| { parse(d); })
  |> flatmap(|p| { validate(p); })
  |> map(|v| { format(v); })
  |> or(default_response);
```

## sealed によるエラー型の定義

業務エラーの種類が多い場合は sealed でドメイン固有のエラー型を定義する。

```koy
sealed AppError {
  NotFound { resource: String, id: Int },
  Validation { field: String, message: String },
  Unauthorized { reason: String },
}

// 途中のレイヤーは ? で伝播するだけ
fn get_user_profile(id: Int): Result {
  val user = find_user(id)?;
  val profile = fetch_profile(user)?;
  Ok(profile);
}

// 最終的にハンドリングする層で match
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

- Result を言語組み込みの sealed として提供するか、標準ライブラリとして提供するか、ユーザー定義に委ねるか
- Result の型パラメータ: `Result<T, E>` のようにジェネリクスを持たせるか、動的型のまま `Ok { value }` とするか
- `?` 演算子のスコープ: Result を返す関数の中でのみ使えるのか、トップレベルでも使えるのか
- `or` の評価戦略: `or` の右辺は遅延評価か即時評価か（`or { expensive_fallback(); }` のような構文が要るか）
- try-catch の catch 節: エラーの型によるフィルタリング（複数 catch 節）を持たせるか
- Result に対する標準的な高階関数の一覧と仕様（`map`, `flatmap`, `recover`, `onSuccess`, `onFailure` 等）
- match 式の網羅性検査: sealed のバリアントを全て網羅しているかインタプリタがチェックするか
