package gh.marad.chi.core.types

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.utils.printAst

fun main() {
    val code = """
        type Circle = { radius: int }
        type Square = { sideLength: int }
        type Shape = Circle | Square
        
        fn area(s: Shape): int {
            if s is Circle {
                val c = s as Circle
                c.radius * c.radius * 3
            } else if s is Square {
                val sq = s as Square
                sq.sideLength * sq.sideLength
            } else {
                0
            }
        }
        
        area({ radius: 8 })
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
