# Chi Compiler — Known Bugs

This document lists confirmed bugs found during a full code review of the Chi compiler.
Each entry includes enough context for a coding agent to locate and fix the issue.

Bugs are ordered by severity: CRITICAL > HIGH > MEDIUM.

---

## BUG-01 [CRITICAL] String literals break Lua emission — no escaping of special characters

**File:** `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt:208-225`

**What happens:** The parser (`AtomReader.kt:74-80`) resolves escape sequences into real
characters (`\n` → actual newline, `\\` → actual backslash, `\t` → tab, etc.). The emitter
then dumps these raw characters into a Lua single-quoted string literal with zero escaping:

```kotlin
emitCode("local $tmp = java.new(String,'${term.value}');")
```

There is an explicit `TODO` on line 211 acknowledging this.

**User impact:**
- `"hello\nworld"` → literal newline inside `'...'` → **Lua syntax error**
- `"it's a test"` → unbalanced quotes → **Lua syntax error**
- `"path\\to\\file"` → Lua interprets `\t` as tab, `\f` as form feed → **silent data corruption**

**Fix guidance:** Before interpolating `term.value` into the Lua string, escape at minimum:
`\` → `\\`, `'` → `\'`, newline → `\n`, carriage return → `\r`, tab → `\t`,
null byte → `\0`. Consider using Lua's `[[ ]]` long-string syntax as an alternative, but
note that it doesn't support escape sequences and has edge cases with nested `]]`.

---

## BUG-02 [CRITICAL] Missing occurs check in unification — infinite types accepted silently

**File:** `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:26-36`

**What happens:** When binding a type variable to a type (`expected is Variable` on line 26,
`actual is Variable` on line 32), the code immediately adds the substitution without
checking whether the variable appears inside the type being bound to:

```kotlin
expected is Variable -> {
    solutions.add(expected to actual)
    val replacer = VariableReplacer(expected, actual)
    queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
}
```

In standard Hindley-Milner, before binding `'a = T` you must verify that `'a` does not
occur free in `T` (the "occurs check"). Without this, circular constraints like
`'a = ('a -> int)` are silently accepted.

**User impact:** Code like `val f = { x -> x(x) }` should produce an "infinite type" error
but instead compiles with unresolved type variables, producing nonsensical types downstream.

**Fix guidance:** Before adding to `solutions`, walk the other type using `children()` or a
dedicated visitor to check if the variable being bound appears inside it. If it does, throw
a `CompilerMessage` with a clear "infinite type" error. The check should apply to both the
`expected is Variable` branch (line 26) and the `actual is Variable` branch (line 32).

Standard implementation:
```kotlin
fun occursIn(variable: Variable, type: Type): Boolean = when (type) {
    is Variable -> type == variable
    else -> type.children().any { occursIn(variable, it) }
}
```

---

## BUG-03 [CRITICAL] ~~Missing `throw` in AtomReader — unrecognized string parts silently dropped~~ ALREADY FIXED

**Status:** The `throw` keyword is already present in the current codebase at `AtomReader.kt:81`.
The bug report was written against an older version or the fix was applied before this review.

**File:** `src/main/kotlin/gh/marad/chi/core/parser/readers/AtomReader.kt:81`

**Current code (correct):**
```kotlin
else -> throw CompilerMessage.from("Unsupported string part: $part!", getSection(source, ctx))
```

**Note:** No existing tests cover this defensive branch. The `else` branch cannot be
triggered through normal Chi source with the current ANTLR grammar — it guards against
future grammar changes introducing new string part token types.

---

## BUG-04 [HIGH] `ForLoop.children()` omits `state` and `init` — invisible to all compiler passes

**Files:**
- `src/main/kotlin/gh/marad/chi/core/Expressions.kt:265-266`
- `src/main/kotlin/gh/marad/chi/core/parser/readers/ForReader.kt:43-44`

**What happens:** Both `ForLoop` (expression AST) and `ParseFor` (parse AST) only return
`listOf(iterable, body)` from `children()`, omitting `state: Expression?` and
`init: Expression?`:

```kotlin
// Expressions.kt:265
override fun children(): List<Expression> =
    listOf(iterable, body)

// ForReader.kt:43
override fun children(): List<ParseAst> =
    listOf(iterable, body)
```

The `children()` method is the traversal mechanism used by **all** default visitor
implementations (`DefaultExpressionVisitor`, `DefaultParseAstVisitor`). This means
`state` and `init` sub-expressions are invisible to:

- **`UsageMarker`** (`Usage.kt:46-48`) — sub-expressions not marked as `used`
- **`ImmutabilityCheckVisitor`** — mutation of `val` inside state/init not caught
- **`VisibilityCheckingVisitor`** — access to private symbols not caught
- **`FnCallCheckingVisitor`** — call-target checks skipped
- **`CheckNamesVisitor`** — undefined name references not caught
- **`replaceTypes`** (`core/types/Operations.kt`) — type variables not resolved after unification

