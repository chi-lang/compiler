## Why

`CheckNamesVisitor.visitHandle` visits handler cases but never calls `parseHandle.body.accept(this)`, so undefined variable references in the handle body pass compilation silently and only fail at Lua runtime. This is BUG-08 from the known bugs list (severity: HIGH).

## What Changes

- Fix `CheckNamesVisitor.visitHandle` to also visit `parseHandle.body` in the outer scope (before processing cases), so undefined name references in handle bodies are caught at compile time.
- Add tests to verify that undefined names in handle bodies produce `UnrecognizedName` errors.

## Capabilities

### New Capabilities

- `handle-body-name-check`: The compiler's name-checking pass visits the body of `handle` expressions, catching undefined variable references at compile time instead of deferring to Lua runtime errors.

### Modified Capabilities

_(none -- no existing specs cover handle expression name checking)_

## Impact

- **Code**: `CheckNamesVisitor.kt` -- single method change in `visitHandle`
- **Correctness**: Programs with undefined names in handle bodies will now produce compile-time errors instead of silent pass-through
- **Risk**: Low -- the fix adds a missing visitor call; no behavioral change for valid programs
