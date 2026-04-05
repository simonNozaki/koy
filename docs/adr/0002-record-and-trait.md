# ADR-0002: record と trait による値と振る舞いの設計

## ステータス

Accepted

## コンテキスト

Koy 言語を比較的サイズのあるプロダクトにも対応できる言語へ進化させるにあたり、値と振る舞いをバンドルする機構が必要になった。

Koy の言語思想は以下の通り。

- **式ベース**: すべての構文要素が値を返す
- **イミュータブルファースト**: `val` がデフォルト、`mutable val` でオプトイン
- **インタプリタ**: ツリーウォーキング方式、GC に依存

この思想と整合する形で、データ構造の定義とポリモーフィズムの仕組みを設計する必要がある。

### 検討した振る舞いバンドルの方式

| 方式 | 代表的な言語 | 概要 |
|---|---|---|
| クラス継承 | Java, Python, C# | クラスに値と振る舞いを持たせ、継承で差分を表現する |
| Struct + Trait | Rust, Swift | データ定義と振る舞い契約を分離し、`impl` ブロックで後付け結合する |
| Typeclass | Haskell, Scala 3 | 型に対する振る舞いの証拠（witness）を文脈ごとに渡す |
| ADT + モジュール | F#, OCaml, Elm | 代数的データ型を定義し、モジュール内の関数群で操作する |

## 決定

**record** と **trait** を言語の中核機構として導入する。

### record

`record` は値の集合を主とし、振る舞いを付随させる構造体を定義する予約語である。

```koy
record Point with Describable {
  x: Int,
  y: Int,

  fn distance(self, other: Point): Int {
    val dx = self.x - other.x;
    val dy = self.y - other.y;
    dx * dx + dy * dy;
  }

  fn describe(self): String {
    "(" + self.x + ", " + self.y + ")";
  }
}
```

record の性質:

- **予約語**であり、record のインスタンスを生成する
- **式**である。変数に束縛でき、関数の戻り値やコレクションの要素として使える
- **イミュータブル**がデフォルト
- すべての record は **Record trait** を暗黙的に満たす（Record trait の具体的な内容は今後詳細化する）

#### 全リテラルが record である

Ruby の「すべてが Object」と同じ考え方で、Koy のすべてのリテラルは record である。

```koy
42                     // Int record
"hello"                // String record
true                   // Bool record
[1, 2, 3]             // Array record
%{ "a", "b" }         // Set record
{ x: 1, y: 2 }       // Object record
|x| { x + 1; }       // Function record
nil                    // Nil record
Point { x: 1, y: 2 } // ユーザー定義 record
```

これにより以下が実現される。

- 言語の値に関するルールが「すべて record である」の一つで済む
- プリミティブとオブジェクトの挙動差異がない（Java の `int` vs `Integer` 問題が存在しない）
- 組み込み型とユーザー定義型を同じ仕組みで扱える

#### record の命名根拠

`struct`、`data`、`type`、`model` 等の候補を検討した。

| 候補 | 不採用の理由 |
|---|---|
| `struct` | C/Go の低レベルなメモリレイアウトの印象がある。インタプリタ言語とのミスマッチ |
| `data` | 意味は合うが、変数名として `data` を使いたい場面が多く予約語にすると実用上の衝突がある |
| `type` | 型注釈（`x: Int`）の文脈と紛らわしい |
| `model` | 意味が曖昧 |

`record` は「値を記録したもの」という原義がイミュータブルの思想と合致し、プログラマに広く馴染みがあり、変数名との衝突が起きにくい。

### trait

`trait` は振る舞いの契約を定義する予約語である。record は `with` キーワードで trait を取り込み、一つのブレース内にデータ・メソッド・trait 実装をすべて記述する。

```koy
trait Describable {
  fn describe(self): String;
}

trait Eq {
  fn eq(self, other): Bool;
}

record Point with Describable, Eq {
  x: Int,
  y: Int,

  fn describe(self): String {
    "(" + self.x + ", " + self.y + ")";
  }

  fn eq(self, other): Bool {
    self.x == other.x and self.y == other.y;
  }
}
```

trait の性質:

- **振る舞いの契約**を定義する。record が `with` で取り込むことで「この record はこの特性を満たす」と宣言する
- **デフォルト実装**を持てる。trait 内に関数本体を書くと、record 側で実装を省略できる
- **ポリモーフィズム**を実現する。異なる record が同じ trait を満たすことで、共通のインターフェースで扱える

```koy
trait Printable {
  fn to_string(self): String;

  // デフォルト実装：to_string さえ実装すれば print はタダで使える
  fn print(self) {
    println(self.to_string());
  }
}
```

#### with による一体宣言

Rust の `impl Trait for Type` のように振る舞いの実装を型定義の外に分離する方式は採用しない。record 宣言の一つのブレース内に値・振る舞い・trait 実装がすべてまとまることで、その record の全体像を一箇所で把握できる。

```koy
// Koy: 一箇所にすべてまとまる
record Point with Describable, Eq {
  x: Int,           // データ
  y: Int,
  fn distance ...   // 自前メソッド
  fn describe ...   // trait 実装
  fn eq ...         // trait 実装
}

// Rust: impl が分散する（不採用）
// struct Point { x: i32, y: i32 }
// impl Describable for Point { ... }
// impl Eq for Point { ... }
```

### sealed

`sealed` は record のバリアント集合を定義する予約語である。取りうる値のバリエーションが有限であることを型として表現する。

```koy
sealed Shape with Describable {
  Circle { radius: Int },
  Rect { width: Int, height: Int },

  fn area(self): Int {
    match(self) {
      Circle(r) => r * r * 3;
      Rect(w, h) => w * h;
    };
  }

  fn describe(self): String {
    match(self) {
      Circle(r) => "circle r=" + r;
      Rect(w, h) => w + "x" + h;
    };
  }
}
```

sealed は match 式と連携し、全パターンの網羅性を検査できる。

## 採用しなかった案

| 案 | 不採用の理由 |
|---|---|
| クラス継承 | 可変状態を前提とし、式ベース・イミュータブルの思想と合わない。fragile base class 問題やダイヤモンド継承の問題を持ち込む |
| `impl` ブロックの分離（Rust 方式） | 振る舞いが型定義の外に分散し、一箇所で全体像を把握できない |
| Typeclass | 概念のハードルが高く、インタプリタ実装が複雑になる |
| ADT + モジュールのみ（trait なし） | ポリモーフィズムが弱く、大規模プロダクトでのインターフェース契約が不足する |

## 結果

- `record` がデータ構造の唯一の定義手段となり、全リテラルも record として統一される
- `trait` + `with` により継承なしでポリモーフィズムを実現し、一つのブレースで値・振る舞い・特性を一覧できる
- `sealed` により有限のバリアント表現と match 式による網羅的なディスパッチが可能になる
- これらの組み合わせで、式ベース・イミュータブルの思想を保ちながら、大規模プロダクトにも対応できる構造化の仕組みを提供する
