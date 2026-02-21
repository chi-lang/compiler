## Context

`CheckNamesVisitor` performs compile-time name resolution for the parse AST. It walks AST nodes via the visitor pattern (`DefaultParseAstVisitor`), tracking which names are in scope via `definedNames`. The `visitHandle` override processes each handler case in a new scope (adding `resume` and case argument names), but never visits the handle body expression -- the `parseHandle.body.accept(this)` call is missing.

The default visitor (`DefaultParseAstVisitor`) would visit all `children()`, and `ParseHandle.children()` at `EffectReader.kt:70` does include `body`. However, because `CheckNamesVisitor.visitHandle` overrides the method without calling `super`, the default traversal is bypassed entirely.

## Goals / Non-Goals

**Goals:**
- Ensure `CheckNamesVisitor.visitHandle` visits the handle body expression in the outer scope so undefined name references produce `UnrecognizedName` errors at compile time.
- Add test coverage for this behavior.

**Non-Goals:**
- Changing how handler cases are scoped (the `resume` + argument names scoping is correct).
- Fixing the separate issue that effects/handle crash at Lua emission (BUG-15) -- that is a different change.
- Modifying `ParseHandle.children()` -- it already correctly includes `body`.

## Decisions

**Visit body before cases, in outer scope.** The handle body does not have `resume` in scope (only handler case bodies do). Therefore `parseHandle.body.accept(this)` is called before the `cases.forEach` loop, in the current (outer) scope. This matches the semantic model: the body is the expression being handled, not a handler definition.

**No changes to DefaultParseAstVisitor.** The bug is specifically in the `CheckNamesVisitor` override. The default visitor already traverses `children()` correctly.

## Risks / Trade-offs

**[Risk] Previously-compiling code now produces errors** -> This is the desired behavior. Programs that compiled but had undefined names in handle bodies were always incorrect; they would crash at Lua runtime. The fix surfaces these errors earlier. This is strictly an improvement.
