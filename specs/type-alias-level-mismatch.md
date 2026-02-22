# BUG: Type alias variable replacement fails due to level mismatch

**Severity:** CRITICAL — blocks compilation of the standard library (`stdlib`)

**Symptom:** Compiling `std/lang.option.chi` produces:
```
pub fn valueOr[T](opt: Option[T], alternative: T): T {
                                                     ^ Infinite type: cannot construct the infinite type ''T' = ''T | 'T'
```

This prevents `make` in `stdlib/` from completing — the entire stdlib cannot be built.

**Triggered by:** The BUG-02 fix (occurs check in `Unification.kt`). Before the fix, the
underlying bug was silently masked — the unifier would accept the circular binding without
complaint and the program would accidentally work. After adding the occurs check, the type
system correctly rejects the circular constraint, but the constraint itself is wrong — it
should never have been generated.

---

## Root cause

Type alias variable replacement in `Compiler.resolveType()` for `TypeConstructorRef`
creates replacement source variables at the **call-site level**, but the alias body stores
variables at **definition-site level (always 1)**. Since `Variable` equality compares both
`name` and `level`, the replacement silently does nothing and the original level-1 variables
leak into the resolved type.

### Detailed trace

#### 1. Type alias definition — variables stored at level 1

`Compiler.kt:72-80`:
```kotlin
val definedTypeAliases = parsedProgram.typeAliases.map { typeAliasDef ->
    val typeSchemeVariables = typeAliasDef.typeParameters.map { it.name }
    val id = TypeId(...)
    TypeAlias(
        id,
        resolveTypeAndWrapRecursive(tables.localTypeTable, typeSchemeVariables, typeAliasDef.type, id, 1)
        //                                                                               level = 1 ^^^
    )
}
```

For `type Option[T] = T | unit`, the alias is stored as:
```
TypeAlias(
    typeId = TypeId("std", "lang.option", "Option"),
    type   = Sum(Variable("T", level=1), Primitive(unit), typeParams=["T"])
)
```

Note: `TypeAlias` does **not** store the original type parameter names separately — they
are only recoverable via `type.typeParams()`.

#### 2. Resolving `Option[T]` at a usage site — replacement fails

When the compiler encounters `pub fn valueOr[T](opt: Option[T], ...)`, parameter type
annotations are resolved in `ExprConversionVisitor.visitFuncWithName` (line ~116):

```kotlin
val type = resolveType(typeTable, currentTypeSchemeVariables, argument.typeRef!!, currentTypeLevel)
//                                                                                ^^^^^^^^^^^^^^^^
//                                                                          currentTypeLevel = 0
```

This calls `Compiler.resolveType` for `TypeConstructorRef` (line ~243-262):

```kotlin
is TypeConstructorRef -> {
    val base = resolveType(typeTable, typeSchemeVariables, ref.baseType, level, ...)
    // base = Sum(Variable("T", level=1), unit, typeParams=["T"])
    //        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //        Variables are at level 1 from alias definition

    val params = ref.typeParameters.map { resolveType(..., it, level, ...) }
    // params = [Variable("T", level=0)]  (T is in typeSchemeVariables, resolved at current level)

    val typeParamNames = base.typeParams()  // ["T"]

    val replacements = typeParamNames.map { Variable(it, level) }.zip(params)
    // replacements = [(Variable("T", level=0), Variable("T", level=0))]
    //                  ^^^^^^^^^^^^^^^^^^^^^^^
    //                  Source variable at level 0, but alias has level 1!

    mapType(base, replacements)
    // mapType tries: replace Variable("T", 0) → Variable("T", 0) in Sum(Variable("T", 1), unit)
    //
    // VariableReplacer checks: Variable("T", 1) == Variable("T", 0)?  → NO (different levels)
    //
    // RESULT: Sum(Variable("T", 1), unit) — level-1 variable LEAKED, unreplaced!
}
```

The resolved type for `opt` is `Sum(Variable("T", 1), unit)` — containing a stale
level-1 variable instead of the expected level-0 variable.

#### 3. In the function body — mixed-level variables appear

In `ExprConversionVisitor.visitFuncWithName` (lines 121-123):
```kotlin
currentTypeLevel += 1   // now 1
val body = parseFuncWithName.body.accept(this) as Block
currentTypeLevel -= 1   // back to 0
```

Inside the body of `valueOr`:
```chi
if opt is unit {
    alternative        // type = Variable("T", level=0) — from parameter, resolved at level 0
} else {
    opt as T           // type = Variable("T", level=1) — cast target resolved at currentTypeLevel=1
}
```

The if-else produces (in `Typer.kt:165-166`):
```kotlin
Sum.create(thenBranchType, elseBranchType)
// = Sum.create(Variable("T", 0), Variable("T", 1))
```

Since `Variable("T", 0) != Variable("T", 1)`, `Sum.create`'s internal `Set` does NOT
deduplicate them. The result is `Sum(Variable("T", 0), Variable("T", 1))`.

#### 4. Unification triggers the occurs check

