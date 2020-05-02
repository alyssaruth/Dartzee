package dartzee.screen.reporting

import dartzee.core.bean.ScrollTable
import dartzee.findComponent
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayerForGame
import dartzee.reporting.ReportParameters
import io.kotlintest.shouldBe
import org.junit.Test

class TestReportingResultsScreen: AbstractTest()
{
    @Test
    fun `Should initialise with results based on the report parameters`()
    {
        val rp = ReportParameters()
        rp.gameType = GameType.X01

        val gX01 = insertGame(gameType = GameType.X01, gameParams = "501")
        insertPlayerForGame("Bob", gX01.rowId)

        val gDartzee = insertGame(gameType = GameType.DARTZEE, gameParams = "")
        insertPlayerForGame("Bob", gDartzee.rowId)

        val resultsScreen = ReportingResultsScreen(ConfigureReportColumnsDialog())
        resultsScreen.rp = rp
        resultsScreen.initialise()

        val table = resultsScreen.findComponent<ScrollTable>()
        table.rowCount shouldBe 1
    }
}