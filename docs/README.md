# Chi Programming Language

Chi is a statically-typed programming language with **Hindley-Milner type inference** and **structural typing**. The compiler parses Chi source code, performs type checking, and emits **Lua code** (targeting LuaJIT), which runs via the `luajava` JVM-to-Lua bridge.

## Key Features

- **Type inference** -- write less type annotations; the compiler figures out types for you
- **Structural typing** -- types are matched by structure, not by name
- **Sum types** -- express values that can be one of several types (`int | string`)
- **Algebraic effects** -- define and handle side effects in a composable way
- **First-class functions** -- lambdas, closures, higher-order functions
- **String interpolation** -- embed expressions directly inside strings
- **Weave operator** -- pipeline-style data transformations with `~>`
- **REPL** -- interactive development with history and tab completion

## Quick Start

### Prerequisites

- JDK 25+ (GraalVM recommended for native image)
- Gradle (wrapper included)

### Building

```bash
# Build the uber JAR
./gradlew shadowJar

# Or use make
make shadow-jar
```

### Running a Chi program

Create a file `hello.chi`:

```chi
println("Hello, World!")
```

Run it:

```bash
# Via the shell script (requires GRAALVM_HOME set)
./chi.sh hello.chi

# Or via Gradle
./gradlew run --args="hello.chi"
```

### Using the REPL

```bash
./gradlew run --args="repl"
```

The REPL supports:
- Multi-line input (open braces auto-continue to the next line)
- History (up/down arrow keys, persisted across sessions)
- Tab completion for dot-commands
- Vi and Emacs keybinding modes (`.vi`, `.emacs`)
- `.help` to list available commands
- `.toggleLuaCode` to see the emitted Lua
- `.exit` to quit

### Native Image (optional)

```bash
# Generate native-image configuration
make native-config

# Build native binary
make native

# Install to ~/.local/bin
make install
```

### CLI Usage

```
chi [options] [FILE] [ARGS ...]
chi repl [ARGS ...]
chi compile [FILE]

Options:
  -l                Show emitted Lua code
  --print-ast       Print the AST and exit
```

## Hello World

```chi
println("Hello, World!")
```

## A Taste of Chi

```chi
// Variables - immutable by default
val name = "Chi"
var counter = 0

// Type inference works seamlessly
val id = { a -> a }
id(42)        // inferred: int
id("hello")   // inferred: string

// Records with structural typing
val person = { name: "Alice", age: 30 }
println(person.name)

// Functions with type annotations
fn greet(who: string): string {
    "Hello $who!"
}

// Sum types
val result: int | unit = 42
if result is int {
    println("Got a number!")
}

// Pattern-like branching with when
val x = 10
when {
    x < 0  -> println("negative")
    x == 0 -> println("zero")
    else   -> println("positive")
}

// For loops over arrays
var sum = 0
for n in [1, 2, 3, 4, 5] {
    sum += n
}
println(sum)
```

## Documentation

- [Syntax](syntax.md) -- language syntax reference
- [Type System](types.md) -- types, inference, and structural typing
- [Functions](functions.md) -- functions, lambdas, and closures
- [Modules](modules.md) -- packages and imports
- [Collections](collections.md) -- arrays and records
- [Control Flow](control-flow.md) -- if/else, loops, when, effects
- [Standard Library](stdlib.md) -- built-in functions and string methods

## Environment Variables

| Variable | Description |
|----------|-------------|
| `GRAALVM_HOME` | Path to GraalVM (for `chi.sh` and native image) |
| `CHI_HOME` | Chi home directory; if set and contains `lib/std.chim`, the stdlib is auto-loaded. REPL history is stored here. |
