## Context

The Chi compiler's type inference engine generates constraints (pairs of types that must unify) during type checking. Each `Constraint` instance is created via the `Constraint` data class in `src/main/kotlin/gh/marad/chi/core/types/Constraint.kt`.

Currently, a top-level `private const val debug = true` causes every `Constraint` construction to allocate a `RuntimeException` and walk the JVM stack to find the first non-`Constraint.kt` frame. This stack trace element is stored in `Constraint.from` and included in `Constraint.toString()`. While useful for debugging type inference issues, it should not be enabled by default.

## Goals / Non-Goals

**Goals:**
- Disable debug stack trace capture by default to eliminate unnecessary overhead
- Preserve the ability to re-enable debug tracing when needed

**Non-Goals:**
- Removing the debug machinery entirely (it has diagnostic value)
- Adding runtime configuration via system properties (over-engineering for a `const val` toggle)
- Changing `Constraint.toString()` behavior (it already handles `from = null` gracefully)

## Decisions

### Decision 1: Set `debug = false` (not remove debug code)

**Choice:** Change `private const val debug = true` to `private const val debug = false`.

**Alternatives considered:**
- *Remove debug code entirely:* Discarded because the stack trace capture is genuinely useful when debugging type inference issues. Keeping it behind a compile-time flag costs nothing when disabled.
- *Gate behind system property (`System.getProperty`):* Over-engineering. The flag is a `const val` in a private scope -- toggling it requires a recompile, which is fine since only compiler developers use it.

**Rationale:** Minimal change, zero risk, preserves developer tooling.

## Risks / Trade-offs

- **[Risk] Developers forget the flag exists** -- Mitigated by the existing code comment pattern; the `if (debug)` block is self-documenting.
- **[Trade-off] `Constraint.toString()` shows `null` for `from`** -- Acceptable; `toString()` is only used in debug/error contexts and the `expected = actual` part remains informative.