The Typer generates a constraint chain that simplifies to:

```
Variable("T", 0) = Sum(Variable("T", 0), Variable("T", 1))
```

The occurs check (added by BUG-02 fix in `Unification.kt:32-33`) finds `Variable("T", 0)`
inside `Sum(Variable("T", 0), Variable("T", 1))`:

```kotlin
expected is Variable -> {
    if (occursIn(expected, actual)) {   // Variable("T",0) occurs in Sum(...) → TRUE
        throw CompilerMessage(InfiniteType(expected, actual, section.toCodePoint()))
    }
    ...
}
```

Error: `Infinite type: cannot construct the infinite type ''T' = ''T | 'T'`

---

## The fix

The bug is in `Compiler.resolveType`, in the `TypeConstructorRef` branch (around line 261).

The replacement source variables must match the level at which the alias body was defined,
NOT the call-site level. Currently:

```kotlin
val replacements = typeParamNames.map { Variable(it, level) }.zip(params)
//                                                   ^^^^^
//                                         call-site level — WRONG
```

The alias body was always created with `level = 1` (see `resolveTypeAndWrapRecursive` call
at `Compiler.kt:77`). The replacement source variables must also be at level 1 to match.

### Option A: Hardcode the alias definition level

Change line ~261:
```kotlin
// BEFORE:
val replacements = typeParamNames.map { Variable(it, level) }.zip(params)

// AFTER:
val replacements = typeParamNames.map { Variable(it, 1) }.zip(params)
```

This works because `resolveTypeAndWrapRecursive` always passes `level = 1`. However, it's
fragile — if that call ever changes, this breaks again.

### Option B: Extract actual variables from the alias body (recommended)

Instead of reconstructing `Variable` objects by name, extract the real `Variable` instances
that exist in the alias type. These carry the correct level by definition:

```kotlin
// BEFORE:
val typeParamNames = base.typeParams()
// ...
val replacements = typeParamNames.map { Variable(it, level) }.zip(params)

// AFTER: find the actual Variable objects from the type's children
val typeVars = findTypeVariables(base, base.typeParams())  // returns List<Variable> with original levels
val replacements = typeVars.zip(params)
```

Where `findTypeVariables` walks the type tree and collects the first `Variable` matching
each type param name (preserving the order of `typeParams`). For example:

```kotlin
fun findTypeVariables(type: Type, paramNames: List<String>): List<Variable> {
    val found = mutableMapOf<String, Variable>()
    fun walk(t: Type) {
        if (t is Variable && t.name in paramNames && t.name !in found) {
            found[t.name] = t
        }
        t.children().forEach { walk(it) }
    }
    walk(type)
    return paramNames.map { found[it] ?: Variable(it, 1) }
}
```

### Option C: Make the alias level-agnostic

Store the alias type at a sentinel level (e.g. `Int.MAX_VALUE`) and make the replacement
match by name only (ignoring level). This requires changing `VariableReplacer` to support
name-only matching for alias substitutions, which is a larger change.

---

## How to verify

1. **Unit test:** Compile this Chi code and assert no errors:
```chi
package std/lang.option

type Option[T] = T | unit

pub fn valueOr[T](opt: Option[T], alternative: T): T {
    if opt is unit {
        alternative
    } else {
        opt as T
    }
}
```

2. **Full stdlib build:** Run `make` in `stdlib/` — it should complete without errors,
producing `std.chim`.

3. **Regression test for BUG-02:** Ensure the occurs check still rejects genuinely infinite
types:
```chi
// This should STILL produce an "Infinite type" error:
val f = { x -> x(x) }
```

---

## Files involved

| File | Role |
|---|---|
| `src/main/kotlin/gh/marad/chi/core/compiler/Compiler.kt:72-80` | Type alias creation — hardcodes `level = 1` |
| `src/main/kotlin/gh/marad/chi/core/compiler/Compiler.kt:243-262` | `resolveType` for `TypeConstructorRef` — **the buggy replacement** |
| `src/main/kotlin/gh/marad/chi/core/compiler/ExprConversionVisitor.kt:43` | `currentTypeLevel` starts at 0 |
| `src/main/kotlin/gh/marad/chi/core/compiler/ExprConversionVisitor.kt:116` | Params resolved at `currentTypeLevel` (0) |
| `src/main/kotlin/gh/marad/chi/core/compiler/ExprConversionVisitor.kt:121-123` | Body resolved at `currentTypeLevel + 1` |
| `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:214-234` | `Variable` — equality checks `name` AND `level` |
| `src/main/kotlin/gh/marad/chi/core/types/VariableReplacer.kt:25-29` | Replacement match — uses `Variable.equals`, so level must match |
| `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:32-33` | Occurs check (BUG-02 fix) — correctly rejects the leaked constraint |
| `src/main/kotlin/gh/marad/chi/core/types/Operations.kt:12-15` | `mapType` — applies replacements via `VariableReplacer` |
| `stdlib/std/lang.option.chi` | The stdlib file that triggers the bug |
