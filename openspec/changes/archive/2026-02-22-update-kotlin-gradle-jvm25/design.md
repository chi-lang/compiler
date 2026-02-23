## Context

The Chi compiler is built with Kotlin 1.9.25 targeting JVM 11, using Gradle 9.3.1 as the build system. The development environment runs OpenJDK 25.0.2 (GraalVM CE). Kotlin 1.9.x is the final release before the 2.x line which introduced the K2 compiler as default. The build uses the Groovy DSL (`build.gradle`), the `kotlinOptions` block for JVM target configuration, and several plugins: Shadow (8.1.1), axion-release (1.15.4), ANTLR (bundled), and maven-publish (bundled).

Key dependencies: ANTLR 4.12.0, luajava/luajit 3.5.0, Kotest 5.0.0.M3 (pre-release milestone).

## Goals / Non-Goals

**Goals:**
- Update Kotlin from 1.9.25 to 2.3.10 (latest stable with JVM 25 bytecode support)
- Raise JVM target from 11 to 25 so compiled output matches the runtime
- Update all plugin and dependency versions to be compatible with the new Kotlin/JVM target
- Migrate deprecated `kotlinOptions` to the `compilerOptions` DSL
- Ensure the full test suite passes after the upgrade

**Non-Goals:**
- Migrating `build.gradle` (Groovy DSL) to `build.gradle.kts` (Kotlin DSL) — separate effort
- Upgrading the Chi language itself or changing language semantics
- Adding new language features or modifying the type system
- Setting up CI/CD pipelines (though CI must use JDK 25+ after this change)

## Decisions

### 1. Kotlin 2.3.10 (latest stable)

**Choice**: Update from 1.9.25 to 2.3.10.

**Rationale**: Kotlin 2.3.0 was the first release with JVM 25 bytecode support. 2.3.10 is the latest patch release with bug fixes. The K2 compiler (default since 2.0) is mature at this point.

**Alternative considered**: Kotlin 2.1.x — does not support JVM 25 bytecode target. Kotlin 2.2.x — supports only up to JVM 24.

### 2. Keep Gradle 9.3.1

**Choice**: Retain the current Gradle 9.3.1 wrapper version.

**Rationale**: Gradle 9.1.0+ has full JVM 25 support. The current 9.3.1 is already installed and working. While Kotlin 2.3.10's official max supported Gradle is 9.0.0, the Kotlin docs state versions above the max can be used with possible deprecation warnings. Downgrading Gradle to 9.0.0 would lose JVM 25 build-time support.

**Alternative considered**: Downgrade to Gradle 9.0.0 — rejected because Gradle 9.0.0 lacks full JVM 25 support, creating a paradox where the build tool can't fully run on the same JVM we target.

### 3. JVM target 25 via `compilerOptions` and `jvmToolchain`

**Choice**: Use `jvmToolchain(25)` in the Kotlin extension rather than manually setting `compilerOptions.jvmTarget`, `sourceCompatibility`, and `targetCompatibility`.

**Rationale**: `jvmToolchain` is the idiomatic Kotlin 2.x approach. It sets the JVM target for both Kotlin and Java compilation tasks uniformly, eliminating the risk of target mismatch. It replaces the three separate settings currently in `build.gradle`.

**Alternative considered**: Manually setting `compilerOptions.jvmTarget = JvmTarget.JVM_25` plus `sourceCompatibility`/`targetCompatibility` — more verbose, more error-prone, no real advantage.

### 4. Shadow plugin update to 8.1.1+

**Choice**: Check if Shadow 8.1.1 works with Kotlin 2.3.10 / Gradle 9.3.1. If not, update to the latest compatible version (the `com.gradleup.shadow` fork if the johnrengelman version is abandoned).

**Rationale**: The Shadow plugin has changed maintainership. The `com.github.johnrengelman.shadow` artifact may not support Gradle 9.x. The `com.gradleup.shadow` fork is the actively maintained successor.

### 5. Kotest update to latest stable

**Choice**: Update from 5.0.0.M3 (milestone pre-release) to 5.9.x (latest stable).

**Rationale**: 5.0.0.M3 is a pre-release milestone from ~2021. It may have incompatibilities with Kotlin 2.x. The latest stable Kotest has full Kotlin 2.x support and many bug fixes.

### 6. Verify ANTLR and luajava — no preemptive update

**Choice**: Keep ANTLR 4.12.0 and luajava 3.5.0 at current versions unless compilation or tests fail.

**Rationale**: These are JVM libraries with no known JVM 25 incompatibilities. ANTLR generates Java source (not bytecode), so the JVM target of the generated code is controlled by our build. luajava uses JNI/native bindings which are JVM-version-independent at the API level.

## Risks / Trade-offs

**[K2 compiler strictness]** → The K2 compiler may reject code that 1.9.x accepted (stricter type inference, nullability checks). Mitigation: run the full test suite and fix any compilation errors incrementally.

**[Gradle compatibility warnings]** → Gradle 9.3.1 exceeds Kotlin 2.3.10's official max (9.0.0). Mitigation: warnings are non-fatal; if blocking issues arise, evaluate upgrading Kotlin or pinning Gradle.

**[Shadow plugin breakage]** → The `johnrengelman.shadow` plugin may not support Gradle 9.x. Mitigation: switch to `com.gradleup.shadow` fork which is actively maintained.

**[luajava native bindings on JVM 25]** → JNI changes across major JVM versions could affect native library loading. Mitigation: the `luajit-platform` natives-desktop artifact handles platform detection; run integration tests to verify.

**[Runtime requirement increase]** → Users must have JVM 25+ to run `chi-all.jar`. Mitigation: this is an intentional decision per the proposal. Document the requirement.
