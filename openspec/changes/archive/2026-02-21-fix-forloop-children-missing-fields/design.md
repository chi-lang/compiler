## Context

The Chi compiler uses a tree-walking visitor pattern for all analysis passes. Both the parse AST (`ParseAst`) and expression AST (`Expression`) hierarchies define a `children()` method that returns all child nodes. Default visitor implementations (`DefaultExpressionVisitor`, `DefaultParseAstVisitor`) call `children()` to recursively visit the tree.

The `ForLoop` node has four sub-expression fields: `iterable`, `state` (nullable), `init` (nullable), and `body`. Similarly, `ParseFor` has matching fields. Both `children()` methods currently return only `listOf(iterable, body)`, omitting `state` and `init`.

This means six compiler passes that use default traversal skip these fields entirely: `UsageMarker`, `ImmutabilityCheckVisitor`, `VisibilityCheckingVisitor`, `FnCallCheckingVisitor`, `CheckNamesVisitor`, and the `replaceTypes` operation.

## Goals / Non-Goals

**Goals:**
- Ensure `ForLoop.children()` and `ParseFor.children()` return all sub-expressions including nullable `state` and `init`
- All existing compiler visitor passes correctly analyze for-loop state and init expressions
- Resolved type variables are applied back to state/init via `replaceTypes`

**Non-Goals:**
- Changing the for-loop syntax or semantics
- Adding new visitor methods specific to for-loop state/init
- Modifying the Typer — it already generates constraints for state/init correctly

## Decisions

**Use `listOfNotNull` instead of `listOf`**: Since `state` and `init` are nullable (`Expression?` / `ParseAst?`), we use `listOfNotNull(iterable, state, init, body)` to include them only when present. This is the idiomatic Kotlin approach and matches how nullable children are handled elsewhere in the codebase. The alternative of filtering nulls from a `listOf` is more verbose with no benefit.

**No visitor changes needed**: The fix is purely in the `children()` methods. All visitors already traverse children automatically via default implementations. No per-visitor changes are required.

## Risks / Trade-offs

**[Risk] Existing tests may not cover for-loop state/init paths** → We add targeted tests that verify compiler passes detect errors in state/init sub-expressions. This confirms the fix is effective end-to-end.

**[Risk] Visitor ordering assumptions** → The order of children changes from `[iterable, body]` to `[iterable, state, init, body]`. No visitor in the codebase depends on child ordering for correctness; they all process each child independently. Risk is negligible.
