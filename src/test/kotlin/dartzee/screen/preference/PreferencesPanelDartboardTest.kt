package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.toBufferedImage
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.bean.PresentationDartboard
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.helper.makeTheme
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.preferences.Preferences
import dartzee.theme.ThemeId
import dartzee.utils.DartsColour
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JTextPane
import org.junit.jupiter.api.Test

class PreferencesPanelDartboardTest : AbstractPreferencePanelTest<PreferencesPanelDartboard>() {
    @Test
    fun `Dartboard should refresh when settings are changed`() {
        val frame = JFrame()
        val panel = PreferencesPanelDartboard()

        runOnEventThreadBlocking {
            frame.size = Dimension(800, 600)
            frame.layout = BorderLayout(0, 0)

            panel.refresh(true)
            frame.add(panel, BorderLayout.CENTER)
            frame.isVisible = true
            frame.repaint()
        }

        verifyDartboardCenterColour(panel, Color.RED)

        setUiFieldValuesToNonDefaults(panel)

        verifyDartboardCenterColour(panel, Color.MAGENTA)
    }

    @Test
    fun `Should not show warning when there's no current theme`() {
        val panel = PreferencesPanelDartboard()
        panel.themeWarning() shouldBe null
    }

    @Test
    fun `Should not show warning when current theme doesn't override the dartboard`() {
        InjectedThings.theme = makeTheme(dartboardColours = null)
        val panel = PreferencesPanelDartboard()
        panel.themeWarning() shouldBe null
    }

    @Test
    fun `Should show warning when current theme overrides the dartboard`() {
        InjectedThings.theme =
            makeTheme(id = ThemeId.Easter, dartboardColours = DEFAULT_COLOUR_WRAPPER)
        val panel = PreferencesPanelDartboard()
        val themeWarning = panel.themeWarning()
        themeWarning.shouldNotBeNull()
        themeWarning.text shouldContain "Easter theme is currently applied."
    }

    private fun PreferencesPanelDartboard.themeWarning() = findChild<JTextPane>("ThemeWarning")

    private fun verifyDartboardCenterColour(panel: PreferencesPanelDartboard, color: Color) {
        waitForAssertion {
            val dartboard = panel.getChild<PresentationDartboard>()
            dartboard.isShowing shouldBe true
            dartboard.width shouldBeGreaterThan 0

            val center = dartboard.computeCenter()
            val oldRgb = dartboard.toBufferedImage().getRGB(center.x, center.y)
            oldRgb shouldBe color.rgb
        }
    }

    override fun factory() = PreferencesPanelDartboard()

    override fun checkUiFieldValuesAreDefaults(panel: PreferencesPanelDartboard) {
        panel.cpOddSingle.selectedColour shouldBe DartsColour.DARTBOARD_WHITE
        panel.cpOddDouble.selectedColour shouldBe DartsColour.DARTBOARD_GREEN
        panel.cpOddTreble.selectedColour shouldBe DartsColour.DARTBOARD_GREEN

        panel.cpEvenSingle.selectedColour shouldBe DartsColour.DARTBOARD_BLACK
        panel.cpEvenDouble.selectedColour shouldBe DartsColour.DARTBOARD_RED
        panel.cpEvenTreble.selectedColour shouldBe DartsColour.DARTBOARD_RED
    }

    override fun setUiFieldValuesToNonDefaults(panel: PreferencesPanelDartboard) {
        panel.cpOddSingle.updateSelectedColor(Color.BLUE)
        panel.cpOddDouble.updateSelectedColor(Color(200, 50, 128))
        panel.cpOddTreble.updateSelectedColor(Color.getHSBColor(0.9f, 0.8f, 1.0f))

        panel.cpEvenSingle.updateSelectedColor(Color.YELLOW)
        panel.cpEvenDouble.updateSelectedColor(Color.MAGENTA)
        panel.cpEvenTreble.updateSelectedColor(Color.CYAN)
    }

    override fun checkUiFieldValuesAreNonDefaults(panel: PreferencesPanelDartboard) {
        panel.cpOddSingle.selectedColour shouldBe Color.BLUE
        panel.cpOddDouble.selectedColour shouldBe Color(200, 50, 128)
        panel.cpOddTreble.selectedColour shouldBe Color.getHSBColor(0.9f, 0.8f, 1.0f)

        panel.cpEvenSingle.selectedColour shouldBe Color.YELLOW
        panel.cpEvenDouble.selectedColour shouldBe Color.MAGENTA
        panel.cpEvenTreble.selectedColour shouldBe Color.CYAN
    }

    override fun checkPreferencesAreSetToNonDefaults() {
        preferenceService.get(Preferences.evenSingleColour) shouldBe Color.YELLOW
        preferenceService.get(Preferences.evenDoubleColour) shouldBe Color.MAGENTA
        preferenceService.get(Preferences.evenTrebleColour) shouldBe Color.CYAN
        preferenceService.get(Preferences.oddSingleColour) shouldBe Color.BLUE
        preferenceService.get(Preferences.oddDoubleColour) shouldBe Color(200, 50, 128)
        preferenceService.get(Preferences.oddTrebleColour) shouldBe
            Color.getHSBColor(0.9f, 0.8f, 1.0f)
    }
}
