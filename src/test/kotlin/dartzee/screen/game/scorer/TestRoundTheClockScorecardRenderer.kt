package dartzee.screen.game.scorer

import dartzee.helper.AbstractTest
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.awt.Color
import java.awt.Font
import javax.swing.SwingConstants
import javax.swing.border.LineBorder
import org.junit.jupiter.api.Test

class TestRoundTheClockScorecardRenderer : AbstractTest() {
    @Test
    fun `Should have right font and alignment`() {
        val renderer = RoundTheClockScorecardRenderer()
        renderer.setFontsAndAlignment()
        renderer.font.size shouldBe 15
        renderer.font.style shouldBe Font.BOLD
        renderer.horizontalAlignment shouldBe SwingConstants.CENTER
    }

    @Test
    fun `Should set neutral colours for an unhit target`() {
        val renderer = RoundTheClockScorecardRenderer()
        renderer.setCellColours(makeClockResult(1, false), false)
        renderer.foreground shouldBe Color.BLACK
        renderer.background shouldBe null
    }

    @Test
    fun `Should set green colours for a hit`() {
        val renderer = RoundTheClockScorecardRenderer()
        renderer.setCellColours(makeClockResult(1, true), false)
        renderer.foreground shouldBe Color.getHSBColor(0.3f, 1f, 0.5f)
        renderer.background shouldBe Color.getHSBColor(0.3f, 1f, 1f)
    }

    @Test
    fun `Should adhere to brightness preferences`() {
        InjectedThings.preferenceService.save(Preferences.fgBrightness, 0.8)
        InjectedThings.preferenceService.save(Preferences.bgBrightness, 0.1)

        val renderer = RoundTheClockScorecardRenderer()
        renderer.setCellColours(makeClockResult(1, true), false)
        renderer.foreground shouldBe Color.getHSBColor(0.3f, 1f, 0.8f)
        renderer.background shouldBe Color.getHSBColor(0.3f, 1f, 0.1f)
    }

    @Test
    fun `Should add a border if currentTarget, and not otherwise`() {
        val currentTarget = makeClockResult(isCurrentTarget = true)
        val nonTarget = makeClockResult(isCurrentTarget = false)

        val renderer = RoundTheClockScorecardRenderer()
        renderer.setCellColours(currentTarget, false)
        renderer.border.shouldBeInstanceOf<LineBorder>()

        renderer.setCellColours(nonTarget, false)
        renderer.border.shouldBeNull()
    }

    @Test
    fun `Should use the number as its rendered value`() {
        val renderer = RoundTheClockScorecardRenderer()
        val hitOne = makeClockResult(1, hit = true)
        val missTwo = makeClockResult(2, hit = false)

        renderer.getReplacementValue(hitOne) shouldBe "1"
        renderer.getReplacementValue(missTwo) shouldBe "2"
    }
}
