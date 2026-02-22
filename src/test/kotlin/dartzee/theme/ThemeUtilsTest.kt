package dartzee.theme

import com.github.alyssaburlton.swingtest.shouldMatch
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.Month
import javax.swing.ImageIcon
import org.junit.jupiter.api.Test

class ThemeUtilsTest : AbstractTest() {
    @Test
    fun `themedIcon should return default icon if no theme`() {
        val expected = ImageIcon(javaClass.getResource("/buttons/playerManagement.png"))
        themedIcon("/buttons/playerManagement.png").shouldMatch(expected)
    }

    @Test
    fun `themedIcon should return overridden icon if theme exists`() {
        InjectedThings.theme = Themes.HALLOWEEN
        val expected =
            ImageIcon(javaClass.getResource("/theme/halloween/buttons/playerManagement.png"))
        themedIcon("/buttons/playerManagement.png").shouldMatch(expected)
    }

    @Test
    fun `themedIcon should return default icon if theme path not found (no override exists)`() {
        InjectedThings.theme = Themes.HALLOWEEN
        val expected = ImageIcon(javaClass.getResource("/buttons/calculator.png"))
        themedIcon("/buttons/calculator.png").shouldMatch(expected)
    }

    @Test
    fun `getBaseFont should take theme into account`() {
        getBaseFont() shouldBe ResourceCache.BASE_FONT

        InjectedThings.theme = Themes.HALLOWEEN

        getBaseFont() shouldBe Themes.HALLOWEEN.font
    }

    @Test
    fun `should pick halloween theme for relevant dates`() {
        (24..31).forEach {
            pickThemeForDate(LocalDate.of(2026, Month.OCTOBER, it)) shouldBe Themes.HALLOWEEN
        }

        pickThemeForDate(LocalDate.of(2026, Month.OCTOBER, 23)) shouldBe null
        pickThemeForDate(LocalDate.of(2026, Month.NOVEMBER, 1)) shouldBe null
        pickThemeForDate(LocalDate.of(2026, Month.NOVEMBER, 28)) shouldBe null
    }
}
