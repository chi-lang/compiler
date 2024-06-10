package gh.marad.chi.core.analyzer

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.asts
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FnCallTypeCheckingSpec {
    @Test
    fun `should check that parameter argument types match`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.int)
        ns.addSymbolInDefaultPackage("test", Type.fn(Type.int, Type.fn(Type.unit), Type.int))

        // expect
        messages("test(10, { 5 })", ns).shouldBeEmpty()
        messages("test(10, 20)", ns).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<NotAFunction>()
//            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
//                error.expected shouldBe Type3.fn(Type3.unit)
//                error.actual shouldBe Type3.int
//            }
        }
    }

    @Test
    fun `should check function arity`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("test", Type.fn(Type.int, Type.fn(Type.unit), Type.int))

        // expect
        messages("test(1)", ns).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<FunctionArityError>().should { error ->
                error.expectedCount shouldBe 2
                error.actualCount shouldBe 1
            }
        }
    }

    @Test
    fun `should check that only functions are called`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.int)

        // expect
        messages("x()", ns).should {
            it shouldHaveSize 1
            it[0].shouldBeTypeOf<TypeMismatch>()
        }
    }

    @Test
    fun `should resolve generic return type for complex case`() {
        // given
        val ns = TestCompilationEnv()
        val T = Variable("T", 0)
        val R = Variable("R", 0)
        ns.addSymbolInDefaultPackage("map", Function(
            listOf(Type.array(T), Type.fn(T, R), Type.array(R)),
        ))
        ns.addSymbolInDefaultPackage("operation", Type.fn(Type.int, Type.string))
        ns.addSymbolInDefaultPackage("arr", Type.array(Type.int))

        // when
        val result = ast("map(arr, operation)", ns)

        // then
        result.type shouldBe Array(Type.string)
    }

    // This test is probably not necessary - along with providing types explicitly at all
    // TODO: check that this is really redundant and remove the ability from the parser
//    @Test
//    fun `should check explicitly specified call type params`() {
//        // given
//        val ns = GlobalCompilationNamespace()
//        val T = TypeVariable("T")
//        val R = TypeVariable("R")
//        ns.addSymbolInDefaultPackage("map", FunctionType(
//            listOf(Type3.array(T), Type3.fn(T, R), Type3.array(R)),
//            listOf(T, R)
//        ))
//        ns.addSymbolInDefaultPackage("operation", Type3.fn(Type3.int, Type3.unit))
//        ns.addSymbolInDefaultPackage("arr", Type3.array(Type3.int))
//
//        // when
//        val messages =
//            analyze(ast("map[int, string](arr, operation)", ns, ignoreCompilationErrors = true))
//
//        // then
//        messages shouldHaveSize 1
//        messages[0].shouldBeTypeOf<TypeInferenceFailed>()
//    }

//    @Test
//    fun `should check explicitly specified type parameter when it's only used as return value`() {
//        val ns = GlobalCompilationNamespaceImpl()
//        val T = TypeVariable("T")
//        ns.addSymbolInDefaultPackage("unsafeArray", FunctionType(
//            listOf(Type3.int, Type3.array(T)),
//            typeSchemeVariables = listOf(T)
//        ))
//
//        val result = ast("unsafeArray[int](10)", ns)
//
//        result.newType shouldBe Type3.array(Type3.int)
//    }

    @Test
    fun `constructing recurring data type should work`() {
        asts(
            """
                type List = { head: int, tail: List } | int
                
                fn foo(bar: List) {}
                foo({ head: 5, tail: { head: 6, tail: 7 } })
                foo({ head: 7, tail: 0 })
            """.trimIndent()
        )
    }

    @Test
    fun `constructing recurring type with type parameters should work`() {
         asts(
            """
                type List[T] = { head: T, tail: List[T] } | T
                
                fn foo(bar: List) {}
                foo({ head: 5, tail: { head: 6, tail: 7 }})
                foo({ head: "hello", tail: "string" })
            """.trimIndent()
        )
    }

    @Test
    fun `count test`() {
        asts(
            """
                type List[T] = { head: T, tail: List[T] } | T
                
                fn count[T](list: List[T]): int {
                   var c = 0
                   var current: List[T] = list
                   while true {
                      if current is T {
                         c = c + 1
                         break
                      } else {
                         c = c + 1
                         val tmp = current as { tail: List[T] }
                         current = tmp.tail
                         break
                      }
                   }
                   c
                }
            """.trimIndent()
        )
    }

    @Test
    fun `should accept function returning non-unit value when unit-returning function is expected as an argument`() {
        val code = """
            fn forEach(f: (string) -> unit) { }    
            forEach({ it: string -> it })
        """.trimIndent()

        ast(code)
    }
}
