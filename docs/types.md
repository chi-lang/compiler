# Type System

Chi has a **Hindley-Milner** based type inference system with **structural typing**. This means you can often omit type annotations and the compiler will figure out the types for you, and types are matched by their structure rather than their name.

## Primitive Types

| Type | Description | Examples |
|------|-------------|---------|
| `int` | Integer numbers | `0`, `42`, `-5` |
| `float` | Floating-point numbers | `3.14`, `0.0`, `100.0` |
| `bool` | Boolean values | `true`, `false` |
| `string` | Text strings | `"hello"`, `""` |
| `unit` | Absence of value | `unit` |
| `any` | Top type, accepts any value | -- |

## Type Inference

Chi uses Hindley-Milner type inference, which means you can safely omit many type declarations and the compiler will determine the types automatically:

```chi
val x = 42              // inferred as int
val name = "hello"      // inferred as string
val flag = true         // inferred as bool
val numbers = [1, 2, 3] // inferred as array[int]
```

This is especially powerful with lambda functions:

```chi
val id = { a -> a }
id(5)       // inferred: int
id("hello") // inferred: string
```

The `id` function has the inferred type `'a -> 'a`, where `'a` is a **type variable** -- it accepts any type without constraints. The relationship between input and output types is preserved: whatever goes in, the same type comes out.

## Structural Typing

Most languages use **nominal** type systems where types are matched by name. Chi uses **structural typing**, where types are matched by their shape (fields and field types).

Consider this example:

```chi
type Person = { name: string }
type PersonWithAddress = { name: string, address: string }

fn greet(person: Person): string {
    "Hello $person.name!"
}
```

If you call `greet({ name: "John", address: "Some St. 87" })`, the type system accepts it because the value structurally matches `Person` -- it has all the required fields. The extra `address` field does not cause a type error.

This means the type system ensures you won't attempt to read a field from a record that doesn't contain it, without requiring rigid name-based type hierarchies.

## Type Aliases

Use `type` to create named aliases for types:

```chi
type Name = string
type Point = { x: int, y: int }
type Callback = (int) -> string
type Result = int | string
```

Type aliases are transparent -- they do not create new distinct types. They are just names for existing structural types.

## Function Types

Function types are written as `(ArgTypes) -> ReturnType`:

```chi
type IntToString = (int) -> string
type BinaryOp = (int, int) -> int
type Predicate = (string) -> bool
type Thunk = () -> int
```

## Record Types

Record types describe objects with named fields:

```chi
type Person = { name: string, age: int }
type Point = { x: float, y: float }
```

Records are created as literals:

```chi
val p: Person = { name: "Alice", age: 30 }
```

Thanks to structural typing, any record with at least the required fields is accepted.

## Array Types

Array types are written as `array[ElementType]`:

```chi
type IntArray = array[int]
type Names = array[string]
```

Arrays are created as literals:

```chi
val nums: array[int] = [1, 2, 3]
val empty: array[string] = []
```

## Sum Types (Union Types)

Sum types represent a value that can be one of several types, separated by `|`:

```chi
type IntOrString = int | string
type MaybeInt = int | unit

val x: int | string = 42
val y: int | string = "hello"
```

When `unit` is one of the alternatives, the type is automatically treated as an **Option** type:

```chi
val maybe: int | unit = 5
val nothing: int | unit = unit
```

### Type narrowing with `is`

Use the `is` operator to check which variant a sum type holds:

```chi
val x: int | string = 42

if x is int {
    println("it's an integer")
}

if x is string {
    println("it's a string")
}
```

### Casting with `as`

Use `as` to cast between types:

```chi
val x: int | string = 42
val n = x as int

// String to number conversion
val s = "5"
val n = s as int
```

## Generic Types (Type Parameters)

Functions and type aliases can have type parameters:

```chi
fn map[T, R](arr: array[T], f: (T) -> R): array[R] {
    val result = []
    var i = 0
    while i < arr.size() {
        result.add(f(arr[i]))
        i += 1
    }
    result
}

map([1, 2], { a -> a as string })
```

Type parameters are declared in square brackets after the function or type name:

```chi
type Pair[A, B] = { first: A, second: B }
```

When calling a generic function, type parameters are usually inferred:

```chi
val id = { a -> a }
id(5)        // T is inferred as int
id("hello")  // T is inferred as string
```

You can also provide explicit type parameters:

```chi
someFunction[int, string](42, "hello")
```

## Variant Types (Data Types)

Define algebraic data types using `data`:

### Simple form (single constructor)

```chi
data Point(x: int, y: int)
data pub Node(value: int, next: Node)
```

### Full form (multiple constructors)

```chi
data Shape =
    Circle(radius: float)
    | Rectangle(width: float, height: float)

data Option[T] =
    Some(value: T)
    | None
```

Fields can be marked `pub` for public access:

```chi
data Person(pub name: string, pub age: int)
```

## Recursive Types

Chi supports recursive type definitions. A type can reference itself:

```chi
data List[T] =
    Cons(head: T, tail: List[T])
    | Nil
```

The compiler detects self-references and wraps them in a `Recursive` type internally.

## Traits

Traits define interfaces that describe a set of function signatures:

```chi
trait Showable[T] {
    fn show(value: T): string
}
```

Traits declare function signatures without implementations.
