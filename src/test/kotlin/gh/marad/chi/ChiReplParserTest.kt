package gh.marad.chi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ChiReplParserTest {

    @Test
    fun `balanced braces have zero depth`() {
        ChiReplParser.computeBracketDepth("fn add(a: int, b: int) { a + b }") shouldBe 0
    }

    @Test
    fun `unclosed brace has positive depth`() {
        ChiReplParser.computeBracketDepth("fn add(a: int, b: int) {") shouldBe 1
    }

    @Test
    fun `unclosed parenthesis has positive depth`() {
        ChiReplParser.computeBracketDepth("fn add(") shouldBe 1
    }

    @Test
    fun `unclosed square bracket has positive depth`() {
        ChiReplParser.computeBracketDepth("val arr = [1, 2,") shouldBe 1
    }

    @Test
    fun `nested unclosed delimiters accumulate depth`() {
        ChiReplParser.computeBracketDepth("fn foo() { val arr = [1,") shouldBe 2
    }

    @Test
    fun `fully balanced nested delimiters have zero depth`() {
        ChiReplParser.computeBracketDepth("fn foo() { val arr = [1, 2] }") shouldBe 0
    }

    @Test
    fun `negative depth on extra closing brace`() {
        ChiReplParser.computeBracketDepth("}") shouldBe -1
    }

    @Test
    fun `negative depth on extra closing paren`() {
        ChiReplParser.computeBracketDepth(")") shouldBe -1
    }

    @Test
    fun `empty input has zero depth`() {
        ChiReplParser.computeBracketDepth("") shouldBe 0
    }

    @Test
    fun `simple expression without brackets has zero depth`() {
        ChiReplParser.computeBracketDepth("val x = 42") shouldBe 0
    }

    // String literal tests

    @Test
    fun `braces inside string literal are ignored`() {
        ChiReplParser.computeBracketDepth("""val s = "{ hello }"""") shouldBe 0
    }

    @Test
    fun `parentheses inside string literal are ignored`() {
        ChiReplParser.computeBracketDepth("""val s = "( hello )"""") shouldBe 0
    }

    @Test
    fun `brackets inside string literal are ignored`() {
        ChiReplParser.computeBracketDepth("""val s = "[ hello ]"""") shouldBe 0
    }

    @Test
    fun `escaped quote does not end string state`() {
        // val s = "escaped \" quote { still in string }"
        ChiReplParser.computeBracketDepth("""val s = "escaped \" quote { still in string }"""") shouldBe 0
    }

    @Test
    fun `escaped backslash before quote ends string normally`() {
        // val s = "backslash \\"  { outside
        // The \\\\ is two escaped backslashes, so the " after them closes the string
        // Then { is outside the string
        ChiReplParser.computeBracketDepth("""val s = "backslash \\" """) shouldBe 0
    }

    // String interpolation tests

    @Test
    fun `balanced interpolation in string has zero depth`() {
        ChiReplParser.computeBracketDepth("""val s = "value: ${"$"}{x + 1}"""") shouldBe 0
    }

    @Test
    fun `unclosed interpolation has positive depth`() {
        ChiReplParser.computeBracketDepth("""val s = "value: ${"$"}{""") shouldBe 1
    }

    @Test
    fun `interpolation with nested braces`() {
        // "result: ${if (x) { 1 } else { 2 }}"
        ChiReplParser.computeBracketDepth("""val s = "result: ${"$"}{if (x) { 1 } else { 2 }}"""") shouldBe 0
    }

    @Test
    fun `multiple interpolations in one string`() {
        ChiReplParser.computeBracketDepth("""val s = "${"$"}{a} and ${"$"}{b}"""") shouldBe 0
    }

    @Test
    fun `unclosed brace inside interpolation`() {
        // "value: ${ fn() {" — interpolation opened, fn brace opened, neither closed
        ChiReplParser.computeBracketDepth("""val s = "value: ${"$"}{ fn() {""") shouldBe 2
    }

    // Multi-line accumulated input

    @Test
    fun `multi-line input with continuation has correct depth`() {
        val input = "fn add(a: int, b: int) {\n  a + b\n}"
        ChiReplParser.computeBracketDepth(input) shouldBe 0
    }

    @Test
    fun `multi-line input still incomplete`() {
        val input = "fn add(a: int, b: int) {\n  a + b"
        ChiReplParser.computeBracketDepth(input) shouldBe 1
    }
}
