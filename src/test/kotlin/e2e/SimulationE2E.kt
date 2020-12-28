package e2e

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.AimDart
import dartzee.ai.SimulationRunner
import dartzee.awaitCondition
import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.NumberField
import dartzee.core.bean.ScrollTable
import dartzee.game.GameType
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.screen.ai.AISimulationSetupDialog
import dartzee.screen.stats.player.StatisticsTabTotalScore
import dartzee.screen.stats.player.x01.StatisticsTabFinishBreakdown
import dartzee.screen.stats.player.x01.StatisticsTabX01ThreeDartScores
import dartzee.screen.stats.player.x01.StatisticsTabX01TopFinishes
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import javax.swing.JButton

class SimulationE2E: AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        InjectedThings.simulationRunner = SimulationRunner()
    }

    @Test
    @Tag("e2e")
    fun `Should be able to run a simulation of 500 games`()
    {
        val model = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = model)

        val dlg = AISimulationSetupDialog(player, model, true)
        dlg.getChild<NumberField>().value = 500
        dlg.clickChild<JButton>("Ok")

        val statsScrn = awaitStatisticsScreen()
        statsScrn.gameType shouldBe GameType.X01

        val totalScorePanel = statsScrn.getChild<StatisticsTabTotalScore>()
        awaitCondition { totalScorePanel.getChild<NumberField> { it.testId == "Mean" }.value == 9.0 }
        totalScorePanel.getChild<NumberField> { it.testId == "Median" }.value shouldBe 9.0
        val allScoreRows = totalScorePanel.getChild<ScrollTableDartsGame>().getRows()
        allScoreRows.shouldHaveSize(500)
        allScoreRows.forEachIndexed { ix, row ->
            val gameNo = ix + 1
            row[0] shouldBe gameNo
            row[1] shouldBe 9
            row[2] shouldBe -gameNo
        }

        val doublesPanel = statsScrn.getChild<StatisticsTabFinishBreakdown>()
        val tableDoubles = doublesPanel.getChild<ScrollTable> { it.testId == "DoublesMine" }
        tableDoubles.getRows().shouldContainExactly(listOf(listOf(12, 500, 100.0)))

        val topFinishesPanel = statsScrn.getChild<StatisticsTabX01TopFinishes>()
        val tableTopFinishes = topFinishesPanel.getChild<ScrollTable> { it.testId == "TopFinishesMine" }
        val rows = tableTopFinishes.getRows()
        rows.first().shouldContainExactly(141, "T20, T19, D12", -1L)

        val threeDartScoresPanel = statsScrn.getChild<StatisticsTabX01ThreeDartScores>()
        val tableScores = threeDartScoresPanel.getChild<ScrollTable> { it.testId == "PlayerScores" }
        tableScores.rowCount shouldBe 1
        tableScores.getRows().first().shouldContainExactly(180, 1000)
        val tableBreakdown = threeDartScoresPanel.getChild<ScrollTable> { it.testId == "PlayerBreakdown" }
        tableBreakdown.rowCount shouldBe 1
        tableBreakdown.getRows().first().shouldContainExactly("T20, T20, T20", 1000, -1L)
    }
}