package dartzee.theme

import dartzee.helper.AbstractTest
import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.Severity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ThemeIdTest : AbstractTest() {
    @Test
    fun `Should parse from a string`() {
        ThemeId.parseFromPreference("Easter") shouldBe ThemeId.Easter
    }

    @Test
    fun `Should log an error and fall back to None for an invalid string`() {
        ThemeId.parseFromPreference("Badger") shouldBe ThemeId.None

        verifyLog(CODE_PARSE_ERROR, Severity.ERROR)
    }
}