The Typer (`Typer.kt:197-198`) **does** generate constraints for state/init, but the
resolved types are never applied back because `replaceTypes` doesn't visit those nodes.

**User impact:** For-loops with stateful generators (`for x in iter, stateExpr, initExpr { ... }`)
can reference undefined variables, mutate immutable values, and access private symbols in
`stateExpr` and `initExpr` without any compiler error.

**Fix guidance:** Change both `children()` methods to include nullable fields:
```kotlin
// Expressions.kt
override fun children(): List<Expression> =
    listOfNotNull(iterable, state, init, body)

// ForReader.kt
override fun children(): List<ParseAst> =
    listOfNotNull(iterable, state, init, body)
```

---

## BUG-05 [HIGH] `VariableMapper.visitFunction` drops `defaultArgs` and `typeParams`

**File:** `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt:16-17`

**What happens:** The `VariableMapper` base class reconstructs `Function` using only the
`types` field, discarding `typeParams` and `defaultArgs`:

```kotlin
override fun visitFunction(function: Function): Type =
    Function(function.types.map { it.accept(this) })
```

The `Function` data class (`Types3.kt:96`) has three fields:
```kotlin
data class Function(val types: List<Type>, val typeParams: List<String> = emptyList(), val defaultArgs: Int = 0)
```

`VariableMapper` is the base for `FreshenAboveVisitor` (line 38-51 in the same file),
which is used by `PolyType.instantiate` for polymorphic type instantiation. Every time a
polymorphic function type is instantiated, `defaultArgs` resets to `0` and `typeParams`
are erased.

Note: `VariableReplacer` (in `Types3.kt` or nearby) does this correctly using
`function.copy(types = ...)`.

**User impact:** Polymorphic functions with default arguments lose their default arg count
after instantiation, causing arity errors at call sites where default args are omitted.

**Fix guidance:** Change line 17 to use `copy`:
```kotlin
override fun visitFunction(function: Function): Type =
    function.copy(types = function.types.map { it.accept(this) })
```

---

## BUG-06 [HIGH] `&&` and `||` do not constrain operands to `bool`

**File:** `src/main/kotlin/gh/marad/chi/core/types/Typer.kt:350-352`

**What happens:** For logical operators `&&` and `||`, the only constraint emitted is that
both sides have the same type (`lhsType = rhsType`). There is no constraint that either
operand must be `bool`:

```kotlin
if (term.op in listOf("<", "<=", ">", ">=", "==", "!=", "&&", "||")) {
    constraints.add(Constraint(lhsType, rhsType, term.right.sourceSection, emptyList()))
    Type.bool
}
```

Compare with `PrefixOp` handling (around line 367), which correctly constrains the
operand to `Type.bool` for the `not` operator.

**User impact:** Expressions like `5 && 3` or `"a" || "b"` pass type checking with result
type `bool`, which is a type soundness hole.

**Fix guidance:** For `&&` and `||` specifically, add a constraint requiring `bool` operands.
The comparison operators (`<`, `<=`, etc.) are fine as-is since they legitimately work on
non-bool types. Split the condition:

```kotlin
is InfixOp -> {
    val result = ctx.freshVariable(level)
    val lhsType = typeTerm(term.left, level, constraints)
    val rhsType = typeTerm(term.right, level, constraints)
    if (term.op in listOf("&&", "||")) {
        constraints.add(Constraint(Type.bool, lhsType, term.left.sourceSection, emptyList()))
        constraints.add(Constraint(Type.bool, rhsType, term.right.sourceSection, emptyList()))
        Type.bool
    } else if (term.op in listOf("<", "<=", ">", ">=", "==", "!=")) {
        constraints.add(Constraint(lhsType, rhsType, term.right.sourceSection, emptyList()))
        Type.bool
    } else {
        // arithmetic operators
        constraints.add(Constraint(result, lhsType, term.left.sourceSection, emptyList()))
        constraints.add(Constraint(result, rhsType, term.right.sourceSection, emptyList()))
        result
    }
}
```

---

## BUG-07 [HIGH] Anonymous functions/lambdas leak into Lua global scope

**File:** `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt:244-250`

**What happens:** When emitting a non-top-level function (lambda or anonymous function),
the emitter writes:

```kotlin
} else {
    emitCode("function $tmpName(")
}
```

In Lua, `function tmpX(...) end` is syntactic sugar for `tmpX = function(...) end`, which
assigns to a **global** variable. It should be `local function tmpName(...)` or
`local tmpName; tmpName = function(...)`.

