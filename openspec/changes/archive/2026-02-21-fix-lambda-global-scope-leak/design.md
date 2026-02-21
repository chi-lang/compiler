## Context

The Chi compiler emits Lua code targeting LuaJIT. When emitting non-top-level functions (lambdas, anonymous functions, while-loop condition helpers), `LuaEmitter.emitFn` produces `function tmpN(...)` which in Lua is syntactic sugar for `tmpN = function(...)` — an assignment to a **global** variable. Top-level named functions correctly use `function __P_.$name(...)` (package-scoped), but all other functions leak into the global namespace.

The `nextTmpName()` counter (`tmp0`, `tmp1`, ...) resets per `LuaEmitter` instance (per compilation unit), so separately compiled modules produce identical tmp names that overwrite each other at runtime.

The `emitWhile` method's `foo()` helper (line ~579-603) also generates anonymous functions via `NameDeclaration` wrapping `Fn` nodes, which flow through the same `emitFn` path.

## Goals / Non-Goals

**Goals:**
- All non-top-level function definitions in emitted Lua code MUST use `local` scoping
- Eliminate cross-module name collisions from shared tmp variable names
- Fix both `emitFn` and the `foo()` helper's generated functions

**Non-Goals:**
- Changing the tmp name generation scheme (e.g., making names globally unique) — `local` scoping makes this unnecessary
- Refactoring `emitWhile`/`foo()` beyond the scoping fix
- Addressing top-level function emission (already correctly package-scoped via `__P_.`)

## Decisions

### Decision 1: Use `local function` syntax

**Choice**: Change `emitCode("function $tmpName(")` to `emitCode("local function $tmpName(")`

**Alternatives considered**:
- `local $tmpName = function(...)` — works but prevents self-recursive lambdas since the name isn't in scope during the function body. `local function` is Lua's standard idiom that handles both cases.
- Making tmp names globally unique (UUIDs/module prefixes) — over-engineered when `local` scoping eliminates the problem entirely.

**Rationale**: `local function name(...)` is semantically equivalent to `local name; name = function(...)` in Lua, which correctly scopes the name and supports recursion. It's the minimal, idiomatic change.

### Decision 2: No changes to `foo()` helper emission path

The `foo()` helper creates `NameDeclaration` nodes wrapping `Fn` values, which are emitted via the existing `emitNameDeclaration` → `emitFn` path. Since `emitNameDeclaration` already adds `local` prefix for in-function declarations (`val name = if (inFunction) "local ${term.name}"...`), and `foo()` is called within `insideFunction {}`, the generated functions from `foo()` will be correctly scoped once `emitFn` itself is fixed. No separate fix needed for the `foo()` path.

## Risks / Trade-offs

- **[Risk] Behavioral change in emitted Lua** → Mitigation: `local` scoping is strictly more correct than global. Any code that depended on global tmp names was relying on undefined behavior. Existing tests will validate no regressions.
- **[Risk] Self-recursive lambdas** → Mitigation: `local function` (not `local f = function`) correctly puts the name in scope for the body, so recursive lambdas work.
