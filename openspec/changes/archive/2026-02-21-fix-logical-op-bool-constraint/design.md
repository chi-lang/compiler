## Context

The Chi type checker (`Typer.kt`) handles infix operators in a single `is InfixOp` branch. Currently, logical operators (`&&`, `||`) are grouped with comparison operators (`<`, `<=`, `>`, `>=`, `==`, `!=`) -- both emit only a `lhsType = rhsType` constraint and return `Type.bool`. This means logical operators accept any type as operands, as long as both sides match.

The `PrefixOp` handler (for `not`) already correctly constrains its operand to `Type.bool` (line 367), so there is precedent for the correct pattern.

## Goals / Non-Goals

**Goals:**
- Constrain `&&` and `||` operands to `Type.bool` during type inference
- Produce a clear type error when non-bool operands are used with logical operators
- Add test coverage for the new constraint

**Non-Goals:**
- Changing comparison operator behavior (they correctly accept non-bool types)
- Changing arithmetic operator behavior
- Adding truthy/falsy coercion semantics

## Decisions

### Split logical operators from comparison operators in the InfixOp branch

**Choice**: Introduce a separate `if` branch for `&&` and `||` that adds `Type.bool` constraints for both operands, rather than modifying the existing shared condition.

**Rationale**: Comparison operators (`<`, `>`, etc.) legitimately work on non-bool types (e.g., `3 < 5`). They only need to ensure both sides are the same type. Logical operators have a stricter requirement: both sides must be `bool`. Splitting the condition makes each case explicit and correct.

**Alternative considered**: Adding `Type.bool` constraint to the existing shared branch -- rejected because it would break comparison operators on non-bool types.

### Emit two separate constraints (one per operand) rather than one

**Choice**: Add `Constraint(Type.bool, lhsType, ...)` and `Constraint(Type.bool, rhsType, ...)` as two separate constraints.

**Rationale**: This matches how `PrefixOp` handles the `not` operator and produces better error messages -- the error points to the specific operand that is not `bool`, not just the overall expression.

## Risks / Trade-offs

- **[Risk] Existing code that abuses `&&`/`||` with non-bool types breaks** → This is intentional. Such code was never type-safe and would produce undefined behavior at runtime. Surfacing compile errors is correct behavior.
- **[Risk] No existing tests for the InfixOp type inference path** → Mitigated by adding new tests for both the happy path (bool operands accepted) and the error path (non-bool operands rejected).
