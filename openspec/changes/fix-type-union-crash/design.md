## Context

`Type.union()` in `Types3.kt:48-49` is a companion-object factory that constructs a sum type from a variadic list of types. It uses `reduceRight` with `Sum.create` to fold types pairwise, then unconditionally casts the result `as Sum`.

`Sum.create` (line 161-165) flattens both sides via `listTypes` (which returns a `Set<Type>`), then calls `types.reduce` to reassemble. When the set has only one element (all input types were duplicates), `reduce` returns that single element directly — which is not a `Sum`.

The `as Sum` cast then throws `ClassCastException`.

Currently `Type.union` is called in two test files but could be called from future compiler code. The `Sum.create` factory is used more broadly (Compiler.kt, Typer.kt, VariableReplacer.kt, TypeVisitor.kt).

## Goals / Non-Goals

**Goals:**
- Prevent `ClassCastException` when `Type.union()` is called with types that deduplicate to a single type
- Maintain backward compatibility with existing callers

**Non-Goals:**
- Changing `Sum.create` behavior (it already correctly returns `Type`, not `Sum`)
- Addressing other `Sum`-related bugs (BUG-10, BUG-13 are separate)

## Decisions

### Decision 1: Change `Type.union()` return type from `Sum` to `Type`

**Rationale:** `Sum.create` already returns `Type` (not `Sum`). The `union()` method wraps `Sum.create`, so its return type should match. Forcing `Sum` is incorrect when deduplication reduces the set to one type.

**Alternative considered:** Keep return type as `Sum` and wrap single-type results in `Sum(ids, type, type)`. Rejected because this creates a semantically incorrect sum type (a type unioned with itself) and would break equality checks.

**Alternative considered:** Guard with an early-return that throws on duplicate-only inputs. Rejected because returning the single type is the correct semantic behavior — the union of `int` and `int` is just `int`.

### Decision 2: Update call sites to use `Type` instead of `Sum`

Both call sites in tests store the result as `Sum` or rely on the `Sum` type. These need to either:
- Change their type annotation to `Type`, or
- Remain as-is if Kotlin can infer the type

The test in `TypeCheckingSpec.kt:141` already compares against a generic matcher (`shouldBe`), so the type widening is transparent. The test in `ObjectsSpec.kt:72` uses the result as a type parameter, which also accepts `Type`.

## Risks / Trade-offs

- **[Low risk] Widened return type** — any code that explicitly declared `val x: Sum = Type.union(...)` would get a compile error. Mitigated by: only two call sites exist, both in tests, and both work fine with `Type`.
- **[No risk] Behavioral change** — `Sum.create` behavior is unchanged. The only change is removing the unsafe cast.
