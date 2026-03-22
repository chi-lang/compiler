# Standard Library

Chi provides a set of built-in functions and methods available through the `std` module. The standard library is implemented partly in Chi and partly in Lua/Java and is automatically loaded when `CHI_HOME` is configured.

## Auto-Imported Functions (`std/lang`)

These functions are available in every Chi program without an explicit import:

### `println(value: any): unit`

Prints a value followed by a newline:

```chi
println("Hello, World!")
println(42)
println([1, 2, 3])
println({ name: "Alice" })
```

Values are converted to their string representation:
- `unit` prints as `"unit"`
- Functions print as `"<function>"`
- Arrays print as `[1, 2, 3]`
- Records print as `{name: Alice, age: 30}`

### `print(value: any): unit`

Prints a value without a trailing newline:

```chi
print("Hello, ")
print("World!")
// Output: Hello, World!
```

### `eval(code: string): any`

Evaluates a Chi code string at runtime and returns the result:

```chi
val result = eval("2 + 2")
println(result)  // 4
```

## Functions Available via Import

### `std/lang`

#### `embedLua(code: string): 'a`

Embeds raw Lua code into the compiled output. The string argument must be a literal:

```chi
import std/lang { embedLua }
embedLua("print('hello from lua')")
```

This is an escape hatch for accessing Lua functionality directly. The return type is a generic type variable, meaning Chi treats the result as whatever type you use it as.

#### `luaExpr(code: string): 'a`

Evaluates a Lua expression and returns its value. Unlike `embedLua`, this is an expression (returns a value):

```chi
import std/lang { luaExpr }
val result = luaExpr("2 + 2")
```

#### `reload(module: string): unit`

Reloads a previously loaded module:

```chi
import std/lang { reload }
reload("mymod/mypkg")
```

#### `loadModule(path: string): unit`

Loads a compiled Chi module file (`.chim`) from the filesystem:

```chi
import std/lang { loadModule }
loadModule("/path/to/module.chim")
```

### `std/lang.any`

#### `toString(value: any): string`

Converts any value to its string representation:

```chi
import std/lang.any { toString }
val s = toString(42)     // "42"
val s2 = toString(true)  // "true"
```

## String Methods

Chi strings are Java `String` objects at runtime. The following methods are available on string values:

### `len(): int`

Returns the length of the string:

```chi
"hello".len()      // 5
"".len()           // 0
```

### `find(substring: string): int`

Finds the position of a substring:

```chi
"hello".find("ll")  // position of "ll"
```

### `substring(start: int, end: int): string`

Extracts a substring:

```chi
"hello".substring(1, 2)
```

### `toLower(): string`

Converts the string to lowercase:

```chi
"HELLO".toLower()   // "hello"
```

### `toUpper(): string`

Converts the string to uppercase:

```chi
"hello".toUpper()   // "HELLO"
```

### `replace(old: string, new: string): string`

Replaces the first occurrence of a substring:

```chi
"hello".replace("ll", "xx")   // "hexxo"
```

### `replaceAll(old: string, new: string): string`

Replaces all occurrences of a substring:

```chi
"hello".replaceAll("l", "x")  // "hexxo"
```

### `codePointAt(index: int): int`

Returns the Unicode code point at the given index:

```chi
"hello".codePointAt(1)  // code point of 'h'
```

### `isEmpty(): bool`

Returns `true` if the string is empty:

```chi
"".isEmpty()       // true
"hello".isEmpty()  // false
```

### `trim(): string`

Removes leading and trailing whitespace:

```chi
"  hello  ".trim()  // "hello"
```

### `contains(substring: string): bool`

Returns `true` if the string contains the given substring:

```chi
"hello".contains("ell")  // true
"hello".contains("xyz")  // false
```

### `startsWith(prefix: string): bool`

Returns `true` if the string starts with the given prefix:

```chi
"hello".startsWith("he")   // true
"hello".startsWith("lo")   // false
```

### `endsWith(suffix: string): bool`

Returns `true` if the string ends with the given suffix:

```chi
"hello".endsWith("lo")   // true
"hello".endsWith("he")   // false
```

### `split(delimiter: string): array[string]`

Splits the string by the given delimiter:

```chi
"hello".split("l")  // splits around "l"
```

### `reverse(): string`

Returns the string reversed:

```chi
// Available via chistr runtime
```

### `trimStart(): string`

Removes leading whitespace:

```chi
"  hello".trimStart()  // "hello"
```

### `trimEnd(): string`

Removes trailing whitespace:

```chi
"hello  ".trimEnd()  // "hello"
```

### `codePoints(): () -> int | unit`

Returns an iterator function over the Unicode code points of the string:

```chi
// Can be used with for loops via the generator pattern
```

## String Concatenation

Strings support concatenation with the `+` operator:

```chi
"hello " + "world"  // "hello world"
```

String comparison uses `==` and `!=`:

```chi
"hello" == "hello"  // true
"hello" != "world"  // true
```

## Printing Conventions

The `println` and `print` functions convert values to strings using the following rules:

| Type | Representation |
|------|---------------|
| `int` | The number (e.g., `42`) |
| `float` | The number (e.g., `3.14`) |
| `bool` | `true` or `false` |
| `string` | The string value |
| `unit` / `nil` | `unit` |
| Function | `<function>` |
| Array | `[elem1, elem2, ...]` |
| Record | `{key1: val1, key2: val2, ...}` |
