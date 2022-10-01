# Koy

Koy has a programming language that has simple and minimal specs. It only has function call and control flow (so, does not have class or equivalent struct).
Toys(from [web+db vol.125](https://gihyo.jp/magazine/wdpress/archive/2021/vol125)) implementation by Kotlin.

Original implementation is here: https://github.com/kmizu/toys

# Language Specs
## Literals
- `Int` : `0`
- `String` : `"text"`
- `Array` : `["Kotlin", "Java", "Koy"]`
- `Object` : `{ x: 1, y: "y" }`
- `Function` : `|x| -> { x * x; };`

## Assignment
`val` declaration creates immutable variable, so it can not accept reassign.
```
val f = |msg| {
  "Hello, " + msg;
};
```

## Control Flow
Standard control flows is all expressions and therefore return last value of blocks.
### While
```
i = 0;
while (i < 0) {
  i = i + 1;
}
```

### if-else
```
if (x < 5) {
  "over 5";
} else {
  "under 5";
}
```

### for-in
`for-in` expression is syntax sugar for `while` expression.
```
for (i in 0 to 10) {
  println(i);
  i = i + 1;
} 
```

## Function
Definition. Program should have `main` function. It also defines function by function literal.
### Top level function definition
```
fn factorial(x) {
  if (x < 2) {
    x
  }
  x * factorial(x - 1);
}

fn main() {
  factorial(5);
  
  square(5);
}
```
### Function literal(or lambda)
Simple function literal sample
```
square = |n| {
  n * n;
};

result = square();
```

Closure-like sample
```
Age = |v| {
  _v = v;
  {
    v: _v,
    getOld: |_| {
      _v = _v + 1;
    }
  };
};

now = Age(21);
println(now);
```

Function can be called by 2 way: standard call and labeled parameter call 
```
fn main() {
  factorial(5);
  
  factorial[x = 5];
}
```
