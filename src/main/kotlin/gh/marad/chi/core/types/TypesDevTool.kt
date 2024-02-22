package gh.marad.chi.core.types

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.utils.printAst

fun main() {
    val code = """
        fn floor(input: string): int {
            val points = [1, 2, 3]
            val open = 40
            val close = 41
            var i = 0
            var fl = 0
            while i < 5 {
                if points[i] == open {
                    fl += 1
                } else {
                    fl -= 1
                }
                i += 1
            }
            fl
        }
    """.trimIndent()

    val ns = GlobalCompilationNamespace()
    val result = Compiler.compile(code, ns)

    result.messages.forEach { println(it.message) }

//    val pkg = Package("user", "default")
//    val typer = Typer(InferenceContext(pkg, ns))
//    val constraints = mutableListOf<Constraint>()
//    result.program.expressions.forEach {
//        typer.typeTerm(it, 0, constraints)
//    }

//    val variables = mutableListOf<Variable>()
//    val visitor = object : DefaultExpressionVisitor {
//        override fun visit(expr: Expression) {
//            val type = expr.newType
//            if (type is Variable) {
//                variables.add(type)
//            }
//            visitAll(expr.children())
//        }
//    }
//    result.program.expressions.forEach { visitor.visit(it) }

//    println(constraints)

//    val solutions = unify(constraints)
//    result.program.expressions.forEach { replaceTypes(it, solutions) }
    printAst(result.program.expressions)
}
