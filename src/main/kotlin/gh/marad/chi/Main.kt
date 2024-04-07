package gh.marad.chi

import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.runtime.LuaEnv
import org.docopt.Docopt
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.system.exitProcess

const val doc = """
Usage:
  chi repl [ARGS ...]
  chi compile [FILE]
  chi [-m MODULE ...] [-L OPT ...] [-l] [FILE] [ARGS ...]
  
Options:
  -m --module=MODULE       Module file or directory to load on startup
  -L --lang-opt=OPT        Language options
  -l                       Show emitted lua code

Examples:
  chi -m std.chim program.chi
  chi repl preload.chi
  chi compile program.chi
"""

fun main(args: Array<String>) {
    val opts = Docopt(doc).parse(*args)
    val programArgs = opts["ARGS"] as ArrayList<String>
    val file = opts["FILE"] as String?
    val modules = opts["--module"] as ArrayList<*>

    val prelude = mutableListOf<PreludeImport>()
    prelude.add(PreludeImport("std", "lang", "println", null))
    prelude.add(PreludeImport("std", "lang", "eval", null))

    val env = LuaEnv(prelude)

    // TODO: load modules

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
            val onlyShowLuaCode = opts["-l"] as Boolean
            env.eval(source, dontEvalOnlyShowLuaCode = onlyShowLuaCode)
        } else if (file.endsWith(".chic")) {
            println("Running compiled code")
        } else {
            println("Unknown file type '$file'")
            exitProcess(1)
        }
    }

}