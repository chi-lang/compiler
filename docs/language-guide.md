# Chi Language Guide

This document describes the Chi programming language as implemented by the compiler in this repository. It focuses on the syntax that is enforced by `ChiLexer.g4` and `ChiParser.g4`, the semantics that follow from the compiler pipeline, and the surrounding tooling so you can read, write, and run Chi programs confidently.

## Contents
- [Overview](#overview)
- [Tooling and Workflow](#tooling-and-workflow)
- [Program Structure](#program-structure)
- [Lexical Structure](#lexical-structure)
- [Type System Overview](#type-system-overview)
- [Declarations and Definitions](#declarations-and-definitions)
- [Expressions and Control Flow](#expressions-and-control-flow)
- [Modules and the Standard Library](#modules-and-the-standard-library)
- [Runtime, REPL, and Native Images](#runtime-repl-and-native-images)
- [Appendix: Quick Reference](#appendix-quick-reference)

## Overview
Chi is an expression-oriented, statically typed language with Hindley-Milner style inference and structural typing. Everything is an expression: declarations, loops, and control flow nodes all produce values. The language embraces algebraic data types, traits, and algebraic effects with explicit handlers. The compiler targets the JVM (Kotlin/Java implementation) and ships scripts for running on HotSpot or ahead-of-time via GraalVM.

Key characteristics:
- **Expression first**: the last expression in any block becomes the block's value, which makes custom control structures and chained computations easy.
- **Structural types**: record compatibility is decided by shape, not by nominal names. See [`typesystem.md`](./typesystem.md) for the rationale and examples.
- **Algebraic data & traits**: use `data` to build sum types and `trait` to describe capabilities.
- **Effects and handlers**: declare operations with `effect` and install interpreters with `handle { ... } with { ... }`.
- **Host interoperability**: the runtime embeds LuaJIT (via `std/lang.luaExpr`) and can be packaged as a GraalVM native executable for console tooling.

## Tooling and Workflow

### Building and testing
- Run `./gradlew build` to compile the compiler, regenerate the ANTLR parser, and execute the full test suite.
- Run `./gradlew test` when you only need verification.
- Run `./gradlew run` to start the compiler CLI (interactive REPL by default). Use `.exit` to leave the REPL session.

The Gradle build targets JVM 11 and uses Kotlin 1.6.21. Grammar generation emits Java sources under `src/main/java/gh/marad/chi/core/antlr` at build time.

### Running Chi code
- After `./gradlew shadowJar`, invoke `./chi.sh path/to/file.chi` (or `chi.bat` on Windows) to run a script using the shaded JAR (`gh.marad.chi.MainKt`).
- Use `test.chi` or `native/seed.chi` as starting points; both demonstrate how to import the standard library and call into Lua via `std/lang { luaExpr }`.
- The CLI also works without a script; simply run `./gradlew run` and type Chi code directly.

### Native builds
The `native/` folder documents how to build a GraalVM binary. Follow `native/readme.md`:
1. Produce `build/libs/chi-all.jar` with `./gradlew shadowJar`.
2. Collect configuration by running the agent (`native/run_with_agent.{bat,sh}`) and exercising the REPL.
3. Build with `native/build.{bat,sh}` to obtain a standalone executable.

## Program Structure

### Files, packages, and modules
- Chi source files end with `.chi` and may start with an optional package declaration: `package core/math`. The syntax is `package <module>/<package>`, where each side may contain dot-separated segments (`package gh.marad/std.lang`).
- Package declarations influence import resolution and generated module names, but they do not affect the filesystem layout directly.

### Imports
Use `import <module>/<package>` to pull in definitions. You can alias the entire import or select individual symbols:
```chi
import std/lang
import std/lang as lang
import std/lang { luaExpr, println as log }
```
Syntax rules:
- Module and package paths mirror the ones accepted by `package` declarations.
- `{ ... }` restricts the import to the listed entries.
- `as` can rename either the entire package (`import foo/bar as baz`) or specific members (`import foo/bar { value as alias }`).

### Top-level forms
A `program` consists of any mix of:
- `type` aliases.
- `data` declarations.
- `trait` definitions.
- Any expression (including `val`/`var` bindings, `fn` definitions, and module-initialization code).
Top-level expressions run eagerly when the module is loaded, making it simple to register effects or initialize constants.

### Visibility
Use the `pub` modifier in front of declarations that must be exported outside the package. `pub` is allowed on `val`/`var`, `fn`, `data`, constructors, and even individual fields inside simplified `data` declarations.

## Lexical Structure

### Identifiers and keywords
Identifiers (`ID`) start with a letter and may contain letters, digits, or underscores. The language is case-sensitive.

Reserved keywords:
```
pub val var fn if else as while for in package import data when match is break continue effect handle with return trait type unit true false
```
`match` is currently reserved by the lexer even though the parser uses `when` for conditional branching.

### Literals
- **Numbers**: decimal integers or floats (`42`, `3.14`).
- **Booleans**: `true`, `false`.
- **Strings**: double-quoted with rich interpolation. Use `$name` for identifier interpolation and `${ expression }` for arbitrary code. Supports escapes (`\n`, `\t`, `\r`, `\"`, `\\`, `\$`).
- **Unit**: the literal `unit` denotes the absence of a value and is the default return type when unspecified.
- **Arrays**: `[expr1, expr2, ...]`.
- **Records**: `{ field: value, other: value }`.

### Comments and whitespace
- Single-line comments start with `//`.
- Block comments use `/* ... */`.
- Line endings (`NEWLINE`) matter for separating top-level forms, but inside blocks whitespace is generally free-form. The parser rule `ws` admits spaces, tabs, and optional newlines almost everywhere, so idiomatic Chi emphasizes readability over strict formatting.

## Type System Overview
Chi uses Hindley-Milner inference plus structural typing:
- Primitive types include `int`, `float`, `bool`, `string`, and `unit`.
- Arrays and records are structural. Two record types are compatible when the consumer's required fields are present in the provider, regardless of extra fields.
- Function types use arrow notation: `(Arg1, Arg2) -> Result`. Single-argument functions may omit the comma: `(Arg) -> Result`.
- Sum types are expressed with `|` (`Result[T] = Ok(value: T) | Error(message: string)`).
- Type constructors accept type parameters in square brackets: `Option[int]`, `Map[string, int]`.
- Qualified names let you refer to external modules: `std::lang::String`.

See [`typesystem.md`](./typesystem.md) for a deeper dive into inference, polymorphism, and structural compatibility.

## Declarations and Definitions

### Values and variables
`val` introduces an immutable binding; `var` creates a mutable one. Both can appear anywhere an expression may occur.
```chi
pub val answer: int = 42
var counter = 0
counter += 1
```
Type annotations are optional because the compiler infers types. Annotate when you want documentation or to restrict inference.

### Functions
Declare named functions with `fn`:
```chi
pub fn map[T, U](value: T, transform: fn(T) -> U): U {
    transform(value)
}
```
Highlights:
- Generic type parameters go in `[]`.
- Arguments require names; annotate types with `:` and provide default values with `=` when needed.
- The return type follows `:` after the parameter list. Omitting it defaults to `unit`.
- Functions are expressions; you can nest them inside other blocks if desired.

### Lambdas
Lambda expressions reuse brace syntax:
```chi
val twice = { x: int -> x * 2 }
val immediate = { println("side effect") }()
```
- Parameters use the same syntax as regular arguments, including optional types and defaults.
- The body is a sequence of expressions; the value of the last expression becomes the lambda's result.
- Supplying a trailing block right after a function call (`doWork { ... }`) invokes the `FnCallLambdaExpr` rule.

### Type aliases
Use `type` to rename or reshape types:
```chi
type Pair[T, U] = { first: T, second: U }
pub type Json = { value: string }
```
Aliases participate in structural typing and behave like the types they reference.

### Variant (`data`) types
`data` introduces algebraic data types. There are two forms:
```chi
// Full form with explicit constructors
pub data Result[T] = Ok(value: T) | Error(message: string)

// Simplified form (single constructor with named fields)
data Point(x: float, y: float)
```
- Constructors can be marked `pub` individually.
- Fields inside simplified declarations can also be individually `pub` to expose structural fields outside the defining module.
- Each constructor becomes a callable function (e.g., `Ok(42)`).

### Traits
Traits are structural contracts that list required function signatures:
```chi
trait Ord[T] {
    fn compare(left: T, right: T): int
}
```
Implementations are provided elsewhere (usually via functions that accept trait instances or via compiler-supported impl blocks). Trait function signatures accept generics, optional default values, and explicit return types.

### Effects
Effects describe operations that may be interpreted later:
```chi
effect Console.log(message: string): unit
```
- The effect name behaves like a function call inside effectful code.
- Use `handle` blocks to install interpreters (see [Effect handling](#effect-handling)).
- Effects support generics and optional return types (defaulting to `unit`).

## Expressions and Control Flow

### Blocks and scope
`{ ... }` introduces a new lexical scope. All declarations inside the block remain visible until the block ends, and the block evaluates to the value of its last expression.
```chi
val message = {
    val prefix = "Hello"
    "$prefix, Chi!"
}
```

### Function calls and generics
- Ordinary calls look like `callee(arg1, arg2)`.
- Provide explicit type arguments with square brackets: `unwrap[int](maybeValue)`.
- A trailing lambda after the argument list is treated as the final argument: `withResource(resource) { use(it) }`.

### Conditionals
- `if` expressions require a condition and block: `if condition { ... } else { ... }`. `else` clauses may nest additional `if` expressions.
- `when` is a multi-branch guard:
```chi
val response = when {
    value > 0 -> "positive"
    value < 0 -> "negative"
    else -> "zero"
}
```
Each branch body can be a block or an expression.

### Loops
- `while` repeats while its condition expression is truthy: `while keepRunning() { tick() }`.
- `for` iterates over iterable expressions:
```chi
for key, value in map {
    println("$key -> $value")
}
```
  - The second identifier is optional; omit it for single-value iteration.
  - An optional `, stateExpr, initExpr` suffix exists for advanced generator patterns. The compiler threads the additional expressions to support accumulator-based desugarings; ordinary user code can ignore this clause.
- Use `break` and `continue` inside loops for flow control.

### Collections and records
- Records: `{ name: "chi", version: 1 }`. Access fields with dot notation: `record.name`. Update fields with assignment: `record.version = 2`.
- Arrays: `[1, 2, 3]`; index with `arr[0]` and assign with `arr[0] = 42`.
- Both records and arrays participate in structural typing.

### Operators and assignments
Supported arithmetic and logical operators:
```
+ - * / %
<< >> & |
== != < <= > >=
&& || !
```
Compound assignments (`+=`, `-=`, `*=`, `/=`) modify existing bindings. Unary negation (`-value`) and logical negation (`!value`) work on numbers and booleans respectively.

### Type tests and casts
- `expr is Type` checks whether `expr` conforms to `Type`.
- `expr as Type` performs an explicit cast (failing at runtime if incompatible).

### Effect handling
Wrap effectful code in a handler:
```chi
handle {
    Console.log("Hello")
} with {
    Console.log(message) -> println(message)
}
```
- `handle` takes a block and dispatches every invoked effect to the first matching case.
- Cases resemble pattern matching: `EffectName(param1, param2) -> body`.
- Handlers may fall back to a default (by omitting a case or delegating to another handler).

### Weaving operator (`~>`)
The parser treats `input ~> template` as a dedicated expression. Use it with the standard template/weaving APIs (for example `value ~> htmlTemplate`). Conceptually it pipes the left-hand expression into the template expression on the right, enabling embedded DSLs.

### Placeholder `_`
`_` is a special placeholder expression. It is primarily used inside templates or higher-order APIs that supply the missing argument later. Using `_` outside such contexts results in a compile error.

### Returns and early exits
- `return expr` exits the nearest function or lambda, optionally omitting `expr` to return `unit`.
- `break` and `continue` affect the nearest loop.

## Modules and the Standard Library

- Organize code into modules using the `package` declaration. Use `pub` strategically to expose APIs.
- The standard library is bundled inside the compiler JAR and is accessed via `std/...` packages. Example (`native/seed.chi`):
```chi
import std/lang { luaExpr }

{
    val s = "hello"
    println(s)
    println(s.len())
    println(luaExpr("s:equals(\"other\")"))
}()
```
- Records and primitives gain methods through the standard library (e.g., `string.len()`, `string.replace`). Method syntax is sugar over calling functions with the receiver as the first argument.
- `std/lang.luaExpr` bridges to embedded LuaJIT. Pass a Lua expression as a string; it executes with Chi bindings (`s` above) exposed to Lua.

## Runtime, REPL, and Native Images

- **CLI / REPL**: `./gradlew run` starts the REPL. Use `.exit` to terminate. Anything you can place in a `.chi` file also works interactively.
- **Scripts**: `./chi.sh script.chi args` runs a file against the shaded JAR. Windows users can call `chi.bat` with the same arguments.
- **Testing and CI**: GitHub workflows (`.github/workflows/ci.yaml` and `publish.yaml`) run `./gradlew build` and publish artifacts when needed.
- **Native**: follow `native/readme.md` to produce GraalVM images. Remember to regenerate configs whenever the surface API changes.

## Appendix: Quick Reference

| Feature | Syntax | Notes |
| --- | --- | --- |
| Package | `package module/package` | Optional, at file top |
| Import | `import module/package { name as alias }` | `as` works on package or symbol |
| Value | `val name: Type = expr` | Use `var` for mutable bindings |
| Function | `fn name[T](args): Return { body }` | Omit `Return` for `unit` |
| Type alias | `type Name[T] = Type` | Works with generics |
| Data type | `data Name = Ctor(fields) | Other` | Single-constructor shorthand: `data Name(fields)` |
| Trait | `trait Name { fn method(args): Type }` | Methods are signatures only |
| Effect | `effect Name(args): Return` | Handle with `handle { ... } with { Name(...) -> ... }` |
| Lambda | `{ (args ->)? body }` | Call immediately by appending `()` |
| Array literal | `[a, b, c]` | Trailing commas allowed |
| Record literal | `{ field: value, ... }` | Structural; dot access |
| If expression | `if cond { ... } else { ... }` | Returns last expression per branch |
| When expression | `when { guard -> body; else -> fallback }` | Guards are arbitrary expressions |
| For loop | `for key?, value in iterable { ... }` | Optional `, stateExpr, initExpr` tail |
| While loop | `while condition exprBlock` | Block is mandatory |
| Cast / test | `expr as Type` / `expr is Type` | Runtime-checked |
| Weave | `input ~> template` | For DSL/template APIs |

For in-depth discussion of structural typing and inference, read [`typesystem.md`](./typesystem.md). When in doubt, inspect `ChiParser.g4` for the authoritative grammar or run the REPL with `--ast` (when available) to visualize parser output.
