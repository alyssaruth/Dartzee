package dartzee.theme

import com.github.alyssaburlton.swingtest.shouldMatch
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.logging.Severity
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.preferenceService
import dartzee.utils.ResourceCache
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.io.IOException
import java.io.InputStream
import java.sql.Timestamp
import java.time.LocalDate
import java.time.Month
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon
import org.junit.jupiter.api.Test

class ThemeUtilsTest : AbstractTest() {
    @Test
    fun `theme map should contain all themes except None`() {
        val expected = ThemeId.entries.filterNot { it == ThemeId.None }.toSet()
        themeMap().keys shouldBe expected

        themeMap().forEach { (id, theme) -> theme.id shouldBe id }
    }

    @Test
    fun `themedIcon should return default icon if no theme`() {
        val expected = ImageIcon(javaClass.getResource("/buttons/playerManagement.png"))
        themedIcon("/buttons/playerManagement.png").shouldMatch(expected)
    }

    @Test
    fun `themedIcon should return default icon if theme is locked`() {
        InjectedThings.now = LocalDate.of(2026, Month.JANUARY, 1)
        InjectedThings.theme = Themes.HALLOWEEN
        val expected = ImageIcon(javaClass.getResource("/buttons/playerManagement.png"))
        themedIcon("/buttons/playerManagement.png").shouldMatch(expected)
    }

    @Test
    fun `themedIcon should return overridden icon if theme exists and is unlocked`() {
        InjectedThings.now = LocalDate.of(2027, Month.JANUARY, 1)
        InjectedThings.theme = Themes.HALLOWEEN
        val expected =
            ImageIcon(javaClass.getResource("/theme/halloween/buttons/playerManagement.png"))
        themedIcon("/buttons/playerManagement.png").shouldMatch(expected)
    }

    @Test
    fun `themedIcon should return default icon if theme path not found (no override exists)`() {
        InjectedThings.now = LocalDate.of(2027, Month.JANUARY, 1)
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
    fun `should pick preference theme if set and not in a festival`() {
        preferenceService.save(Preferences.theme, ThemeId.Easter)
        testPickTheme(LocalDate.of(2026, Month.NOVEMBER, 1)) shouldBe Themes.EASTER
    }

    @Test
    fun `should pick festival theme over preference`() {
        preferenceService.save(Preferences.theme, ThemeId.Easter)
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 31)) shouldBe Themes.HALLOWEEN
    }

    @Test
    fun `should pick birthday theme over festival`() {
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 31)) shouldBe Themes.HALLOWEEN
        InjectedThings.birthdayInfo = BirthdayInfo(listOf("Spooky"), emptyList())
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 31))?.id shouldBe ThemeId.Birthday
    }

    @Test
    fun `should pick halloween theme for relevant dates`() {
        (24..31).forEach {
            testPickTheme(LocalDate.of(2026, Month.OCTOBER, it)) shouldBe Themes.HALLOWEEN
        }

        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 23)) shouldBe Themes.DARTZEE
        testPickTheme(LocalDate.of(2026, Month.NOVEMBER, 1)) shouldBe Themes.DARTZEE
        testPickTheme(LocalDate.of(2026, Month.NOVEMBER, 28)) shouldBe Themes.DARTZEE
    }

    @Test
    fun `Should pick easter for the relevant dates`() {
        testPickTheme(LocalDate.of(2026, Month.MARCH, 27)) shouldBe Themes.DARTZEE
        testPickTheme(LocalDate.of(2026, Month.MARCH, 28)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.MARCH, 29)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.MARCH, 30)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.MARCH, 31)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 1)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 2)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 3)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 4)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 5)) shouldBe Themes.EASTER
        testPickTheme(LocalDate.of(2026, Month.APRIL, 6)) shouldBe Themes.DARTZEE
    }

    @Test
    fun `Should pick oktoberfest for the relevant dates`() {
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 18)) shouldBe Themes.DARTZEE
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 19)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 20)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 21)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 22)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 23)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 24)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 25)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 26)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 27)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 28)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 29)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.SEPTEMBER, 30)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 1)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 2)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 3)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 4)) shouldBe Themes.OKTOBERFEST
        testPickTheme(LocalDate.of(2026, Month.OCTOBER, 5)) shouldBe Themes.DARTZEE
    }

    @Test
    fun `Description should be correct for no theme`() {
        val description = themeDescription(ThemeId.None)
        description shouldBe CLASSIC_THEME_DESC
    }

    @Test
    fun `Description should include correct details for birthday theme`() {
        InjectedThings.now = LocalDate.of(2026, 6, 1)
        insertPlayer(name = "Leah", dateOfBirth = Timestamp.valueOf("1993-06-03 00:00:00"))

        val description = themeDescription(ThemeId.Birthday)
        description shouldBe
            "${Themes.BIRTHDAY.description}\n\nNext celebrated on 3 Jun 2026 for Leah!"
    }

    @Test
    fun `Description should be correct if theme is locked`() {
        InjectedThings.now = LocalDate.of(2026, Month.JANUARY, 1)

        val description = themeDescription(ThemeId.Easter)
        description shouldBe "This theme hasn't unlocked yet. Wait and see!"
    }

    @Test
    fun `Description should include when theme will next kick in`() {
        InjectedThings.now = LocalDate.of(2027, Month.JANUARY, 1)

        val description = themeDescription(ThemeId.Easter)
        description shouldBe "${Themes.EASTER.description}\n\nWill next pop up on 20 Mar 2027."
    }

    @Test
    fun `Description should state if theme is currently active and when it will end`() {
        InjectedThings.now = LocalDate.of(2026, Month.MARCH, 29)

        val description = themeDescription(ThemeId.Easter)
        description shouldBe
            "${Themes.EASTER.description}\n\nCurrently active - will end on 5 Apr 2026."
    }

    private fun testPickTheme(now: LocalDate): Theme? {
        InjectedThings.now = now
        return pickTheme()
    }

    @Test
    fun `Should return null clip for non-existent resource`() {
        clipForResource("/foo/bar") shouldBe null
    }

    @Test
    fun `Should return null clip if unable to construct audio input stream`() {
        mockkStatic(AudioSystem::class) {
            every { AudioSystem.getAudioInputStream(any<InputStream>()) } returns null
            clipForResource("/theme/birthday/newGame.wav") shouldBe null
        }
    }

    @Test
    fun `Should return null clip if error instantiating clip`() {
        mockkStatic(AudioSystem::class) {
            val streamMock = mockk<AudioInputStream>(relaxed = true)
            every { streamMock.mark(any()) } throws IOException("oh no!")
            every { AudioSystem.getAudioInputStream(any<InputStream>()) } returns streamMock

            clipForResource("/theme/birthday/newGame.wav") shouldBe null
        }

        verifyLog(CODE_AUDIO_ERROR, Severity.WARN)
    }
}
