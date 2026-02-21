## 1. Fix Recursive.withAddedTypeIds

- [x] 1.1 Change `withAddedTypeIds` in `Types3.kt:257-263` to use `copy(type = type.withAddedTypeIds(ids))` when inner type is `HasTypeId`
- [x] 1.2 Change the `else` branch to return `this` instead of `type`

## 2. Tests

- [x] 2.1 Add test: `withAddedTypeIds` on a `Recursive` wrapping a `HasTypeId` inner type returns a `Recursive` instance with IDs added
- [x] 2.2 Add test: `withAddedTypeIds` on a `Recursive` wrapping a non-`HasTypeId` inner type returns the original `Recursive` unchanged
- [x] 2.3 Add test: verify consistency between `withAddedTypeId` (singular) and `withAddedTypeIds` (plural) — both preserve the `Recursive` wrapper

## 3. Verification

- [x] 3.1 Run `./gradlew test` and confirm all tests pass
