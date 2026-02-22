## Context

The Chi compiler resolves type aliases during the `resolveType` phase (`Compiler.kt`). When a type alias like `type Option[T] = T | unit` is defined, its body is stored with type variables at level 1 (hardcoded in `resolveTypeAndWrapRecursive`). When that alias is used at a call site (e.g., `Option[T]` in a function parameter), the `TypeConstructorRef` branch of `resolveType` must substitute the alias's type parameters with the actual type arguments.

Currently, the replacement source variables are constructed at the call-site level (passed as `level`), but the alias body contains variables at definition-site level 1. Since `Variable.equals` compares both `name` and `level`, the replacement silently fails — no substitution occurs. The stale level-1 variables leak through, creating mixed-level constraints that the occurs check correctly rejects as infinite types.

## Goals / Non-Goals

**Goals:**
- Fix type alias variable substitution so the replacement source variables match the actual level stored in the alias body.
- Unblock stdlib compilation (`std/lang.option.chi` and any other files using generic type aliases).
- Maintain the BUG-02 occurs check — genuinely infinite types must still be rejected.

**Non-Goals:**
- Refactoring the type level system broadly (e.g., making aliases level-agnostic).
- Changing `Variable.equals` semantics or `VariableReplacer` matching logic.
- Modifying how `TypeAlias` stores its type or adding new fields to it.

## Decisions

### Decision 1: Extract actual Variable instances from the alias body (Option B)

Rather than hardcoding `level = 1` in the replacement (fragile — breaks if the alias definition level ever changes), walk the alias body's type tree and extract the real `Variable` instances that match each type parameter name. These carry the correct level by construction.

**Alternatives considered:**
- **Option A (hardcode level 1):** Simple one-line fix, but fragile — if `resolveTypeAndWrapRecursive` changes its level argument, this silently breaks again.
- **Option C (level-agnostic aliases):** Requires changing `VariableReplacer` matching semantics, a larger and riskier change for a targeted bug fix.

**Rationale:** Option B is robust: it derives the replacement level from the actual alias body, so it automatically stays correct regardless of how the alias was defined. The helper function (`findTypeVariables`) is small and localized.

### Decision 2: Place the helper in Compiler.kt as a private function

The `findTypeVariables` helper is only needed in the `TypeConstructorRef` branch of `resolveType`. Placing it as a private function (or companion function) in `Compiler.kt` keeps the change minimal and contained.

## Risks / Trade-offs

- **[Risk] Multiple variables with the same name at different levels in an alias body** → The helper takes the first match per name. This is safe because alias bodies are created at a single level (1), so there can't be conflicting levels for the same name.
- **[Risk] Alias body has no variable matching a type param name (e.g., phantom type params)** → The fallback `Variable(it, 1)` handles this case, preserving current behavior.
