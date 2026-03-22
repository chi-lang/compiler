# Functions and Lambdas

Chi has first-class functions. Functions can be passed as arguments, returned from other functions, and stored in variables.

## Named Functions

Define named functions with `fn`:

```chi
fn add(a: int, b: int): int {
    a + b
}
```

The last expression in the function body is its return value. You do not need an explicit `return` for the final expression.

### Return type inference

If the return type can be inferred, you can omit it (though explicit return types are common for top-level functions):

```chi
fn double(x: int): int {
    x * 2
}
```

### Visibility

Use `pub` to make functions visible from other packages:

```chi
pub fn greet(name: string): string {
    "Hello $name!"
}
```

### Early return

Use `return` to exit a function early:

```chi
fn abs(x: int): int {
    if x < 0 { return -x }
    x
}
```

## Lambda Expressions

Lambdas are anonymous functions defined with curly braces:

```chi
val double = { x -> x * 2 }
double(5)  // 10
```

### Multiple parameters

```chi
val add = { a, b -> a + b }
add(3, 4)  // 7
```

### No parameters

```chi
val greet = { "hello" }
greet()  // "hello"
```

### With type annotations

Lambda parameters can have optional type annotations:

```chi
val add = { a: int, b: int -> a + b }
```

### Multi-expression body

A lambda body can contain multiple expressions. The last expression is the return value:

```chi
val compute = { x ->
    val doubled = x * 2
    val incremented = doubled + 1
    incremented
}
compute(5)  // 11
```

## Default Arguments

Both named functions and lambdas support default argument values:

```chi
fn foo(a: int, b: int = 5): int {
    a + b
}
foo(1)      // 6 (b defaults to 5)
foo(1, 10)  // 11
```

Default values can reference previous parameters:

```chi
fn foo(a: int, b: int = a): int {
    a + b
}
foo(1)  // 2 (b defaults to a, which is 1)
```

Default values can be expressions, including function calls:

```chi
fn defaultValue(): int { 5 }
fn bar(a: int, b: int = defaultValue()): int {
    a + b
}
bar(1)  // 6
```

Lambdas also support default arguments:

```chi
val foo = { a, b = 5 -> a + b }
foo(1)  // 6
```

## Method-Style Invocation (UFCS)

Any function can be called using dot notation on its first argument. This is sometimes called "Uniform Function Call Syntax" (UFCS):

```chi
fn greet(who: string): string {
    "Hello $who!"
}

// Both are equivalent:
greet("World")
"World".greet()
```

This also works with multi-argument functions:

```chi
fn add(a: int, b: int): int { a + b }

// Both are equivalent:
add(1, 2)
1.add(2)
```

This is how "methods" work in Chi -- there are no special method declarations. Any function whose first argument type matches the receiver can be called with dot notation. The compiler looks up functions in this order:

1. Record field access (if the receiver is a record with that field name)
2. Local functions in scope
3. Functions in the package where the receiver's type is defined

```chi
fn bar(i: int): float { 5.0 }
val foo = 5
foo.bar()   // calls bar(foo), returns 5.0
```

## Higher-Order Functions

Functions can accept and return other functions:

```chi
fn apply(f: (int) -> int, x: int): int {
    f(x)
}

apply({ x -> x * 2 }, 5)  // 10
```

### Trailing lambda syntax

When the last argument of a function is a function type, you can pass it as a trailing lambda:

```chi
fn doTwice(f: () -> unit) {
    f()
    f()
}

// These are equivalent:
doTwice({ println("hello") })
doTwice { println("hello") }
```

## Generic Functions

Functions can have type parameters:

```chi
fn identity[T](x: T): T { x }

fn map[T, R](arr: array[T], f: (T) -> R): array[R] {
    val result = []
    var i = 0
    while i < arr.size() {
        result.add(f(arr[i]))
        i += 1
    }
    result
}

map([1, 2, 3], { x -> x * 2 })
```

Type parameters are declared in square brackets after the function name. They are usually inferred from the arguments.

## Closures

Lambdas capture variables from their enclosing scope:

```chi
var counter = 0
val increment = {
    counter += 1
    counter
}

increment()  // 1
increment()  // 2
increment()  // 3
```

## Immediately Invoked Function Expressions

Blocks (which are lambdas with no arguments) can be immediately invoked:

```chi
val result = {
    var sum = 0
    var i = 1
    while i <= 10 {
        sum += i
        i += 1
    }
    sum
}()
// result is 55
```
