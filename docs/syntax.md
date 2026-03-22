# Chi Syntax Reference

This document covers the core syntax of the Chi programming language.

## Comments

```chi
// Single-line comment

/* Multi-line
   comment */
```

## Literals

### Numbers

Numbers are written as sequences of digits, optionally with a decimal point:

```chi
42        // int
3.14      // float
0         // int
100.0     // float
```

### Booleans

```chi
true
false
```

### Unit

The `unit` literal represents the absence of a value (similar to `void` or `null` in other languages):

```chi
unit
```

### Strings

Strings are enclosed in double quotes:

```chi
"hello world"
```

#### Escape sequences

| Sequence | Meaning |
|----------|---------|
| `\n` | Newline |
| `\r` | Carriage return |
| `\t` | Tab |
| `\\` | Backslash |
| `\"` | Double quote |
| `\$` | Literal dollar sign |

```chi
"line one\nline two"
"path\\to\\file"
"she said \"hi\""
"price: \$100"
```

#### String interpolation

Use `$name` to interpolate a variable, or `${expression}` for arbitrary expressions:

```chi
val name = "World"
val greeting = "Hello $name!"
val result = "2 + 2 = ${2 + 2}"
```

### Arrays

Arrays are created with square brackets:

```chi
[1, 2, 3]
["hello", "world"]
[]              // empty array
```

### Records

Records are created with curly braces containing named fields:

```chi
{ name: "Alice", age: 30 }
{ x: 1, y: 2 }
```

## Variable Declarations

### Immutable variables (`val`)

```chi
val x = 42
val name = "Chi"
val numbers = [1, 2, 3]
```

### Mutable variables (`var`)

```chi
var counter = 0
counter = counter + 1
counter += 1
```

### With explicit type annotation

```chi
val x: int = 42
var name: string = "Chi"
val result: int | unit = 5
```

### Visibility

Use `pub` to make a declaration publicly accessible from other packages:

```chi
pub val version = "1.0"
pub var state = "ready"
```

## Assignment

Simple assignment (only for `var` variables):

```chi
var x = 5
x = 10
```

Compound assignment operators:

```chi
var x = 10
x += 5    // x = x + 5
x -= 3    // x = x - 3
x *= 2    // x = x * 2
x /= 4   // x = x / 4
```

Record field assignment:

```chi
val r = { a: 10 }
r.a = 5
```

Array index assignment:

```chi
var a = [1, 2, 3]
a[1] = 10
```

## Operators

### Arithmetic

| Operator | Description |
|----------|-------------|
| `+` | Addition (also string concatenation) |
| `-` | Subtraction |
| `*` | Multiplication |
| `/` | Division |
| `%` | Modulo |

```chi
5 + 3       // 8
"hello " + "world"  // "hello world"
10 - 4      // 6
3 * 7       // 21
15 / 3      // 5
10 % 3      // 1
-5          // negation
```

### Comparison

| Operator | Description |
|----------|-------------|
| `==` | Equal |
| `!=` | Not equal |
| `<` | Less than |
| `<=` | Less than or equal |
| `>` | Greater than |
| `>=` | Greater than or equal |

```chi
5 == 5      // true
5 != 3      // true
3 < 5       // true
```

### Logical

| Operator | Description |
|----------|-------------|
| `&&` | Logical AND |
| `\|\|` | Logical OR |
| `!` | Logical NOT |

```chi
true && false   // false
true || false   // true
!true           // false
```

### Bitwise

| Operator | Description |
|----------|-------------|
| `&` | Bitwise AND |
| `\|` | Bitwise OR |
| `<<` | Left shift |
| `>>` | Right shift |

### Type operators

| Operator | Description |
|----------|-------------|
| `is` | Type check (returns `bool`) |
| `as` | Type cast |

```chi
val x: int | string = 42
x is int       // true
x is string    // false

"5" as int     // cast string to int
```

### Weave operator (`~>`)

The weave operator pipes a value into an expression, substituting `_` (placeholder) with the piped value:

```chi
"hello" ~> toUpper(_)

// Chaining
"2hello"
    ~> toUpper(_)
    ~> _[0] as int
    ~> 2 + _
```

The weave operator creates a temporary variable for the left-hand side and replaces every `_` on the right-hand side with that variable.

## Blocks

Blocks are sequences of expressions enclosed in curly braces. The value of a block is the value of its last expression:

```chi
val result = {
    val x = 5
    val y = 10
    x + y
}
// result is 15
```

Blocks can be immediately invoked:

```chi
{
    var i = 0
    while i < 5 { i += 1 }
    i
}()
// evaluates to 5
```

## Field Access

Access record fields with dot notation:

```chi
val person = { name: "Alice", age: 30 }
person.name    // "Alice"
person.age     // 30
```

## Index Access

Access array elements with square brackets (1-based indexing in the Lua runtime):

```chi
val arr = [10, 20, 30]
arr[1]     // 10
arr[2]     // 20
```

## Grouping

Parentheses group expressions:

```chi
(2 + 3) * 4   // 20
```

## Return

Use `return` to exit a function early:

```chi
fn abs(x: int): int {
    if x < 0 { return -x }
    x
}
```

Without `return`, the last expression in a function body is its return value.
