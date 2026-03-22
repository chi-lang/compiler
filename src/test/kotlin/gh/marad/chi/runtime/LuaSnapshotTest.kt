package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

/**
 * Lua snapshot tests for the Chi language migration (Phase 0, section 0.3).
 *
 * For a curated subset of golden test programs, captures the Lua code generated
 * by the compiler and stores it as a *.lua.snapshot file next to the .chi file.
 *
 * Purpose: NOT to verify correctness of the generated Lua (that changes in Phase 1),
 * but to make changes to the emitter visible during code review.
 *
 * On first run: creates snapshot files.
 * On subsequent runs: asserts that the generated Lua hasn't changed.
 * To regenerate: delete the *.lua.snapshot file and re-run.
 */
class LuaSnapshotTest {

    companion object {
        private val GOLDEN_DIR = Paths.get("..").resolve("golden").toAbsolutePath().normalize()

        /** Subset of golden programs for Lua snapshots — one per category. */
        private val SNAPSHOT_PROGRAMS = listOf(
            "strings/basic.chi",
            "strings/interpolation.chi",
            "strings/operations.chi",
            "types/records.chi",
            "types/arrays.chi",
            "types/sum_types.chi",
            "control_flow/if_else.chi",
            "control_flow/while.chi",
            "functions/lambdas.chi",
            "functions/closures.chi"
        )
    }

    @TestFactory
    fun luaSnapshotTests(): List<DynamicTest> {
        if (!GOLDEN_DIR.exists()) {
            println("[LuaSnapshots] golden/ directory not found at $GOLDEN_DIR — no tests generated")
            return emptyList()
        }

        return SNAPSHOT_PROGRAMS.mapNotNull { relPath ->
            val chiFile = GOLDEN_DIR.resolve(relPath).toFile()
            if (!chiFile.exists()) {
                println("[LuaSnapshots] WARNING: $relPath not found, skipping")
                null
            } else {
                DynamicTest.dynamicTest("snapshot: $relPath") {
                    runSnapshotTest(chiFile)
                }
            }
        }
    }

    /**
     * Create a LuaEnv with the standard prelude imports (println, print, eval from std/lang),
     * mirroring how Main.kt sets up the runtime environment.
     */
    private fun createEnvWithPrelude(): LuaEnv {
        val imports = mutableListOf<Import>()
        imports.add(Import("std", "lang", packageAlias = null, entries = listOf(
            Import.Entry("println", alias = null, section = null),
            Import.Entry("print", alias = null, section = null),
            Import.Entry("eval", alias = null, section = null)
        ), section = null))
        return LuaEnv(imports)
    }

    private fun loadStdlib(env: LuaEnv) {
        val chiHome = System.getenv("CHI_HOME") ?: "${System.getProperty("user.home")}/.local"
        val stdlibPath = java.nio.file.Path.of(chiHome, "lib", "std.chim")
        if (java.nio.file.Files.exists(stdlibPath)) {
            env.evalLua("""chi_load_module("$chiHome/lib/std.chim")""")
        } else {
            println("[LuaSnapshots] WARN: std.chim not found at $stdlibPath — stdlib imports will fail")
        }
    }

    private fun runSnapshotTest(chiFile: File) {
        val chiCode = chiFile.readText()
        val env = createEnvWithPrelude()
        loadStdlib(env)
        val luaCode = LuaCompiler(env).compileToLua(chiCode, LuaCompiler.ErrorStrategy.THROW)
            ?: error("Compilation failed for ${chiFile.name}")

        val snapshotFile = File(chiFile.parentFile, chiFile.nameWithoutExtension + ".lua.snapshot")

        if (!snapshotFile.exists()) {
            snapshotFile.writeText(luaCode)
            println("[LuaSnapshots] Generated ${snapshotFile.name}")
            return
        }

        val expected = snapshotFile.readText()
        if (expected != luaCode) {
            // Show a diff-friendly error: print both sides
            val expectedLines = expected.lines()
            val actualLines = luaCode.lines()
            val sb = StringBuilder()
            sb.appendLine("Lua snapshot changed for ${chiFile.name}")
            sb.appendLine("If this change is intentional, delete ${snapshotFile.absolutePath} and re-run.")
            sb.appendLine()
            sb.appendLine("--- expected (${expectedLines.size} lines)")
            sb.appendLine("+++ actual   (${actualLines.size} lines)")
            val maxLines = maxOf(expectedLines.size, actualLines.size)
            for (i in 0 until minOf(maxLines, 50)) {
                val exp = expectedLines.getOrElse(i) { "<missing>" }
                val act = actualLines.getOrElse(i) { "<missing>" }
                if (exp != act) {
                    sb.appendLine("L${i+1} - $exp")
                    sb.appendLine("L${i+1} + $act")
                }
            }
            error(sb.toString())
        }
    }
}
