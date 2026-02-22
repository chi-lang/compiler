# BUG: Unifier lacks sum type widening — actual type not recognized as member of expected sum

**Severity:** CRITICAL — blocks compilation of `std/lang.option.chi` (and therefore entire stdlib)

**Depends on:** Fix for type-alias-level-mismatch (must be applied first). That fix resolved
the `valueOr` case but exposed this deeper issue in the remaining functions.

**Symptom:** After the level-mismatch fix, three functions in `std/lang.option.chi` fail:

```
pub fn asOption[T](value: T): Option[T] {
                                        ^ Infinite type: cannot construct the infinite type ''T' = '[std::lang.option::Option]['T]'

        f(opt)
        ^ Infinite type: cannot construct the infinite type ''T' = '[std::lang.option::Option]['T]'
        // (in map and ifPresent)
```

---

## Root cause

The unifier handles `expected is Sum` (line 77 in `Unification.kt`) by decomposing the sum
and trying to unify individual branches against the actual type. However, the `when` branch
ordering means `expected is Variable` and `actual is Variable` are checked **before**
`expected is Sum`. When a type variable is being bound to a sum type that *contains* that
variable, the occurs check fires before the sum decomposition logic ever gets a chance.

In Chi's type system, `T` is a valid member of `T | unit` (i.e. `Option[T]`). Returning a
value of type `T` where `Option[T]` is expected should be valid — it's a widening/subsumption
operation, not an infinite type.

### Detailed trace for `asOption`

```chi
pub fn asOption[T](value: T): Option[T] {
    value
}
```

1. **ExprConversionVisitor** produces a `NameDeclaration` with:
   - `expectedType` = `Function([Variable("T", 0), Sum(ids=[Option], Variable("T", 0), unit)])`
     - i.e. `(T) -> Option[T]`
   - `value` = `Fn` whose body is just `value` (the parameter)

2. **Typer** for `Fn` (line 43-67):
   - `returnType` = fresh variable `'a`
   - parameter `value` has type annotation `Variable("T", 0)` (= `T`)
   - body type = `Variable("T", 0)` (= `T`, just returning the parameter)
   - Constraint added: `'a = Variable("T", 0)` (returnType = bodyType)
   - Fn type = `Function([Variable("T", 0), 'a])`

3. **Typer** for `NameDeclaration` (line 137-149):
   - Constraint: `expectedType = valueType`, which generates:
     - `Variable("T", 0) = Variable("T", 0)` → OK (parameter types match)
     - `Sum(Variable("T", 0), unit) = 'a` → binds `'a = Sum(Variable("T", 0), unit)`

4. **Unification** processes all constraints. After binding `'a`, the earlier constraint
   `'a = Variable("T", 0)` becomes (via substitution):

   ```
   Sum(Variable("T", 0), unit) = Variable("T", 0)
   ```

   or equivalently (depending on which side ends up where):

   ```
   Variable("T", 0) = Sum(Variable("T", 0), unit)
   ```

5. **The `when` chain in `unify`** (line 14-101):
   - `expected == actual` → NO (Variable ≠ Sum)
   - `expected is Variable` → **YES** → occurs check:
     ```kotlin
     occursIn(Variable("T", 0), Sum(Variable("T", 0), unit))  // → TRUE
     ```
   - **Throws `InfiniteType`** before the `expected is Sum` branch is ever reached.

### The same pattern in `map` and `ifPresent`

```chi
pub fn map[T,R](opt: Option[T], f: (T) -> R): Option[R] {
    if opt is unit { unit } else { opt is T; f(opt) }
}
```

Here `f(opt)` returns `R`, the if-else produces `Sum(unit, R)` = `Option[R]`. But through
a similar constraint chain, unification ends up with `'R = Option['R]` and the occurs check
fires.

---

## Why this worked before BUG-02

Before the occurs check was added (BUG-02 fix), the constraint `'T = Sum('T, unit)` was
silently accepted. The unifier would bind `'T` to `Sum('T, unit)`, creating a circular
reference. This is technically unsound in Hindley-Milner, but in Chi's type system where
sum types represent unions, it "accidentally" worked because `T` being bound to `T | unit`
is semantically correct — `T` *is* a valid `Option[T]`.

---

## The fix

The unifier needs to recognize that when binding a variable to a sum type that contains
that variable, this may be a valid widening rather than an infinite type. Specifically:

**When `expected is Variable` and `actual is Sum` (or vice versa), and the variable occurs
in the sum type only as a direct branch (not nested inside a function or other constructor),
then bind the variable to the full sum type without triggering the occurs check.**

### Recommended approach: sum-aware occurs check

Modify the occurs check to not recurse into sum type branches when the variable is a direct
member. The idea: `'T = 'T | unit` is valid widening, but `'T = ('T -> int) | unit` is a
genuine infinite type.

In `Unification.kt`, change the `expected is Variable` branch (and symmetrically
`actual is Variable`):

