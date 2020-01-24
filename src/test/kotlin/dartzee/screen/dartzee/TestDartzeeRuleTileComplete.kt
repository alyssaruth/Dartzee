package dartzee.screen.dartzee

import dartzee.helper.AbstractTest
import dartzee.helper.makeDartzeeRuleDto
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color

class TestDartzeeRuleTileComplete: AbstractTest()
{
    @Test
    fun `Should use a disabled button model and set the colours to green for success`()
    {
        val tile = DartzeeRuleTileComplete(makeDartzeeRuleDto(), 2, true, 5)

        tile.model.shouldBeInstanceOf<SoftDisableButtonModel>()
        tile.isFocusable shouldBe false

        tile.background shouldBe Color.GREEN
        tile.getScoreForHover() shouldBe 5
    }

    @Test
    fun `Should set the colours to red for a failed rule`()
    {
        val tile = DartzeeRuleTileComplete(makeDartzeeRuleDto(), 2, false, -15)

        tile.background shouldBe Color.RED
        tile.getScoreForHover() shouldBe -15
    }
}