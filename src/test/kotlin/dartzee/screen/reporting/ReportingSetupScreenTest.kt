package dartzee.screen.reporting

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.expectErrorDialog
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.makeInvalid
import dartzee.screen.ScreenCache
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import javax.swing.JButton
import javax.swing.JCheckBox
import org.junit.jupiter.api.Test

class ReportingSetupScreenTest : AbstractTest() {
    @Test
    fun `Should not progress if game tab is invalid`() {
        val scrn = ReportingSetupScreen()
        ScreenCache.switch(scrn)

        val gameTab = scrn.getChild<ReportingGameTab>()
        gameTab.clickChild<JCheckBox>(text = "Start Date")
        gameTab.getStartDateFilterPanel().makeInvalid()

        scrn.clickChild<JButton>("Next", async = true)
        expectErrorDialog("The 'date from' cannot be after the 'date to'")

        ScreenCache.currentScreen() shouldBe scrn
    }

    @Test
    fun `Should not progress if players tab is invalid`() {
        val scrn = ReportingSetupScreen()
        ScreenCache.switch(scrn)

        val playerOne = insertPlayer(name = "Alice")

        val tab = scrn.getChild<ReportingPlayersTab>()
        tab.addPlayers(listOf(playerOne))
        tab.includedPlayerPanel.chckbxPosition.doClick()

        scrn.clickChild<JButton>("Next", async = true)
        expectErrorDialog("You must select at least one finishing position for player Alice")
        ScreenCache.currentScreen() shouldBe scrn
    }

    @Test
    fun `Should populate report parameters from both tabs and progress to results screen`() {
        val scrn = ReportingSetupScreen()

        val gameTab = scrn.getChild<ReportingGameTab>()
        gameTab.clickChild<JCheckBox>(text = "Game")

        val playersTab = scrn.getChild<ReportingPlayersTab>()
        playersTab.clickChild<JCheckBox>(text = "Exclude games with only AI players")

        scrn.clickChild<JButton>("Next")

        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingResultsScreen>()
        val resultsScreen = ScreenCache.currentScreen() as ReportingResultsScreen
        val rp = resultsScreen.rp!!
        rp.game.gameType shouldBe GameType.X01
        rp.players.excludeOnlyAi shouldBe true
    }

    private fun ReportingGameTab.getStartDateFilterPanel() =
        getAllChildComponentsForType<DateFilterPanel>().first()
}
