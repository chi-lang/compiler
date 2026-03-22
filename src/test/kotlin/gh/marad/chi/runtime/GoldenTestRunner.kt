package gh.marad.chi.runtime

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Golden test framework for the Chi language migration (Phase 0).
 *
 * Discovers all *.chi files under chi/golden/ (sibling directory of the compiler),
 * runs each one through the `chi` CLI, captures stdout, and compares against
 * the corresponding *.expected file.
 *
 * On first run (no *.expected file present) the captured output is written as
 * the expected baseline — subsequent runs assert equality.
 *
 * The stdlib is compiled automatically before the test suite runs so that
 * programs that import std/... work correctly.
 */
class GoldenTestRunner {

    companion object {
        /** Root of the golden test programs, relative to the compiler project root. */
        private val GOLDEN_DIR: Path = run {
            // When Gradle runs tests the working directory is the compiler/ folder.
            Paths.get("..").resolve("golden").toAbsolutePath().normalize()
        }

        /** Path to the stdlib Makefile, used to (re)compile std.chim before tests. */
        private val STDLIB_DIR: Path = run {
            Paths.get("..").resolve("stdlib").toAbsolutePath().normalize()
        }

        /** `chi` executable resolved from PATH or from the known install location. */
        private val CHI_EXE: String = resolveChiExecutable()

        private fun resolveChiExecutable(): String {
            // Try PATH first
            val fromPath = ProcessBuilder("which", "chi")
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader().readText().trim()
            if (fromPath.isNotBlank() && File(fromPath).exists()) {
                return fromPath
            }
            // Fall back to home-local install
            val homeLocal = Paths.get(System.getProperty("user.home"), ".local", "bin", "chi")
            if (homeLocal.exists()) return homeLocal.absolutePathString()
            error("Cannot locate `chi` executable. Make sure it is on PATH or installed at ~/.local/bin/chi")
        }
    }

    /**
     * Before discovering tests, compile the stdlib so that std.chim is up-to-date.
     * A missing or stale std.chim would cause all programs that import stdlib to fail.
     */
    private fun ensureStdlibCompiled() {
        if (!STDLIB_DIR.exists()) {
            println("[GoldenTests] WARNING: stdlib directory not found at $STDLIB_DIR — skipping stdlib compile")
            return
        }
        println("[GoldenTests] Compiling stdlib in $STDLIB_DIR ...")
        val result = ProcessBuilder("make", "compile")
            .directory(STDLIB_DIR.toFile())
            .redirectErrorStream(true)
            .start()
        val output = result.inputStream.bufferedReader().readText()
        val exitCode = result.waitFor()
        if (exitCode != 0) {
            println("[GoldenTests] WARNING: stdlib compile failed (exit $exitCode):\n$output")
        } else {
            println("[GoldenTests] stdlib compiled successfully")
        }
    }

    @TestFactory
    fun goldenTests(): List<DynamicTest> {
        if (!GOLDEN_DIR.exists()) {
            println("[GoldenTests] golden/ directory not found at $GOLDEN_DIR — no tests generated")
            return emptyList()
        }

        ensureStdlibCompiled()

        val chiFiles = GOLDEN_DIR.toFile()
            .walkTopDown()
            .filter { it.isFile && it.extension == "chi" }
            .sorted()
            .toList()

        if (chiFiles.isEmpty()) {
            println("[GoldenTests] No *.chi files found under $GOLDEN_DIR")
            return emptyList()
        }

        return chiFiles.map { chiFile ->
            val relPath = GOLDEN_DIR.toFile().toURI().relativize(chiFile.toURI()).path
            DynamicTest.dynamicTest(relPath) {
                runGoldenTest(chiFile)
            }
        }
    }

    private fun runGoldenTest(chiFile: File) {
        val actualOutput = runChiProgram(chiFile)
        val expectedFile = File(chiFile.parentFile, chiFile.nameWithoutExtension + ".expected")

        if (!expectedFile.exists()) {
            // First run: generate the expected file from actual output
            expectedFile.writeText(actualOutput)
            println("[GoldenTests] Generated ${expectedFile.name} (${actualOutput.lines().size} lines)")
            return
        }

        val expectedOutput = expectedFile.readText()
        assertEquals(
            expectedOutput,
            actualOutput,
            "Golden test failed for ${chiFile.name}\n" +
            "Expected file: ${expectedFile.absolutePath}"
        )
    }

    private fun runChiProgram(chiFile: File): String {
        val process = ProcessBuilder(CHI_EXE, chiFile.absolutePath)
            .redirectErrorStream(false)
            .start()

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            val errorDetail = if (stderr.isNotBlank()) stderr else "(no stderr)"
            error("chi exited with code $exitCode for ${chiFile.name}\nstderr: $errorDetail\nstdout: $stdout")
        }

        // Normalise line endings (Windows safety) and trim trailing whitespace
        return stdout.replace("\r\n", "\n")
    }
}
