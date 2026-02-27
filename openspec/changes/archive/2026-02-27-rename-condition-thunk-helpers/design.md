## Context

`LuaEmitter.kt` line 589 contains a private function `foo(term, bar)` that extracts sub-expressions from while-loop conditions into temporary thunk declarations. The names `foo` and `bar` are clearly placeholders that were never replaced during initial development. The function is called recursively within itself and once from `emitWhile()`.

## Goals / Non-Goals

**Goals:**
- Rename `foo` to `extractConditionThunks` and `bar` to `thunkDeclarations` for readability.
- Remove the stale commented-out `InfixOp` constructor call on line 594.

**Non-Goals:**
- Refactoring the function's logic or signature.
- Changing any emitted Lua output.
- Adding tests (pure rename, existing tests already cover while-loop emission).

## Decisions

### 1. Name choice: `extractConditionThunks`

**Decision**: Name the function `extractConditionThunks` to describe what it does — it walks a condition expression tree and extracts non-infix sub-expressions into thunk function declarations.

**Alternatives considered**: `emitConditionParts`, `rewriteCondition` — rejected because the function doesn't emit anything; it returns a string and accumulates declarations into a list.

### 2. Parameter name: `thunkDeclarations`

**Decision**: Name the accumulator parameter `thunkDeclarations` because it collects `NameDeclaration` entries that wrap sub-expressions in zero-argument functions (thunks).

### 3. Remove commented-out code

**Decision**: Remove the `//InfixOp(term.op, left, right, term.sourceSection)` comment on line 594. It's a remnant from when the function returned AST nodes instead of strings and adds no documentary value.
