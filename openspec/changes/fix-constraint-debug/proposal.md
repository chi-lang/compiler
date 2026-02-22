## Why

The `Constraint` data class has a hardcoded `debug = true` flag that causes every constraint construction to capture a JVM stack trace via `RuntimeException()`. Type inference generates hundreds to thousands of constraints per compilation unit, making this a significant and unnecessary performance cost in non-debug builds.

## What Changes

- Disable the debug stack trace capture in `Constraint.kt` by setting `debug = false`
- Optionally gate the debug flag behind a system property so it can be re-enabled when needed without code changes

## Capabilities

### New Capabilities

_None_ -- this is a single-flag fix with no new capabilities.

### Modified Capabilities

_None_ -- no spec-level behavior changes. This is purely an implementation-level performance fix; the type inference system produces identical results with or without debug tracing.

## Impact

- **File:** `src/main/kotlin/gh/marad/chi/core/types/Constraint.kt`
- **Performance:** Eliminates per-constraint `RuntimeException` allocation and stack trace capture during compilation
- **Debugging:** Stack trace info in `Constraint.toString()` will show `null` instead of a source location when debug is off. Developers can re-enable via the flag if needed.
- **No behavioral change:** Type inference results, error messages, and compiled output are unaffected.
