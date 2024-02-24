package gh.marad.chi.core.types

import gh.marad.chi.addSymbol
import gh.marad.chi.compile
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.utils.printAst
import org.junit.jupiter.api.Test

class RealLifeCodeTest {
    @Test
    fun `should find function from different package by type id`() {
        val ns = GlobalCompilationNamespace()
        compile(
            """
                package std/lang.types.array
                pub fn add[T](arr: array[T]) {}
            """.trimIndent(),
            ns
        )

        val result = compile(
            """
                [].add()
            """.trimIndent(),
            ns
        )
        printAst(result.expressions)
    }

    @Test
    fun `should properly type a map function`() {
        val typeVar = Variable("T", 1)
        val arrayType = Type.array(typeVar)
        val arrayTypeId = arrayType.getTypeId()
        val ns = GlobalCompilationNamespace()
        ns.addSymbol(arrayTypeId.moduleName, arrayTypeId.packageName, "size",
            type = PolyType(0, Type.fn(arrayType, Type.int)), public = true)
        ns.addSymbol(arrayTypeId.moduleName, arrayTypeId.packageName, "add",
            type = PolyType(0, Type.fn(arrayType, typeVar, Type.unit)), public = true)

        val result = compile(
            """
                fn map[T, R](arr: array[T], f: (T) -> R): array[R] {
                    val result = []
                    var i = 0
                    while i < arr.size() {
                        val value = f(arr[i])
                        result.add(value)
                        i += 1
                    }
                    result
                }
                
                map([1,2], { a -> a as string })
            """.trimIndent(),
            ns
        )

        printAst(result.expressions)
    }
}