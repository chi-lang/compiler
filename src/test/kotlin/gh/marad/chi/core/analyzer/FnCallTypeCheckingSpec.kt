package gh.marad.chi.core.analyzer

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Array
import gh.marad.chi.core.types3.Function
import gh.marad.chi.core.types3.Type3
import gh.marad.chi.core.types3.Variable
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FnCallTypeCheckingSpec {
    @Test
    fun `should check that parameter argument types match`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("x", Type3.int)
        ns.addSymbolInDefaultPackage("test", Type3.fn(Type3.int, Type3.fn(Type3.unit), Type3.int))

        // expect
        messages("test(10, {})", ns).shouldBeEmpty()
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
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("test", Type3.fn(Type3.int, Type3.fn(Type3.unit), Type3.int))

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
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("x", Type3.int)

        // expect
        messages("x()", ns).should {
            it shouldHaveSize 1
            it[0].shouldBeTypeOf<TypeMismatch>()
        }
    }

    @Test
    fun `should resolve generic return type for complex case`() {
        // given
        val ns = GlobalCompilationNamespace()
        val T = Variable("T", 0)
        val R = Variable("R", 0)
        ns.addSymbolInDefaultPackage("map", Function(
            listOf(Type3.array(T), Type3.fn(T, R), Type3.array(R)),
        ))
        ns.addSymbolInDefaultPackage("operation", Type3.fn(Type3.int, Type3.string))
        ns.addSymbolInDefaultPackage("arr", Type3.array(Type3.int))

        // when
        val result = ast("map(arr, operation)", ns)

        // then
        result.newType shouldBe Array(Type3.string)
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
//        val ns = GlobalCompilationNamespace()
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

    //@Test FIXME
    fun `constructing recurring generic data type should work`() {
        ast(
            """
                type List[T] = { head: T, tail: List[T] | int }
            """.trimIndent()
        )
    }

    // @Test
    fun `should compare type parameters in product types`() {
        val ex = messages("""
            data List[T] = Node(head: T, tail: List[T]) | Nil
            Node(5, Nil) == Node("hello", Nil)
        """.trimIndent())

        ex shouldHaveSize 1
        ex.first().shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Type3.int
            it.actual shouldBe Type3.string
        }
    }


//    @Test
    fun `typechecking should work for generic parameter types in type constructors`() {
        val result = messages("""
            data List[T] = Node(head: T, tail: List[T]) | Nil
            Node(10, Node("string", Nil))
        """.trimIndent())

        result.shouldNotBeEmpty()
        result[0].shouldBeTypeOf<TypeMismatch>() should {
            it.expected shouldBe Type3.string
            it.actual shouldBe Type3.int
        }
    }

    @Test
    fun `should accept function returning non-unit value when unit-returning function is expected as an argument`() {
        val code = """
            fn forEach(f: (string) -> unit) { }    
            forEach({ it: string -> it })
        """.trimIndent()

        val result = ast(code)
    }
}