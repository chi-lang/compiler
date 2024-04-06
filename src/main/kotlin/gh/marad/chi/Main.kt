package gh.marad.chi

import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.runtime.LuaEnv
import org.docopt.Docopt
import java.nio.file.Files
import java.nio.file.Path

const val doc = """
Usage:
  chi repl [ARGS ...]
  chi [-m MODULE ...] [-L OPT ...] [FILE] [ARGS ...]
  
Options:
  -m --module=MODULE       Module file or directory to load on startup
  -L --lang-opt=OPT        Language options

Examples:
  chi -m std.chim program.chi
  chi repl preload.chi
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

    if (file == null || "repl" == file) {
        val path = programArgs.take(1).filter { it.endsWith(".chi")  }.singleOrNull()
        if (path != null) {
            env.eval(Files.readString(Path.of(path)))
        }
        Repl(env).run()
    } else {
        val source = Files.readString(Path.of(file))
        env.eval(source)
    }

}