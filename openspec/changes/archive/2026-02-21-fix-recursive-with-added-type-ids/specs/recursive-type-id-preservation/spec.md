## ADDED Requirements

### Requirement: Recursive.withAddedTypeIds preserves wrapper
The `Recursive.withAddedTypeIds(ids)` method SHALL return a `Recursive` instance (via `copy()`) when the inner type implements `HasTypeId`, preserving the recursive wrapper around the modified inner type.

#### Scenario: Adding type IDs to a Recursive wrapping a HasTypeId inner type
- **WHEN** `withAddedTypeIds(ids)` is called on a `Recursive` instance whose inner `type` implements `HasTypeId`
- **THEN** the result SHALL be a `Recursive` instance with the same `variable` and an inner type that has the IDs added

#### Scenario: Adding type IDs to a Recursive wrapping a non-HasTypeId inner type
- **WHEN** `withAddedTypeIds(ids)` is called on a `Recursive` instance whose inner `type` does NOT implement `HasTypeId`
- **THEN** the result SHALL be the original `Recursive` instance unchanged (return `this`)

### Requirement: Consistency between singular and plural withAddedTypeId methods
The `Recursive` type's `withAddedTypeIds` (plural) SHALL follow the same wrapper-preservation pattern as `withAddedTypeId` (singular) — both MUST use `copy()` to wrap the result and both MUST return `this` when no modification is needed.

#### Scenario: Both methods preserve Recursive wrapper
- **WHEN** `withAddedTypeId(id)` and `withAddedTypeIds(listOf(id))` are each called on the same `Recursive` instance with a `HasTypeId` inner type
- **THEN** both results SHALL be `Recursive` instances (not bare inner types)
