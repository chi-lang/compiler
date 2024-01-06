package gh.marad.chi.core.compiler

import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import gh.marad.chi.core.utils.printAst
import org.junit.jupiter.api.Test
import java.lang.AssertionError

class Compiler2Test {
    @Test
    fun `foo` () {
        val ns = GlobalCompilationNamespace()
        ns.getOrCreatePackage("std", "io").symbols.add(
            Symbol(
                "std", "io", "println",
                SymbolKind.Local,
                Types.fn(Types.string, Types.unit),
                slot = 0, public = true, mutable = true
            )
        )

        ns.getOrCreatePackage("user", "default").symbols.add(
            Symbol(
                "user", "default", "x",
                SymbolKind.Local,
                Types.array(Types.int),
//                Types.array(Types.string),
                slot = 0,
                public = true,
                mutable = true
            )
        )

        val code = """
            fn hello(): int { 5 }
        """.trimIndent()

        val result = Compiler2.compile(code, ns)

        printAst(result.first.expressions)

        if (result.second.isNotEmpty()) {
            for (message in result.second) {
                val msg = Compiler2.formatCompilationMessage(code, message)
                println(msg)
            }
            throw AssertionError("There were errors!")
        }
    }
}