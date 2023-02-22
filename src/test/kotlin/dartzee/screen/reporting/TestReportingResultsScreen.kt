package dartzee.screen.reporting

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.game.GameType
import dartzee.getColumnNames
import dartzee.getDisplayValueAt
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayerForGame
import dartzee.logging.CODE_SQL
import dartzee.reporting.ReportParameters
import dartzee.screen.ScreenCache
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import javax.swing.JButton

class TestReportingResultsScreen: AbstractTest()
{
    @Test
    fun `Should initialise with results based on the report parameters`()
    {
        val rp = ReportParameters()
        rp.gameType = GameType.X01

        val gX01 = insertGame(localId = 1, gameType = GameType.X01, gameParams = "501")
        insertPlayerForGame("Bob", gX01.rowId)

        val gDartzee = insertGame(localId = 2, gameType = GameType.DARTZEE, gameParams = "")
        insertPlayerForGame("Bob", gDartzee.rowId)

        val resultsScreen = ReportingResultsScreen()
        resultsScreen.rp = rp
        resultsScreen.initialise()

        val table = resultsScreen.getChild<ScrollTable>()
        table.rowCount shouldBe 1
        table.getValueAt(0, 0) shouldBe 1
    }

    @Test
    fun `Should adjust columns based on the configure dialog, without rerunning SQL`()
    {
        val gX01 = insertGame(localId = 1, gameType = GameType.X01, gameParams = "501")
        insertPlayerForGame("Bob", gX01.rowId)

        val dlg = mockk<ConfigureReportColumnsDialog>(relaxed = true)
        val scrn = ReportingResultsScreen(dlg)

        every { dlg.excludedColumns() } returns emptyList()
        scrn.rp = ReportParameters()
        scrn.initialise()

        val table = scrn.getChild<ScrollTable>()
        table.rowCount shouldBe 1
        table.getColumnNames() shouldBe listOf("Game", "Type", "Players", "Start Date", "Finish Date", "Match")

        clearLogs()
        every { dlg.excludedColumns() } returns listOf("Players", "Finish Date")

        scrn.clickChild<JButton>(text="Configure Columns...")
        verifyNoLogs(CODE_SQL)
        table.rowCount shouldBe 1
        table.getColumnNames() shouldBe listOf("Game", "Type", "Start Date", "Match")
    }

    @Test
    fun `Should sort by timestamps correctly`()
    {
        val g1 = insertGame(dtCreation = Timestamp.valueOf("2020-03-04 15:00:00"))
        val g2 = insertGame(dtCreation = Timestamp.valueOf("2020-01-01 00:00:00"))
        val g3 = insertGame(dtCreation = Timestamp.valueOf("2020-03-03 12:00:00"))
        val g4 = insertGame(dtCreation = Timestamp.valueOf("2020-03-04 05:00:00"))

        insertPlayerForGame("Bob", g1.rowId)
        insertPlayerForGame("Bob", g2.rowId)
        insertPlayerForGame("Bob", g3.rowId)
        insertPlayerForGame("Bob", g4.rowId)

        val scrn = ReportingResultsScreen()
        scrn.rp = ReportParameters()
        scrn.initialise()

        val table = scrn.getChild<ScrollTable>()
        table.sortBy(3, false)

        table.getDisplayValueAt(0, 0) shouldBe 2
        table.getDisplayValueAt(1, 0) shouldBe 3
        table.getDisplayValueAt(2, 0) shouldBe 4
        table.getDisplayValueAt(3, 0) shouldBe 1
    }

    @Test
    fun `Should go back to setup screen`()
    {
        val scrn = ReportingResultsScreen()
        scrn.btnBack.doClick()

        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingSetupScreen>()
    }

}