The `nextTmpName()` counter (`tmp0`, `tmp1`, ...) is per-emitter-instance and resets for
each compilation unit. This means separately compiled modules share the same tmp names.

**User impact:**
- Global namespace pollution with `tmp0`, `tmp1`, ... for every lambda
- When two Chi modules are loaded, `tmp0` from module A is overwritten by `tmp0` from
  module B — potential silent wrong-function-called bug
- The `emitWhile` function (lines 564-589, the `foo()` helper) creates additional
  functions for while-loop conditions, compounding the problem

**Fix guidance:** Prefix the function definition with `local`:
```kotlin
} else {
    emitCode("local function $tmpName(")
}
```
Alternatively, emit `local $tmpName = function(` and close with `end;` instead of
`function $tmpName(` ... `end`.

---

## BUG-08 [HIGH] `CheckNamesVisitor.visitHandle` does not visit the handle body

**File:** `src/main/kotlin/gh/marad/chi/core/compiler/checks/CheckNamesVisitor.kt:58-66`

**What happens:** The visitor processes effect handler cases (with `resume` and argument
names in scope) but never visits the main body of the `handle` expression:

```kotlin
override fun visitHandle(parseHandle: ParseHandle) {
    parseHandle.cases.forEach { case ->
        withNewScope {
            definedNames.add("resume")
            definedNames.addAll(case.argumentNames)
            case.body.accept(this)
        }
    }
    // parseHandle.body is NEVER visited!
}
```

**User impact:** Undefined variable references in the handle body pass compilation silently
and fail at Lua runtime.

**Fix guidance:** Add `parseHandle.body.accept(this)` before or after the `forEach` loop.
The body should be visited in the **outer** scope (not inside `withNewScope`), since it
doesn't have `resume` in scope:

```kotlin
override fun visitHandle(parseHandle: ParseHandle) {
    parseHandle.body.accept(this)
    parseHandle.cases.forEach { case ->
        withNewScope {
            definedNames.add("resume")
            definedNames.addAll(case.argumentNames)
            case.body.accept(this)
        }
    }
}
```

---

## BUG-09 [MEDIUM] `Recursive.withAddedTypeIds` unwraps the `Recursive` wrapper

**File:** `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:257-263`

**What happens:** The plural `withAddedTypeIds` returns the inner type instead of preserving
the `Recursive` wrapper:

```kotlin
override fun withAddedTypeIds(ids: List<TypeId>): Type {
    return if (type is HasTypeId) {
        type.withAddedTypeIds(ids)   // returns inner type, NOT wrapped in Recursive
    } else {
        type                          // also returns inner type, NOT Recursive
    }
}
```

Compare with the correct singular `withAddedTypeId` (line 249-254):
```kotlin
override fun withAddedTypeId(id: TypeId): Type {
    return if (type is HasTypeId && !type.getTypeIds().contains(id)) {
        copy(type = type.withAddedTypeId(id))   // correctly preserves Recursive wrapper
    } else {
        this
    }
}
```

**User impact:** Recursive type aliases (linked lists, trees) lose their recursive identity
when multiple type IDs are added, leading to incorrect type checking.

**Fix guidance:** Mirror the pattern from `withAddedTypeId`:
```kotlin
override fun withAddedTypeIds(ids: List<TypeId>): Type {
    return if (type is HasTypeId) {
        copy(type = type.withAddedTypeIds(ids))
    } else {
        this
    }
}
```

---

## BUG-10 [MEDIUM] `Sum.toString()` dead code produces unreadable error messages

**File:** `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:144-151`

**What happens:** The non-Option branch of `toString()` contains a dead `?:` branch:

```kotlin
override fun toString(): String {
    return if (Type.optionTypeId in ids) {
        val subtypes = listTypes(this) - Type.unit
        "$ids[${subtypes.joinToString("|")}]"
    } else {
        ids.toString() ?: "$lhs | $rhs"  // ids.toString() is NEVER null
    }
}
```

`ids` is `List<TypeId>`, so `ids.toString()` always returns a non-null `String` (e.g.,
`"[]"` for empty lists). The `?: "$lhs | $rhs"` branch is unreachable. For anonymous sum
types (e.g., from an if-else returning different types), `ids` is empty, so the user sees
`"[]"` in type error messages.

**User impact:** Error messages like *"Expected type is 'int' but got '[]'"* instead of
*"Expected type is 'int' but got 'string | float'"*.

**Fix guidance:** Replace the else branch:
```kotlin
} else {
    if (ids.isEmpty()) "$lhs | $rhs" else "$ids[$lhs | $rhs]"
}
```

---

## BUG-11 [MEDIUM] `Type.union()` crashes with `ClassCastException` on duplicate types

**File:** `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:48-49`

**What happens:**

