# Control Flow

Chi provides several control flow constructs: conditional expressions, loops, pattern-like branching with `when`, and algebraic effects.

## If / Else

`if`/`else` is an **expression** in Chi -- it returns a value:

```chi
val result = if true { 5 } else { 10 }
// result is 5
```

### Basic if

```chi
if x > 0 {
    println("positive")
}
```

### If-else

```chi
if x > 0 {
    println("positive")
} else {
    println("non-positive")
}
```

### Else-if chains

```chi
if x > 0 {
    println("positive")
} else if x == 0 {
    println("zero")
} else {
    println("negative")
}
```

### As an expression

Since `if` is an expression, you can use it anywhere a value is expected:

```chi
val sign = if x > 0 { "positive" } else if x == 0 { "zero" } else { "negative" }
```

## While Loop

The `while` loop repeatedly executes a block as long as the condition is true:

```chi
var i = 0
while i < 5 {
    println(i)
    i += 1
}
```

`while` loops return `unit`.

### Break and Continue

Use `break` to exit a loop early:

```chi
var i = 0
while true {
    if i >= 5 { break }
    println(i)
    i += 1
}
```

Use `continue` to skip to the next iteration:

```chi
var i = 0
while i < 10 {
    i += 1
    if i % 2 == 0 { continue }
    println(i)  // prints odd numbers only
}
```

## For Loop

The `for` loop iterates over arrays, records, and generator functions.

### Iterating over arrays

```chi
for item in [1, 2, 3] {
    println(item)
}
```

With index and value (index is 1-based):

```chi
for idx, value in ["a", "b", "c"] {
    println("$idx: $value")
}
```

### Iterating over records

With two variables, iterate over key-value pairs:

```chi
for key, value in { name: "Alice", age: 30 } {
    println("$key = $value")
}
```

### Iterating with generator functions

A generator function returns `T | unit` -- it returns `unit` to signal end of iteration:

```chi
var x = 0
fn gen(): int | unit {
    x += 1
    if x <= 3 { x }
}

var sum = 0
for a in gen {
    sum += a
}
// sum is 6
```

You can also use a lambda as a generator:

```chi
var x = 0
var sum = 0
for a in {
    x += 1
    if x <= 3 { x }
} {
    sum += a
}
```

### Stateful generator functions

For generators that need explicit state, use the extended form with state and initial value:

```chi
fn gen(state: {}, last: int): int | unit {
    val x = if last is unit { 0 } else { last as int } + 1
    return if x <= 3 { x } else { unit }
}

var sum = 0
for a in gen, {}, 0 {
    sum += a
}
// sum is 6
```

The syntax is `for var in generator, stateValue, initialValue { ... }`. The generator receives the state object and the last yielded value as arguments.

### Stateful lambda generators

```chi
var sum = 0
for a in { state: { v: int }, last ->
    state.v = state.v + 1
    if state.v < 4 { state.v }
}, { v: 0 }, 0 {
    sum += a
}
// sum is 6
```

## When Expression

`when` is a multi-branch conditional expression, similar to a `switch` or `cond` in other languages:

```chi
val x = 10
when {
    x < 0  -> println("negative")
    x == 0 -> println("zero")
    else   -> println("positive")
}
```

Each case has a condition expression followed by `->` and a body. The `else` case is optional and acts as a fallback.

`when` is an expression and returns a value:

```chi
val label = when {
    x < 0  -> "negative"
    x == 0 -> "zero"
    else   -> "positive"
}
```

Cases can have block bodies:

```chi
when {
    x > 100 -> {
        println("large number")
        println("very large indeed")
    }
    else -> println("small number")
}
```

## Type Checking with `is`

The `is` operator checks the runtime type of a value. This is especially useful with sum types:

```chi
val x: int | string = 42

if x is int {
    println("integer")
}

if x is unit {
    println("no value")
}
```

Supported type checks:
- Primitive types: `int`, `float`, `bool`, `string`, `unit`
- Arrays: `array[T]`
- Records: `{ field: type }`
- Function types

## Algebraic Effects

Chi supports algebraic effects -- a way to define and handle side effects in a composable manner.

### Defining effects

Define an effect with the `effect` keyword:

```chi
effect greet(name: string): string
```

An effect declaration specifies:
- The effect name
- Its parameters
- Its return type

Effects can be marked `pub` for cross-package access:

```chi
pub effect log(level: int, msg: string): unit
```

### Invoking effects

Call an effect like a regular function inside a `handle` block:

```chi
val result = handle {
    greet("World")
} with {
    greet(name) -> resume("Hello $name!")
}
// result is "Hello World!"
```

### Handle expression

The `handle ... with { ... }` expression:

1. Runs the **body** block
2. When an effect is invoked inside the body, execution suspends
3. The matching **handler case** runs
4. `resume(value)` sends the value back to the point where the effect was invoked
5. The result of the `handle` expression is the result of the body block

### Multiple effect handlers

```chi
effect ask(prompt: string): string
effect log(msg: string): unit

handle {
    val name = ask("What is your name?")
    log("User said: $name")
    "Done: $name"
} with {
    ask(prompt) -> resume("Alice")
    log(msg) -> {
        println(msg)
        resume(unit)
    }
}
```

### Effects with type parameters

Effects can be generic:

```chi
effect read[T](): T
```

## Return

Use `return` to exit a function early. This also works inside control flow constructs:

```chi
fn findFirst(arr: array[int], target: int): int | unit {
    var i = 1
    while i <= 3 {
        if arr[i] == target { return i }
        i += 1
    }
    unit
}
```

Without `return`, the last expression in a function/block is its value.
