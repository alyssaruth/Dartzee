package dartzee.theme

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import com.github.alyssaburlton.swingtest.shouldMatch
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import java.awt.Color
import java.awt.event.ActionListener
import java.time.LocalDate
import java.time.Month
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.border.TitledBorder
import org.junit.jupiter.api.Test

class ThemeSelectorTest : AbstractTest() {
    private val themeIds = listOf(ThemeId.None, ThemeId.Halloween, ThemeId.Easter)

    @Test
    fun `Should initialise with the first theme in the list`() {
        val selector = ThemeSelector(themeIds)
        selector.getChild<ThemePanel>().themeId shouldBe ThemeId.None
    }

    @Test
    fun `Next and previous buttons should disable appropriately`() {
        val selector = ThemeSelector(themeIds)
        selector.nextButton().shouldBeEnabled()
        selector.previousButton().shouldBeDisabled()

        selector.nextButton().doClick()
        selector.nextButton().shouldBeEnabled()
        selector.previousButton().shouldBeEnabled()

        selector.nextButton().doClick()
        selector.nextButton().shouldBeDisabled()
        selector.previousButton().shouldBeEnabled()

        // Select middle one directly
        selector.selectTheme(themeIds[1])
        selector.nextButton().shouldBeEnabled()
        selector.previousButton().shouldBeEnabled()

        selector.previousButton().doClick()
        selector.nextButton().shouldBeEnabled()
        selector.previousButton().shouldBeDisabled()
    }

    @Test
    fun `Should notify listeners when the selection is changed`() {
        val listener = mockk<ActionListener>(relaxed = true)

        val selector = ThemeSelector(themeIds)
        selector.addActionListener(listener)

        selector.nextButton().doClick()

        verify { listener.actionPerformed(any()) }
    }

    @Test
    fun `Should update theme panel, colours and button icons appropriately`() {
        InjectedThings.now = LocalDate.of(2026, Month.DECEMBER, 1)

        val selector = ThemeSelector(themeIds)

        // Next -> Halloween
        selector.nextButton().doClick()
        selector.getChild<ThemePanel>().theme shouldBe Themes.HALLOWEEN
        selector.background shouldBe Themes.HALLOWEEN.background

        val halloweenArrow =
            ImageIcon(javaClass.getResource("/theme/halloween/buttons/rightArrow.png"))
        selector.nextButton().background shouldBe Themes.HALLOWEEN.primary
        selector.nextButton().icon.shouldMatch(halloweenArrow)

        // Previous -> None
        selector.previousButton().doClick()
        selector.getChild<ThemePanel>().theme shouldBe null
        selector.background shouldBe DEFAULT_BACKGROUND

        val regularArrow = ImageIcon(javaClass.getResource("/buttons/rightArrow.png"))
        selector.nextButton().background shouldBe DEFAULT_BUTTON_COLOUR
        selector.nextButton().icon.shouldMatch(regularArrow)

        selector.selectionIsLocked() shouldBe false
    }

    @Test
    fun `Should not reveal any details about a locked theme`() {
        InjectedThings.now = LocalDate.of(2026, Month.MARCH, 28)

        val selector = ThemeSelector(themeIds)
        selector.nextButton().doClick()
        selector.getChild<ThemePanel>().theme shouldBe Themes.HALLOWEEN

        selector.background shouldBe Color.DARK_GRAY
        (selector.border as TitledBorder).titleColor shouldBe Color.LIGHT_GRAY

        val regularArrow = ImageIcon(javaClass.getResource("/buttons/rightArrow.png"))
        selector.nextButton().background shouldBe DEFAULT_BUTTON_COLOUR
        selector.nextButton().icon.shouldMatch(regularArrow)

        selector.selectionIsLocked() shouldBe true
    }

    private fun ThemeSelector.nextButton() = getChild<JButton>("NextTheme")

    private fun ThemeSelector.previousButton() = getChild<JButton>("PreviousTheme")
}