```kotlin
@JvmStatic fun union(id: TypeId?, vararg types: Type): Sum =
    types.reduceRight { lhs, rhs -> Sum.create(id?.let { listOf(id) } ?: emptyList(), lhs, rhs) } as Sum
```

`Sum.create` flattens and deduplicates types. If all input types collapse to a single type
(e.g., `Type.union(null, int, int)`), `create` returns a single `Primitive`, and the
`as Sum` cast throws `ClassCastException`.

**User impact:** Internal compiler crash if union is constructed with duplicate types.

**Fix guidance:** Either guard `union()` to return `Sum` only when 2+ distinct types remain,
or change the return type to `Type` and remove the `as Sum` cast. Since callers may expect
`Sum`, check each call site.

---

## BUG-12 [MEDIUM] `Block.used` flag not propagated — last expression always marked used

**File:** `src/main/kotlin/gh/marad/chi/core/compiler/Usage.kt:69-73`

**What happens:**

```kotlin
override fun visitBlock(block: Block) {
    block.body.lastOrNull()?.let {
        it.used = true    // unconditional, ignores block.used
    }
    visitChildren(block)
}
```

Compare with the correct pattern in `visitIfElse` (line 83-86):
```kotlin
ifElse.thenBranch.used = ifElse.used
ifElse.elseBranch?.used = ifElse.used
```

The last expression in every block is unconditionally marked `used = true`, even when the
block's own result is discarded.

**User impact:** Affects the `Is` expression check (`Usage.kt:124-131`): an `is` check on a
type variable inside a discarded block would incorrectly throw a compile error. Also affects
LuaEmitter behavior for expressions that check the `used` flag.

**Fix guidance:** Propagate the flag:
```kotlin
override fun visitBlock(block: Block) {
    block.body.lastOrNull()?.let {
        it.used = block.used
    }
    visitChildren(block)
}
```

---

## BUG-13 [MEDIUM] `VariableMapper.visitSum` drops `typeParams`

**File:** `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt:24-29`

**What happens:**

```kotlin
override fun visitSum(sum: Sum): Type =
    Sum.create(
        ids = sum.ids,
        lhs = sum.lhs.accept(this),
        rhs = sum.rhs.accept(this)
    )
```

`Sum.create` has a parameter `typeParams: List<String> = emptyList()`. The call does not
pass `sum.typeParams`, so type parameter names are lost during variable mapping (including
during `FreshenAboveVisitor` instantiation). Same class of bug as BUG-05.

**User impact:** Parametric sum types (e.g., `Option[T]`) lose their type parameter names
when instantiated, which can affect downstream type parameter resolution.

**Fix guidance:** Pass `typeParams`:
```kotlin
override fun visitSum(sum: Sum): Type =
    Sum.create(
        ids = sum.ids,
        lhs = sum.lhs.accept(this),
        rhs = sum.rhs.accept(this),
        typeParams = sum.typeParams
    )
```

---

## BUG-14 [MEDIUM] Debug stack trace capture always enabled in Constraint

**File:** `src/main/kotlin/gh/marad/chi/core/types/Constraint.kt:5`

**What happens:**

```kotlin
private const val debug = true
```

Every `Constraint` construction (lines 8-17) creates a `RuntimeException()` and captures
its stack trace. This is expensive and happens for every single constraint generated during
type inference. In a non-trivial program, hundreds or thousands of constraints are created.

**User impact:** Significant performance degradation during compilation. No functional bug,
but noticeable slowdown.

**Fix guidance:** Change to `false`:
```kotlin
private const val debug = false
```

Or remove the debug machinery entirely, or gate it behind a system property / environment
variable.

---

## BUG-15 [MEDIUM] Effects/Handle compile but crash at Lua emission

**File:** `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt:156-199`

**What happens:** The Lua code generation for `EffectDefinition` (lines 189-198) and
`Handle` (lines 156-188) is entirely commented out. The `when` in `emitExpr` falls through
to:

```kotlin
else -> TODO("Term $term not supported yet!")
```

The compiler front-end (parser, type checker) still accepts these constructs without error.

**User impact:** A Chi program using `effect` or `handle` compiles through all front-end
phases, then crashes with `kotlin.NotImplementedError` during Lua emission. The user gets
no helpful diagnostic.

**Fix guidance:** Either:
1. Re-implement the Lua emission for effects/handle, or
2. Reject these constructs early with a clear error message. The best place is in
   `Compiler.kt` after AST conversion, before emission — walk the AST and throw a
   `CompilerMessage` if `EffectDefinition` or `Handle` nodes are found:
   ```kotlin
   expressions.forEach {
       if (it is EffectDefinition || it is Handle) {
           throw CompilerMessage.from("Effects are not currently supported", it.sourceSection)
       }
   }
   ```
