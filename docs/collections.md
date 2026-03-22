# Collections

Chi provides two primary collection types: **arrays** (ordered, indexed sequences) and **records** (named field collections).

## Arrays

### Creating arrays

Arrays are created with square bracket syntax:

```chi
val numbers = [1, 2, 3, 4, 5]
val names = ["Alice", "Bob", "Charlie"]
val empty = []
val mixed: array[int | string] = [1, "two", 3]
```

### Array type

The type of an array is written `array[ElementType]`:

```chi
val nums: array[int] = [1, 2, 3]
val strs: array[string] = ["hello", "world"]
```

The element type is inferred from the contents:

```chi
val nums = [1, 2, 3]  // inferred as array[int]
```

### Indexing

Access elements with square bracket notation. Array indices are **1-based** in the Lua runtime:

```chi
val arr = [10, 20, 30]
arr[1]   // 10
arr[2]   // 20
arr[3]   // 30
```

### Index assignment

Assign to array indices (the array variable itself can be `val` -- mutability applies to the variable binding, not the contents):

```chi
var a = [1, 2, 3]
a[1] = 10
println(a[1])  // 10
```

### Inline indexing

You can index an array literal directly:

```chi
[1, 2, 3][1]  // 1
```

### Iterating over arrays

Use `for` to iterate over array elements:

```chi
for item in [1, 2, 3] {
    println(item)
}
```

With index and value:

```chi
for idx, value in ["a", "b", "c"] {
    println("$idx: $value")
}
```

When iterating with a single variable, you get the values. With two variables, the first is the 1-based index and the second is the value.

### Type checking

Use `is` to check if a value is an array:

```chi
val x: array[int] | string = [1, 2]
if x is array[int] {
    println("it's an array")
}
```

## Records

Records are structural objects with named fields.

### Creating records

```chi
val person = { name: "Alice", age: 30 }
val point = { x: 0, y: 0 }
val empty = {}
```

### Record types

Record types are written with curly braces listing field names and types:

```chi
type Person = { name: string, age: int }
type Point = { x: float, y: float }
```

Thanks to structural typing, any record that has at least the required fields is accepted:

```chi
type HasName = { name: string }

fn greet(x: HasName): string {
    "Hello $x.name!"
}

// All of these work:
greet({ name: "Alice" })
greet({ name: "Bob", age: 25 })
greet({ name: "Charlie", email: "c@example.com" })
```

### Field access

Access fields with dot notation:

```chi
val person = { name: "Alice", age: 30 }
person.name   // "Alice"
person.age    // 30
```

Inline field access on a record literal:

```chi
{ a: 10 }.a   // 10
```

### Field assignment

Assign to record fields:

```chi
val r = { a: 10 }
r.a = 5
r.a   // 5
```

### Iterating over records

Use `for` with two variables to iterate over key-value pairs:

```chi
var result = ""
for k, v in { a: 1, b: 2 } {
    result = "$k=$v "
}
```

The keys are strings, and the iteration order depends on the Lua runtime.

### Type checking

Use `is` to check if a value is a record:

```chi
val x: { name: string } | int = { name: "test" }
if x is { name: string } {
    println("it's a record")
}
```

## Variant Types (Algebraic Data Types)

For tagged collections (discriminated unions), use `data` declarations:

### Simple form

```chi
data Point(x: int, y: int)
```

This creates both a type `Point` and a constructor function `Point(x, y)`.

### Full form with multiple constructors

```chi
data Shape =
    Circle(radius: float)
    | Rectangle(width: float, height: float)
```

### With type parameters

```chi
data Option[T] =
    Some(value: T)
    | None
```

### Field visibility

Mark fields as `pub` for public access:

```chi
data Person(pub name: string, pub age: int)
```

Constructors themselves can also be `pub`:

```chi
data Result[T] =
    pub Ok(pub value: T)
    | pub Err(pub message: string)
```

## String as a Collection

Strings in Chi are Java String objects in the runtime. They support various operations documented in the [Standard Library](stdlib.md) reference. Strings can be iterated over by code points:

```chi
import std/lang { luaExpr }
val s = "hello"
s.len()         // 5
s.substring(1, 2)  // substring
```
