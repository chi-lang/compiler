## 1. Update Kotlin Plugin

- [ ] 1.1 Update `org.jetbrains.kotlin.jvm` plugin version from `1.9.25` to `2.3.10` in `build.gradle`
- [ ] 1.2 Replace the `kotlinOptions { jvmTarget = '11' }` block with `jvmToolchain(25)` in the `kotlin` extension
- [ ] 1.3 Remove the explicit `compileJava` `sourceCompatibility`/`targetCompatibility` settings (handled by `jvmToolchain`)
- [ ] 1.4 Run `./gradlew classes` and fix any K2 compiler errors in production code

## 2. Update Plugins

- [ ] 2.1 Check if `com.github.johnrengelman.shadow:8.1.1` works with Gradle 9.3.1 and Kotlin 2.3.10; if not, switch to `com.gradleup.shadow` and update to latest compatible version
- [ ] 2.2 Check if `pl.allegro.tech.build.axion-release:1.15.4` works with Kotlin 2.3.10 / Gradle 9.3.1; update if needed

## 3. Update Test Dependencies

- [ ] 3.1 Update `io.kotest:kotest-runner-junit5` from `5.0.0.M3` to latest stable (5.9.x)
- [ ] 3.2 Verify `org.junit.jupiter:junit-jupiter` resolves correctly with the updated Gradle/Kotlin versions

## 4. Verify Core Dependencies

- [ ] 4.1 Verify `org.antlr:antlr4:4.12.0` works with JVM 25 — run `./gradlew generateGrammarSource`
- [ ] 4.2 Verify `party.iroiro.luajava:luajit:3.5.0` and `luajit-platform:3.5.0` load correctly on JVM 25

## 5. Build Verification

- [ ] 5.1 Run `./gradlew build` end-to-end (compile + test) and confirm zero failures
- [ ] 5.2 Run `./gradlew shadowJar` and verify `build/libs/chi-all.jar` is produced
- [ ] 5.3 Run `java -jar build/libs/chi-all.jar` with JDK 25 to confirm the fat JAR starts without errors

## 6. Fix K2 Compiler Issues (if any)

- [ ] 6.1 Fix any type inference or nullability errors surfaced by the K2 compiler
- [ ] 6.2 Fix any deprecated API usages flagged by Kotlin 2.3.x
