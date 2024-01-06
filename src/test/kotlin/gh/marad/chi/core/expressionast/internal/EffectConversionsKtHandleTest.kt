package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Handle
import gh.marad.chi.core.OldType
import gh.marad.chi.core.parser.readers.ParseHandle
import gh.marad.chi.core.parser.readers.ParseHandleCase
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.junit.jupiter.api.Test
import java.lang.AssertionError

class EffectConversionsKtHandleTest {
    @Test
    fun `should create virtual scope for every case`() {
        // given
        val context = defaultContext()
        context.addPublicSymbol(sampleCase.effectName, OldType.fn(returnType = OldType.int, OldType.string))
        val handle = sampleHandle.copy(
            cases = listOf(
                sampleCase.copy(),
                sampleCase.copy()
            )
        )

        // when
        val result = convertHandle(context, handle)
            .shouldBeTypeOf<Handle>()

        // then
        result.cases shouldHaveSize 2
        result.cases should {
            it[0].scope shouldNotBeSameInstanceAs context.currentScope
            it[0].scope shouldNotBeSameInstanceAs it[1].scope
        }
    }

    @Test
    fun `case scope should have 'resume' function defined`() {
        // given
        val context = defaultContext()
        context.addPublicSymbol(sampleCase.effectName, OldType.fn(returnType = OldType.int, OldType.string))
        val parseHandle = sampleHandle.copy(
            cases = listOf(sampleCase)
        )

        // when
        val handle = convertHandle(context, parseHandle)
            .shouldBeTypeOf<Handle>()

        // then
        handle.cases shouldHaveSize 1

        throw AssertionError("This test needs update")
//        with(handle.cases.first().scope) {
//            val resumeInfo = getSymbol("resume").shouldNotBeNull()
//            resumeInfo.scopeType shouldBe ScopeType.Virtual
//            resumeInfo.symbolType shouldBe SymbolType.Local
//            resumeInfo.type shouldBe OldType.fn(returnType = handle.type, OldType.intType)
//        }
    }

    @Test
    fun `effect arguments should be defined within case scope`() {
        // given
        val context = defaultContext()
        context.addPublicSymbol(sampleCase.effectName, OldType.fn(returnType = OldType.int, OldType.string))
        val parseHandle = sampleHandle.copy(
            cases = listOf(
                sampleCase.copy(
                    argumentNames = listOf("argument")
                )
            )
        )

        // when
        val handle = convertHandle(context, parseHandle)
            .shouldBeTypeOf<Handle>()

        // then
        handle.cases shouldHaveSize 1
        with(handle.cases.first().scope) {
            val arg = getSymbol("argument").shouldNotBeNull()
            arg.type shouldBe OldType.string
        }
    }

    private val sampleCase = ParseHandleCase(
        effectName = "myEffect",
        argumentNames = emptyList(),
        body = sampleParseBlock,
        sectionA
    )

    private val sampleHandle = ParseHandle(
        body = sampleParseBlock,
        cases = listOf(sampleCase),
        testSection
    )
}