package burlton.dartzee.test.screen.game

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSummaryPanel
import burlton.dartzee.code.screen.game.GamePanelDartzee
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.makeDartzeeRuleDto
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test

class TestGamePanelDartzee: AbstractDartsTest()
{
    @Test
    fun `Should initialise totalRounds based on the number of rules`()
    {
        makeGamePanel(listOf(makeDartzeeRuleDto())).totalRounds shouldBe 2
        makeGamePanel(listOf(makeDartzeeRuleDto(), makeDartzeeRuleDto())).totalRounds shouldBe 3
    }

    @Test
    fun `Should register itself as a listener on the carousel`()
    {
        val dtos = listOf(makeDartzeeRuleDto())

        val carousel = DartzeeRuleCarousel(dtos)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(dtos, summaryPanel)
        carousel.listener shouldBe gamePanel
    }

    private fun makeGamePanel(dtos: List<DartzeeRuleDto>, summaryPanel: DartzeeRuleSummaryPanel = mockk(relaxed = true)): GamePanelDartzee
    {
        val game = insertGame()
        return GamePanelDartzee(mockk(relaxed = true), game, dtos, summaryPanel)
    }
}