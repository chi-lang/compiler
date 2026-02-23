package gh.marad.chi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ReplTest {

    @Test
    fun `historyPath uses CHI_HOME when set`() {
        val path = Repl.historyPath(chiHome = "/tmp/chi")
        path shouldBe Path.of("/tmp/chi", "repl_history")
    }

    @Test
    fun `historyPath falls back to home directory when CHI_HOME is null`() {
        val path = Repl.historyPath(chiHome = null)
        path shouldBe Path.of(System.getProperty("user.home"), ".chi_repl_history")
    }
}
