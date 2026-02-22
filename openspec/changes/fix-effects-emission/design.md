## Context

BUG-15: The Chi compiler's Lua emission for `EffectDefinition` and `Handle` expressions is entirely commented out in `LuaEmitter.kt:156-198`. The `emitExpr` `when` block falls through to `else -> TODO(...)`, crashing at runtime with `NotImplementedError`. The front-end (parser, type checker, AST conversion) fully supports these constructs.

The commented-out code has two API mismatches from a previous refactor:
1. `qualifiedName(module, pkg, name)` no longer exists — replaced by `topLevelName(name)` for current-package and `localQualifiedName(module, pkg, name)` for cross-package references.
2. `emitExpr(expr, needResult = true)` — the `needResult` parameter was removed; `emitExpr` now always returns a result string.

Additionally, the commented-out code references a `chi_handle_effect` Lua runtime function that does not exist in the codebase and must be provided.

## Goals / Non-Goals

**Goals:**
- Re-enable Lua emission for `EffectDefinition` and `Handle` so effects compile and run end-to-end.
- Fix API mismatches in the commented-out code to match current emitter conventions.
- Provide the `chi_handle_effect` Lua runtime helper needed by the handle emission.
- Fix a Lua indexing bug in the original code (0-based vs 1-based array access).
- Add tests for basic effect definition, invocation, and handling.

**Non-Goals:**
- Multi-shot or deep resumption handlers — the existing one-shot/shallow model is sufficient.
- Cross-module effect handling — effects defined and handled within the same compilation unit only.
- Performance optimization of the coroutine-based implementation.
- Changing the front-end (parser, type checker, AST conversion) — these already work correctly.

## Decisions

### 1. Uncomment and fix rather than rewrite

**Decision**: Restore the commented-out emission code with targeted fixes rather than writing new emission from scratch.

**Rationale**: The commented-out code correctly implements the coroutine-yield/resume pattern for effects. Only the API surface changed (`qualifiedName` -> `topLevelName`/`localQualifiedName`, `needResult` removal). The algorithmic approach is sound.

**Alternatives considered**: Writing fresh emission code — rejected because the existing approach is standard for algebraic effects via coroutines.

### 2. Naming: use `topLevelName` for current-package effects

**Decision**: Replace `qualifiedName(term.moduleName, term.packageName, term.name)` with `topLevelName(term.name)` for `EffectDefinition` emission (the defining module). For `HandleCase` effect name references, use `localQualifiedName(case.moduleName, case.packageName, case.effectName)` to correctly reference effects that may come from other packages.

**Rationale**: This matches how `NameDeclaration` handles top-level definitions (see `emitNameDeclaration`). Effects are top-level constructs in their defining package, assigned to `__P_.<name>`.

### 3. Emit `chi_handle_effect` as inline Lua in the preamble

**Decision**: Emit the `chi_handle_effect` helper function at the top of the Lua output (in the `emit()` method) only when the program contains `Handle` expressions. This is a local function in the emitted Lua, not a separate runtime file.

**Rationale**: Keeps the runtime self-contained. The helper is small (~15 lines of Lua). Conditional emission avoids overhead for programs not using effects.

**Alternative considered**: A separate runtime `.lua` file loaded via `require` — rejected because it adds deployment complexity and the helper is small.

### 4. Fix Lua 1-based indexing for handler arguments

**Decision**: Change `args[${index}]` to `args[${index + 1}]` in the handler argument unpacking.

**Rationale**: Lua arrays are 1-indexed. The original commented-out code used Kotlin's 0-based `forEachIndexed`, producing `args[0]`, `args[1]`, etc. Since `coroutine.yield` packs args with `{...}` (which creates a 1-based Lua table), indices must start at 1.

### 5. Effect function emission pattern

**Decision**: Emit `EffectDefinition` as a package-level function assigned to `__P_.<name>`:
```lua
__P_.<name> = function(...) return coroutine.yield("<normalised_name>", {...}) end;
```

**Rationale**: Matches how top-level `NameDeclaration` with `Fn` values are emitted (assigned to `topLevelName`). Using direct assignment rather than `function __P_.<name>(...)` is clearer and consistent.

## Risks / Trade-offs

- **[Risk] One-shot handler model limitation** → The `resume` function in handlers is a shallow stub (`return false, x`). Handlers that need to execute code after resumption won't work correctly. This matches the original design and is acceptable for the current scope.
- **[Risk] No cross-module effect tests** → Effects defined in one module and handled in another are not tested. Mitigation: the naming uses `localQualifiedName` which handles cross-package references, but integration testing is deferred.
- **[Risk] Lua coroutine stack depth** → Deeply nested handle expressions create nested coroutines. Mitigation: LuaJIT has generous coroutine limits; this is unlikely to be a practical issue.
