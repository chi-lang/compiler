## Why

`ForLoop.children()` and `ParseFor.children()` only return `listOf(iterable, body)`, omitting the nullable `state` and `init` sub-expressions. Since `children()` is the traversal mechanism for all default visitor implementations, these fields are invisible to every compiler pass that relies on tree walking — including name checking, immutability checking, visibility checking, usage marking, and type variable resolution.

## What Changes

- Fix `ForLoop.children()` in `Expressions.kt` to include `state` and `init` using `listOfNotNull`
- Fix `ParseFor.children()` in `ForReader.kt` to include `state` and `init` using `listOfNotNull`
- Add tests verifying that compiler passes correctly analyze `state` and `init` sub-expressions in for-loops

## Capabilities

### New Capabilities
- `for-loop-ast-traversal`: Correct AST traversal of for-loop nodes, ensuring all sub-expressions (iterable, state, init, body) are visited by compiler passes

### Modified Capabilities

## Impact

- `src/main/kotlin/gh/marad/chi/core/Expressions.kt` — `ForLoop.children()` method
- `src/main/kotlin/gh/marad/chi/core/parser/readers/ForReader.kt` — `ParseFor.children()` method
- All visitors using `DefaultExpressionVisitor` and `DefaultParseAstVisitor` will now correctly visit `state` and `init` fields: `UsageMarker`, `ImmutabilityCheckVisitor`, `VisibilityCheckingVisitor`, `FnCallCheckingVisitor`, `CheckNamesVisitor`, and `replaceTypes`
