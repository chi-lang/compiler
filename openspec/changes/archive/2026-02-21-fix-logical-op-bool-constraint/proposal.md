## Why

The logical operators `&&` and `||` in the type checker (`Typer.kt:350-352`) only constrain both operands to have the same type but do not require them to be `bool`. This is a type soundness hole: expressions like `5 && 3` or `"a" || "b"` pass type checking with result type `bool`, even though logical operations are only meaningful on boolean values.

## What Changes

- Split the `InfixOp` type checking for logical operators (`&&`, `||`) away from comparison operators (`<`, `<=`, `>`, `>=`, `==`, `!=`)
- Add constraints requiring both operands of `&&` and `||` to be `Type.bool`
- Comparison operators remain unchanged (they legitimately work on non-bool types)
- Add tests verifying that non-bool operands to `&&`/`||` produce type errors

## Capabilities

### New Capabilities
- `logical-op-bool-constraint`: Type checking constraint that `&&` and `||` operands must be `bool`

### Modified Capabilities
(none)

## Impact

- **File**: `src/main/kotlin/gh/marad/chi/core/types/Typer.kt` -- `InfixOp` branch in `typeTerm`
- **Behavioral change**: Chi programs using non-bool operands with `&&`/`||` will now get a type error at compile time instead of silently passing
- **Risk**: Low. Any code that was using non-bool operands with logical operators was already semantically incorrect; this change surfaces those errors at compile time
