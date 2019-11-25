package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.screen.game.scorer.DartzeeRoundResultRenderer
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color

class TestDartzeeRoundResultRenderer: AbstractDartsTest()
{
    @Test
    fun `Should allow nulls`()
    {
        DartzeeRoundResultRenderer().allowNulls() shouldBe true
    }

    @Test
    fun `Should display a question mark if user input is needed`()
    {
        val result = DartzeeRoundResult(-1, false, true)

        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "?"

        renderer.setCellColours(result, false)
        renderer.background shouldBe Color.CYAN
        renderer.foreground shouldBe Color.BLUE
    }

    @Test
    fun `Should display a hyphen if ruleNumber is not set`()
    {
        val result = DartzeeRoundResult(-1, true)
        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "-"
    }

    @Test
    fun `Should render the rule number in green for success`()
    {
        val result = DartzeeRoundResult(5, true)

        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "#5"

        renderer.setCellColours(result, false)
        renderer.background shouldBe Color.GREEN
    }

    @Test
    fun `Should render the rule number in red for failure`()
    {
        val result = DartzeeRoundResult(7, false)

        val renderer = DartzeeRoundResultRenderer()
        renderer.getReplacementValue(result) shouldBe "#7"

        renderer.setCellColours(result, false)
        renderer.background shouldBe Color.RED
    }
}
