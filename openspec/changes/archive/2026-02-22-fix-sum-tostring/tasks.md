## 1. Reproduce bug with failing tests

- [x] 1.1 Add unit test: anonymous sum type (empty ids) toString returns `"lhs | rhs"` format — expect this to FAIL initially, confirming the bug
- [x] 1.2 Add unit test: named sum type (non-empty ids) toString returns `"ids[lhs | rhs]"` format
- [x] 1.3 Add unit test: Option sum type toString behavior is unchanged

## 2. Fix Sum.toString()

- [x] 2.1 Replace the dead `ids.toString() ?: "$lhs | $rhs"` else branch in `Sum.toString()` (`Types3.kt:148-149`) with `if (ids.isEmpty()) "$lhs | $rhs" else "$ids[$lhs | $rhs]"`
- [x] 2.2 Re-run tests from step 1 — all should now pass

## 3. Verify

- [x] 3.1 Run full test suite to verify no regressions
