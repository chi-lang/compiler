package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.PolyType
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.core.utils.printAst

fun main() {
    val code = """
        import aoc/lib as aoc
        
        type LWH = { l: int, w: int, h: int}

        fn parseLine(line: string): LWH {
            val arr = line.split("x", 0)
            { 
                l: arr[0] as int, 
                w: arr[1] as int, 
                h: arr[2] as int
            }
        }
        
        fn calcPaper(d: LWH): int {
            val surface = 2 * d.l * d.w + 2 * d.w * d.h + 2 * d.h * d.l
            val slack = d.l * d.w
            surface + slack
        }

        fn map[T, R](arr: array[T], f: (T) -> R): array[R] {
            val result = []
            var i = 0
            while i < 0 {
                result.add(f(arr[i]))
                i += 1
            }
            result
        }
    
        fn sum(a: array[int]): int {
            var s = 0
            var i = 0
            while i < 0 {
                s += a[i]
                i += 1
            }
            s
        }


        val input = aoc.load("day02/prod.txt").split("\n", 0).map(parseLine)
        val p1 = input.map(calcPaper).sum()
    """.trimIndent()

    val ns = GlobalCompilationNamespace()
    ns.addSymbol("aoc", "lib", "load", Type.fn(Type.string, Type.string), public = true)
    val stringTypeId = Type.string.getTypeId()
    ns.addSymbol(stringTypeId.moduleName, stringTypeId.packageName, "split",
        type = PolyType(0, Type.fn(Type.string, Type.string, Type.int, Type.array(Type.string))), public = true)
    val arrayTypeId = Type.array(Type.int).getTypeId()
    ns.addSymbol(arrayTypeId.moduleName, arrayTypeId.packageName, "add",
        type = PolyType(0, Type.fn(Type.array(Variable("T", 1)), Variable("T", 1), Type.unit)), public = true)
    val result = Compiler.compile(code, ns)

    printAst(result.program.expressions)

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
}
