package e2e

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.AimDart
import dartzee.awaitCondition
import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.NumberField
import dartzee.game.GameType
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.screen.ai.AISimulationSetupDialog
import dartzee.screen.stats.player.PlayerStatisticsScreen
import dartzee.screen.stats.player.StatisticsTabTotalScore
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JButton

class SimulationE2E: AbstractTest()
{
    @Test
    fun `Should be able to run a simulation of 500 games`()
    {
        val model = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = model)

        val dlg = AISimulationSetupDialog(player, model, true)
        dlg.getChild<NumberField>().value = 500
        dlg.clickChild<JButton>("Ok")

        awaitCondition { getWindow { it.findChild<PlayerStatisticsScreen>() != null }?.isVisible ?: false }

        val statsScrn = getWindow { it.findChild<PlayerStatisticsScreen>() != null }!!.getChild<PlayerStatisticsScreen>()
        statsScrn.gameType shouldBe GameType.X01

        val totalScorePanel = statsScrn.getChild<StatisticsTabTotalScore>()
        totalScorePanel.getChild<NumberField> { it.testId == "Mean" }.value shouldBe 9.0
        totalScorePanel.getChild<NumberField> { it.testId == "Median" }.value shouldBe 9.0
        val allScoreRows = totalScorePanel.getChild<ScrollTableDartsGame>().getRows()
        allScoreRows.shouldHaveSize(500)
        allScoreRows.forEachIndexed { ix, row ->
            row[0] shouldBe ix + 1
            row[1] shouldBe 9
            row[2] shouldBe -(ix + 1)
        }


    }
}