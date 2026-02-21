## Context

Chi uses Hindley-Milner type inference with a constraint-based unification algorithm in `Unification.kt`. During unification, when a type variable is bound to a type (`expected is Variable` or `actual is Variable`), the substitution is recorded immediately without verifying the variable doesn't appear in the target type. This is the standard "occurs check" that all correct HM implementations require.

The unification function processes a queue of `Constraint(expected, actual, section, history)` pairs. When a variable is encountered, it creates a `VariableReplacer` and applies it to the remaining queue. The `Type` hierarchy (`Types3.kt`) provides `children()` for recursive traversal over subtypes.

Error reporting uses structured `Message` data classes in `core/analyzer/Analyzer.kt`, thrown as `CompilerMessage` exceptions for fatal errors.

## Goals / Non-Goals

**Goals:**
- Detect and reject circular type variable bindings during unification (e.g., `'a = ('a -> int)`)
- Produce a clear, user-facing "infinite type" error message with source location
- Follow existing error reporting patterns (`Message` data class + `CompilerMessage` exception)

**Non-Goals:**
- Supporting equi-recursive types (explicit `Recursive` type wrapper already handles intended recursive types)
- Changing the constraint generation or type inference algorithm beyond adding the occurs check
- Optimizing unification performance (the check adds negligible overhead)

## Decisions

**1. Implement occurs check as a standalone recursive function**

The check will be a simple recursive function `occursIn(variable, type)` that walks the type tree using `children()`. This is the standard textbook approach and integrates naturally with Chi's existing `Type.children()` traversal.

Alternative considered: Using the `TypeVisitor` pattern. Rejected because the check is simple enough that a plain recursive function is clearer and avoids creating a new visitor class.

**2. Check both Variable branches identically**

Both `expected is Variable` (line 26) and `actual is Variable` (line 32) will get the same occurs check. In the `expected is Variable` case, we check if `expected` occurs in `actual`. In the `actual is Variable` case, we check if `actual` occurs in `expected`.

**3. Add a new `InfiniteType` message data class**

Following the existing pattern of structured error messages (`TypeMismatch`, `FunctionArityError`, etc.), we'll add `InfiniteType` to `Analyzer.kt`. This keeps error types enumerable and consistent with the rest of the codebase.

**4. Place the occurs check function in `Unification.kt`**

The function is only used by unification and is a small helper. Placing it in the same file keeps the fix localized and easy to understand.

## Risks / Trade-offs

**[Risk] Rejecting currently-compiling code** -> This is intentional. Any code relying on the missing occurs check was already type-unsound and would produce incorrect behavior at runtime. No valid Chi programs are affected.

**[Risk] Interaction with `Recursive` type** -> The `Recursive` type is unwrapped via `unfold()` before variable binding is reached (lines 20-24 in `Unification.kt`), so the occurs check will not interfere with intentional recursive types. No mitigation needed.