```kotlin
expected is Variable -> {
    val bindType = if (actual is Sum && sumContainsDirectly(actual, expected)) {
        // Variable is a direct branch of the sum — this is widening, not infinite type.
        // Bind the variable to the full sum.
        actual
    } else {
        if (occursIn(expected, actual)) {
            throw CompilerMessage(InfiniteType(expected, actual, section.toCodePoint()))
        }
        actual
    }
    solutions.add(expected to bindType)
    val replacer = VariableReplacer(expected, bindType)
    queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
}
```

Where `sumContainsDirectly` checks that the variable appears only as a top-level branch
of the sum, not nested deeper:

```kotlin
fun sumContainsDirectly(sum: Sum, variable: Variable): Boolean {
    val branches = Sum.listTypes(sum)  // flattened set of all branches
    return variable in branches
}
```

Note: `Sum.listTypes` is already a companion function (line ~171 in `Types3.kt`) that
recursively flattens nested sums into a `Set<Type>`. If the variable is one of these
top-level branches, and the `occursIn` check only fires because of this membership, then
it's safe widening. If the variable also appears nested inside another branch (e.g.,
`Sum(Function([variable, int]), unit)`), the occurs check should still fire.

A more precise implementation:

```kotlin
fun occursInExcludingSumBranches(variable: Variable, type: Type): Boolean = when (type) {
    is Variable -> type == variable
    is Sum -> {
        // Don't count the variable as "occurring" if it's just a direct branch.
        // Only flag if it occurs INSIDE a non-sum child.
        val branches = Sum.listTypes(type)
        val otherBranches = branches - variable
        otherBranches.any { occursIn(variable, it) }
    }
    else -> type.children().any { occursIn(variable, it) }
}
```

Then replace `occursIn` with `occursInExcludingSumBranches` in both the `expected is Variable`
and `actual is Variable` branches.

### Alternative approach: reorder branches + add `actual is Sum`

Add a check **before** the variable-binding branches that handles the case where one side
is a variable and the other is a sum containing that variable as a direct branch:

```kotlin
// Add before `expected is Variable`:
expected is Variable && actual is Sum && expected in Sum.listTypes(actual) -> {
    solutions.add(expected to actual)
    val replacer = VariableReplacer(expected, actual)
    queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
}

actual is Variable && expected is Sum && actual in Sum.listTypes(expected) -> {
    solutions.add(actual to expected)
    val replacer = VariableReplacer(actual, expected)
    queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
}
```

This approach is simpler but less precise — it doesn't guard against genuine infinite types
where the variable also appears nested inside another branch.

---

## How to verify

1. **All functions in `lang.option.chi` compile without errors:**

```chi
package std/lang.option

type Option[T] = T | unit

pub fn asOption[T](value: T): Option[T] { value }

pub fn valueOr[T](opt: Option[T], alternative: T): T {
    if opt is unit { alternative } else { opt as T }
}

pub fn map[T,R](opt: Option[T], f: (T) -> R): Option[R] {
    if opt is unit { unit } else { opt is T; f(opt) }
}

pub fn ifPresent[T](opt: Option[T], f: (T) -> unit) {
    if opt is unit { unit } else { opt is T; f(opt) }
}
```

2. **Full stdlib build:** `make` in `stdlib/` completes and produces `std.chim`.

3. **Genuine infinite types are still rejected:**

```chi
// Must still produce "Infinite type" error:
val f = { x -> x(x) }
```

4. **Nested occurrence is still rejected:**

```chi
// Should still error — variable occurs inside a function type within the sum:
// (this is a contrived example, may need adjustment to Chi syntax)
type Bad[T] = (T) -> int | unit
fn bad[T](x: T): Bad[T] { x }   // should fail: T ≠ (T) -> int | unit
```

---

## Files involved

| File | Role |
|---|---|
| `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:32-36` | `expected is Variable` branch — occurs check fires here |
| `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:39-44` | `actual is Variable` branch — symmetric case |
| `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:77-92` | `expected is Sum` branch — decomposition logic, never reached |
| `src/main/kotlin/gh/marad/chi/core/types/Unification.kt:5-8` | `occursIn` function — needs sum-aware variant |
| `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:140-178` | `Sum` class — `listTypes` companion, `typeParams`, `toString` |
| `src/main/kotlin/gh/marad/chi/core/types/Typer.kt:43-67` | `Fn` typing — generates `returnType = bodyType` constraint |
| `src/main/kotlin/gh/marad/chi/core/types/Typer.kt:137-149` | `NameDeclaration` — generates `expectedType = valueType` constraint |
| `stdlib/std/lang.option.chi` | The file that triggers the bug |

## Relationship to other bugs

- **type-alias-level-mismatch**: Must be fixed first. That fix ensures type variables in
  aliases have correct levels. Without it, the variables are inconsistent and additional
  spurious errors appear.
- **BUG-02 (occurs check)**: The occurs check itself is correct and must not be removed.
  This bug requires *refining* the occurs check to be sum-type-aware, not disabling it.
