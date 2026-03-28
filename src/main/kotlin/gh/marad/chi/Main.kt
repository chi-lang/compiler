package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.utils.printAst
import gh.marad.chi.runtime.LuaCompilationEnv
import gh.marad.chi.runtime.LuaCompiler
import gh.marad.chi.runtime.LuaEnv
import org.docopt.Docopt
import party.iroiro.luajava.Lua
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.system.exitProcess

const val doc = """
Usage:
  chi repl [ARGS ...]
  chi compile [-o FILE] [--bundle] FILE
  chi [options] [FILE] [ARGS ...]

Options:
  -o --output=FILE         Output file path (default: <input>.lua)
  --bundle                 Bundle runtime + stdlib into output file
  -m --module=MODULE       Module file or directory to load on startup
  -L --lang-opt=OPT        Language options
  -l                       Show emitted lua code
  --print-ast              Prints the AST for given input file and exit

Examples:
  chi program.chi                               Run a Chi program
  chi compile program.chi                       Compile to program.lua
  chi compile program.chi -o out.lua            Compile to specific path
  chi compile --bundle program.chi -o out.lua   Compile with bundled runtime
  chi repl                                      Start REPL
"""

fun main(args: Array<String>) {
    val opts = Docopt(doc).parse(*args)
    val programArgs = opts["ARGS"] as ArrayList<String>
    val file = opts["FILE"] as String?
    //val modules = opts["--module"] as ArrayList<String>
    val modules = ArrayList<String>()

    val imports = mutableListOf<Import>()
    imports.add(Import("std", "lang", packageAlias = null, entries = listOf(
        Import.Entry("println", alias = null, section = null),
        Import.Entry("print", alias = null, section = null),
        Import.Entry("eval", alias = null, section = null)
    ), null))

    val env = LuaEnv(imports)

    val chiHome = System.getenv("CHI_HOME")
    if (chiHome != null) {
//        env.setModuleLoader(
//            ModuleLoader(
//                LuaCompiler(env), Path.of(chiHome,"lib")
//            )
//        )
        if (Files.exists(Path.of(chiHome, "lib", "std.chim"))) {
            val result = env.lua.run(
                """
                    chi_load_module("$chiHome/lib/std.chim")
                """.trimIndent()
            )
            if (result != Lua.LuaError.OK) {
                println("Error loading stdlib: ${env.lua.get().toJavaObject()}")
            } else {
                // If no error add additional imports
                imports.add(Import("std", "lang.option", packageAlias = null, entries = listOf(
                    Import.Entry("Option", alias = null, section = null)
                ), section = null))
            }
        } else {
            println("WARN: Missing stdlib. Things might not work as expected!")
        }
    }

    evalModules(env, modules)

    if (opts["compile"] == true) {
        val path = Path.of(file!!)
        if (path.extension != "chi") {
            System.err.println("Error: $path is not a Chi source file (.chi)")
            exitProcess(1)
        }

        val source = Files.readString(path)
        val luaCode = LuaCompiler(env).compileToLua(source, LuaCompiler.ErrorStrategy.PRINT)
        if (luaCode == null) {
            System.err.println("Compilation failed.")
            exitProcess(1)
        }

        val bundle = opts["--bundle"] as Boolean
        val outputPath = (opts["--output"] as String?)
            ?.let { Path.of(it) }
            ?: path.resolveSibling(path.name.removeSuffix(".chi") + ".lua")

        val output = if (bundle) {
            buildBundle(luaCode)
        } else {
            luaCode
        }

        Files.writeString(outputPath, output)
        println("Compiled: ${path.name} -> $outputPath" +
                if (bundle) " (bundled)" else "")
    } else if (file == null || opts["repl"] == true) {
        val path = programArgs.take(1).singleOrNull { it.endsWith(".chi") }
        if (path != null) {
            env.eval(Files.readString(Path.of(path)))
        }
        Repl(env).run()
    } else {
        if (file.endsWith(".chi")) {
            val source = Files.readString(Path.of(file))
            val showLuaCode = opts["-l"] as Boolean
            val printAst = opts["--print-ast"] as Boolean
            if (printAst) {
                val ns = LuaCompilationEnv(env)
                val result = Compiler.compile(source, ns)
                if (result.hasErrors()) {
                    result.messages.forEach { message ->
                        println(Compiler.formatCompilationMessage(source, message))
                    }
                }
                printAst(result.program.expressions)
            } else {
                env.eval(source, showLuaCode = showLuaCode, dryRun = showLuaCode)
            }
        } else if (file.endsWith(".chic")) {
            println("Running compiled code")
        } else {
            println("Unknown file type '$file'")
            exitProcess(1)
        }
    }
}

fun evalModules(env: LuaEnv, modules: java.util.ArrayList<String>) {
    modules.forEach {
        val path = Path.of(it)
        if (path.exists()) {
            println(path.extension)
            env.eval(Files.readString(path))
        } else {
            println("File does not exist: $it ")
        }
    }
}

private fun buildBundle(programLua: String): String {
    val utf8 = LuaEnv::class.java.getResourceAsStream("/utf8.lua")!!
        .bufferedReader().readText()
    val chistr = LuaEnv::class.java.getResourceAsStream("/chistr.lua")!!
        .bufferedReader().readText()
    val runtime = LuaEnv::class.java.getResourceAsStream("/chi_runtime.lua")!!
        .bufferedReader().readText()

    // Load stdlib from CHI_HOME if available
    val chiHome = System.getenv("CHI_HOME")
    val stdlib = if (chiHome != null) {
        val stdlibPath = Path.of(chiHome, "lib", "std.chim")
        if (stdlibPath.exists()) {
            Files.readString(stdlibPath)
        } else {
            System.err.println("Warning: $stdlibPath not found, bundle will not include stdlib")
            ""
        }
    } else {
        System.err.println("Warning: CHI_HOME not set, bundle will not include stdlib")
        ""
    }

    return buildString {
        appendLine("-- Chi standalone bundle")
        appendLine("-- Generated by chi compile --bundle")
        appendLine()
        appendLine("-- UTF-8 support")
        appendLine("utf8 = (function()")
        appendLine(utf8)
        appendLine("end)()")
        appendLine()
        appendLine("-- Chi string utilities")
        appendLine("chistr = (function()")
        appendLine(chistr)
        appendLine("end)()")
        appendLine()
        appendLine("-- Chi runtime")
        appendLine(runtime)
        appendLine()
        if (stdlib.isNotEmpty()) {
            appendLine("-- Standard library")
            appendLine(stdlib)
            appendLine()
        }
        appendLine("-- User program")
        appendLine(programLua)
    }
}
