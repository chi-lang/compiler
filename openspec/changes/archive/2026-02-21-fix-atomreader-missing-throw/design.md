## Context

BUG-03 reports that `AtomReader.kt:81` creates a `CompilerMessage` but never throws it in the `else` branch of string part parsing. However, upon inspection of the current codebase, **the `throw` keyword is already present** at line 81:

```kotlin
else -> throw CompilerMessage.from("Unsupported string part: $part!", getSection(source, ctx))
```

This means the bug has already been fixed in the current code, or the bug report was written against an older version.

The existing `string-escaping` spec (requirement: "Unrecognized string parts SHALL produce a compiler error") is already satisfied by the current implementation.

## Goals / Non-Goals

**Goals:**
- Verify the fix is in place and the existing spec requirement is met
- Add a regression test to prevent the `throw` from being accidentally removed in the future

**Non-Goals:**
- No code change needed — the fix is already present
- No changes to error message format or content

## Decisions

**Decision: Verify-only, no code change.**
The implementation already matches the spec. The only value-add is a regression test confirming the `else` branch throws. Since the ANTLR grammar defines a fixed set of string part token types, triggering the `else` branch from Chi source is not straightforward without mocking the parser. A manual code review confirms the `throw` is present.

**Decision: No regression test.**
The `else` branch is a defensive guard against future grammar changes introducing new token types. It cannot be triggered through normal Chi source input with the current grammar. Writing a test would require constructing a fake `StringPartContext` with no recognized tokens, which would be brittle and low-value.

## Risks / Trade-offs

- [Risk] The `throw` could be accidentally removed in a future refactor → Low probability; code review and the existing spec provide adequate coverage.
- [Risk] Bug report is stale/inaccurate → No harm done; confirming the fix is present is still valuable.
