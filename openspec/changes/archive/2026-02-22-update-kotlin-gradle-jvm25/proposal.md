## Why

The Chi compiler targets JVM 11 using Kotlin 1.9.25, while the development environment runs OpenJDK 25 (GraalVM CE). Kotlin 1.9.x is end-of-life and does not support JVM 25 bytecode emission. Updating to the current Kotlin (2.3.10) and targeting JVM 25 aligns the compiler output with the runtime, enables modern JVM optimizations, and keeps the project on supported tooling.

## What Changes

- **BREAKING**: JVM target raised from 11 to 25 — compiled artifacts require a JVM 25+ runtime
- Kotlin plugin updated from 1.9.25 to 2.3.10 (K2 compiler becomes default)
- `kotlinOptions` block migrated to `compilerOptions` DSL (deprecated in Kotlin 2.x)
- `sourceCompatibility` / `targetCompatibility` updated from `VERSION_11` to `VERSION_25`
- Gradle wrapper kept at 9.3.1 (already installed, supports JVM 25; exceeds Kotlin 2.3.10's official max of 9.0.0 but is usable with possible deprecation warnings)
- Shadow plugin updated from 8.1.1 to latest compatible version
- Kotest updated from 5.0.0.M3 (milestone/pre-release) to latest stable (5.9.x)
- axion-release plugin checked/updated for Kotlin 2.x and Gradle 9.x compatibility
- ANTLR 4.12.0 and luajava 3.5.0 compatibility verified on JVM 25

## Capabilities

### New Capabilities
- `jvm25-build-support`: Documents the build toolchain requirements, JVM target configuration, and dependency compatibility constraints for JVM 25

### Modified Capabilities

(none — this change affects build infrastructure only, no language behavior changes)

## Impact

- **Build files**: `build.gradle` — plugin versions, compilation options, dependency versions
- **Gradle wrapper**: `gradle/wrapper/gradle-wrapper.properties` — may update if a newer Gradle version improves Kotlin 2.3.x compatibility
- **Runtime**: Compiled JAR (`chi-all.jar`) will require JVM 25+ to run
- **CI/CD**: Any CI pipelines must use JDK 25+
- **Dependencies**: Shadow, Kotest, axion-release plugin versions change; ANTLR and luajava verified
- **Source code**: Potential minor fixes if K2 compiler is stricter about type inference or nullability in existing code
