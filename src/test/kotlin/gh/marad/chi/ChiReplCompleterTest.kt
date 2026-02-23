package gh.marad.chi

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jline.reader.Candidate
import org.junit.jupiter.api.Test

class ChiReplCompleterTest {

    private val dotCommands = listOf(".exit", ".help", ".toggleLuaCode", ".imports", ".clearImports", ".vi", ".emacs")
    private val completer = ChiReplCompleter(dotCommands)

    private fun complete(word: String): List<String> {
        val candidates = mutableListOf<Candidate>()
        completer.completeCandidates(word, candidates)
        return candidates.map { it.value() }
    }

    // Dot-command completion

    @Test
    fun `completes partial dot-command`() {
        complete(".ex") shouldContainExactly listOf(".exit")
    }

    @Test
    fun `shows all dot-commands on bare dot`() {
        complete(".") shouldContainExactlyInAnyOrder dotCommands
    }

    @Test
    fun `completes unique dot-command prefix`() {
        complete(".tog") shouldContainExactly listOf(".toggleLuaCode")
    }

    @Test
    fun `no match for unknown dot-command`() {
        complete(".xyz").shouldBeEmpty()
    }

    @Test
    fun `completes dot-command with common prefix`() {
        complete(".i") shouldContainExactly listOf(".imports")
    }

    // Keyword completion

    @Test
    fun `completes partial keyword`() {
        complete("whi") shouldContainExactly listOf("while")
    }

    @Test
    fun `completes single matching keyword`() {
        complete("matc") shouldContainExactly listOf("match")
    }

    @Test
    fun `f prefix matches fn, false, for`() {
        complete("f") shouldContainExactlyInAnyOrder listOf("false", "fn", "for")
    }

    @Test
    fun `no match for unknown word`() {
        complete("xyz").shouldBeEmpty()
    }

    @Test
    fun `tr matches trait and true`() {
        complete("tr") shouldContainExactlyInAnyOrder listOf("trait", "true")
    }

    @Test
    fun `co matches continue`() {
        complete("co") shouldContainExactly listOf("continue")
    }

    @Test
    fun `empty word matches all keywords`() {
        complete("").size shouldBe ChiReplCompleter.keywords.size
    }
}
