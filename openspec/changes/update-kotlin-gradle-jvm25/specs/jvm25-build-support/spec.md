## ADDED Requirements

### Requirement: Kotlin version is 2.3.x or later
The build system SHALL use Kotlin 2.3.10 or later as the Kotlin JVM plugin version, enabling JVM 25 bytecode emission and the K2 compiler.

#### Scenario: Kotlin plugin version in build script
- **WHEN** the build script is evaluated
- **THEN** the `org.jetbrains.kotlin.jvm` plugin version SHALL be `2.3.10` or later

### Requirement: JVM target is 25
The compiler SHALL produce JVM 25 bytecode for all Kotlin and Java source files. Both Kotlin `jvmTarget` and Java `sourceCompatibility`/`targetCompatibility` SHALL be set to 25.

#### Scenario: Kotlin compilation targets JVM 25
- **WHEN** the `compileKotlin` task runs
- **THEN** the emitted `.class` files SHALL have class file major version 69 (Java 25)

#### Scenario: Java compilation targets JVM 25
- **WHEN** the `compileJava` task runs
- **THEN** the emitted `.class` files SHALL have class file major version 69 (Java 25)

### Requirement: Gradle version supports JVM 25
The Gradle wrapper SHALL use a version that supports running on and building with JVM 25 (Gradle 9.1.0 or later).

#### Scenario: Gradle wrapper version
- **WHEN** `gradle/wrapper/gradle-wrapper.properties` is inspected
- **THEN** the `distributionUrl` SHALL reference Gradle 9.1.0 or later

### Requirement: compilerOptions DSL is used
The build script SHALL use the Kotlin `compilerOptions` DSL (or `jvmToolchain`) instead of the deprecated `kotlinOptions` block.

#### Scenario: No deprecated kotlinOptions usage
- **WHEN** the build script is inspected
- **THEN** there SHALL be no `kotlinOptions` block
- **THEN** JVM target configuration SHALL use `jvmToolchain` or `compilerOptions`

### Requirement: All dependencies are compatible with JVM 25
All project dependencies SHALL load and function correctly on a JVM 25 runtime. This includes Shadow plugin, ANTLR, luajava, Kotest, axion-release, and JUnit.

#### Scenario: Full build succeeds on JVM 25
- **WHEN** `./gradlew build` is executed with JDK 25
- **THEN** the build SHALL complete without errors

#### Scenario: All tests pass on JVM 25
- **WHEN** `./gradlew test` is executed with JDK 25
- **THEN** all existing tests SHALL pass

### Requirement: Shadow JAR runs on JVM 25
The fat JAR produced by the Shadow plugin SHALL be executable on a JVM 25 runtime.

#### Scenario: Shadow JAR execution
- **WHEN** `java -jar build/libs/chi-all.jar` is run with JDK 25
- **THEN** the application SHALL start without `UnsupportedClassVersionError` or classloading failures
