package dartzee.screen.game.scorer

import dartzee.dartzee.DartzeeRoundResult
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import java.awt.Color
import org.junit.jupiter.api.Test

class TestDartzeeRoundResultRenderer : AbstractTest() {
    @Test
    fun `Should allow nulls`() {
        val renderer = DartzeeRoundResultRenderer()
        renderer.allowNulls() shouldBe true

        renderer.setCellColours(null, false)
        renderer.foreground shouldBe null
        renderer.background shouldBe null
    }

    @Test
    fun `Should display a hyphen if ruleNumber is not set`() {
        val result = DartzeeRoundResult(-1, true)
        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "-"
    }

    @Test
    fun `Should render the rule number in green for success`() {
        val result = DartzeeRoundResult(5, true)

        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "#5"

        renderer.setCellColours(result, false)
        renderer.background shouldBe Color.GREEN
    }

    @Test
    fun `Should render the rule number in red for failure`() {
        val result = DartzeeRoundResult(7, false)

        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "#7"

        renderer.setCellColours(result, false)
        renderer.background shouldBe Color.RED
    }
}
