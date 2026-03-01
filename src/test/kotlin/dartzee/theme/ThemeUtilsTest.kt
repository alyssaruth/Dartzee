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

    @Test
    fun `Should pick easter for the relevant dates`() {
        pickThemeForDate(LocalDate.of(2026, Month.MARCH, 27)) shouldBe null
        pickThemeForDate(LocalDate.of(2026, Month.MARCH, 28)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.MARCH, 29)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.MARCH, 30)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.MARCH, 31)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 1)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 2)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 3)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 4)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 5)) shouldBe Themes.EASTER
        pickThemeForDate(LocalDate.of(2026, Month.APRIL, 6)) shouldBe null
    }

    @Test
    fun `Should find the right date for easter sunday`() {
        findEasterSunday(2027) shouldBe LocalDate.of(2027, Month.MARCH, 28)
        findEasterSunday(2026) shouldBe LocalDate.of(2026, Month.APRIL, 5)
        findEasterSunday(2025) shouldBe LocalDate.of(2025, Month.APRIL, 20)
        findEasterSunday(2024) shouldBe LocalDate.of(2024, Month.MARCH, 31)
        findEasterSunday(2023) shouldBe LocalDate.of(2023, Month.APRIL, 9)
    }
}
