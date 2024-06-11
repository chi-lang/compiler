package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.utils.printAst
import gh.marad.chi.runtime.LuaCompilationEnv
import gh.marad.chi.runtime.LuaEnv
import org.docopt.Docopt
import party.iroiro.luajava.Lua
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.system.exitProcess

const val doc = """
Usage:
  chi [-m MODULE]... repl [ARGS ...]
  chi [-m MODULE]... compile [FILE]
  chi [-m MODULE]... [options] [FILE] [ARGS ...]
  
Options:
  -m --module=MODULE       Module file or directory to load on startup 
  -L --lang-opt=OPT        Language options
  -l                       Show emitted lua code
  --print-ast              Prints the AST for given input file and exit
  --no-std                 Prevents loading stdlib

Examples:
  chi -m std.chim program.chi
  chi repl preload.chi
  chi compile program.chi
"""

fun main(args: Array<String>) {
    val opts = Docopt(doc).parse(*args)
    val programArgs = opts["ARGS"] as ArrayList<String>
    val file = opts["FILE"] as String?
    val modules = opts["--module"] as ArrayList<String>?
    val noStd = opts["--no-std"] as Boolean

    val imports = mutableListOf<Import>()
    imports.add(Import("std", "lang", packageAlias = null, entries = listOf(
        Import.Entry("println", alias = null, section = null),
        Import.Entry("print", alias = null, section = null),
        Import.Entry("eval", alias = null, section = null)
    ), null))

    val env = LuaEnv(imports)

    val chiHome = System.getenv("CHI_HOME")
    if (!noStd && chiHome != null) {
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

    if (modules != null) {
        evalModules(env, modules)
    }

    if (opts["compile"] == true) {
        val path = Path.of(file!!)
        if (path.extension != "chi") {
            println("File $path is not a Chi source file!")
            exitProcess(1)
        }
        val nameWithoutExt = path.name.substring(0, path.name.length - path.extension.length - 1)
        val compiledPath = path.resolveSibling("$nameWithoutExt.chic")
        val code = Files.readString(path)
        // perform: load(chi_compile(code))
        // and then dump the result
        env.lua.getGlobal("load")
        env.lua.getGlobal("chi_compile")
        env.lua.push(code)
        env.lua.pCall(1, 1)
        env.lua.pCall(1, 1)
//        env.lua.get().toJavaObject().let { println(it) }
        val bytes = env.lua.dump()!!
        println(bytes)
//        Files.newOutputStream(compiledPath).use {
//            val buf = ByteArray(100)
//            while (bytes.hasRemaining()) {
//                val size = min(buf.size, bytes.remaining())
//                bytes.get(buf, 0, size)
//                it.write(buf, 0, size)
//            }
//        }
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
            env.lua.run(
                """
                    chi_load_module("${path.absolute()}")
                """.trimIndent()
            )
        } else {
            println("File does not exist: $it ")
        }
    }
}
