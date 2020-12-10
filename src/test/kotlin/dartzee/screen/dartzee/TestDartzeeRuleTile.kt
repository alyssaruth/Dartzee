package dartzee.screen.dartzee

import dartzee.core.helper.makeMouseEvent
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.total.DartzeeTotalRuleLessThan
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeTotalScoreRule
import dartzee.screen.game.dartzee.DartzeeRuleTile
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeRuleTile: AbstractTest()
{
    @Test
    fun `Should initialise with the rule number and description`()
    {
        val dto = makeDartzeeRuleDto()

        val tile = FakeDartzeeRuleTile(dto, 2)
        tile.text shouldBe "<html><center><b>#2 <br /><br /> Anything</b></center></html>"
    }

    @Test
    fun `Should escape special characters in the description`()
    {
        val dto = makeDartzeeRuleDto(totalRule = makeTotalScoreRule<DartzeeTotalRuleLessThan>(20))

        val tile = FakeDartzeeRuleTile(dto, 3)

        tile.text shouldBe "<html><center><b>#3 <br /><br /> Total &lt; 20</b></center></html>"
    }

    @Test
    fun `Should do nothing on hover if no score is set`()
    {
        val dto = makeDartzeeRuleDto()

        val tile = FakeDartzeeRuleTile(dto, 2)
        tile.mouseEntered(makeMouseEvent())

        tile.font.size shouldBe 12
        tile.text shouldBe "<html><center><b>#2 <br /><br /> Anything</b></center></html>"
    }

    @Test
    fun `Should change the button text to the score on hover, and revert it on mouse exited`()
    {
        val dto = makeDartzeeRuleDto()

        val tile = FakeDartzeeRuleTile(dto, 2)
        tile.score = 53
        tile.mouseEntered(makeMouseEvent())

        tile.font.size shouldBe 24
        tile.text shouldBe "<html><center><b>+ 53</b></center></html>"

        tile.mouseExited(makeMouseEvent())
        tile.font.size shouldBe 12
        tile.text shouldBe "<html><center><b>#2 <br /><br /> Anything</b></center></html>"
    }

    @Test
    fun `Should set the button text on hover correctly for negative scores`()
    {
        val dto = makeDartzeeRuleDto()
        val tile = FakeDartzeeRuleTile(dto, 2)
        tile.score = -10
        tile.mouseEntered(makeMouseEvent())

        tile.text shouldBe "<html><center><b>- 10</b></center></html>"
    }

    private class FakeDartzeeRuleTile(dto: DartzeeRuleDto, ruleNumber: Int): DartzeeRuleTile(dto, ruleNumber)
    {
        var score: Int? = null

        override fun getScoreForHover() = score
    }
}