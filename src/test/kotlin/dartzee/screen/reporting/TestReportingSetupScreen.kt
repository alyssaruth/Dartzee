package dartzee.screen.reporting

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.makeInvalid
import dartzee.screen.ScreenCache
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JCheckBox

class TestReportingSetupScreen: AbstractTest()
{
    @Test
    fun `Should not progress if game tab is invalid`()
    {
        val scrn = ReportingSetupScreen()
        ScreenCache.switch(scrn)

        val gameTab = scrn.getChild<ReportingGameTab>()
        gameTab.clickChild<JCheckBox>("Start Date")
        gameTab.getStartDateFilterPanel().makeInvalid()

        scrn.btnNext.doClick()

        ScreenCache.currentScreen() shouldBe scrn
        dialogFactory.errorsShown.shouldNotBeEmpty()
    }

    @Test
    fun `Should not progress if players tab is invalid`()
    {
        val scrn = ReportingSetupScreen()
        ScreenCache.switch(scrn)

        val playerOne = insertPlayer(name = "Alice")

        val tab = scrn.getChild<ReportingPlayersTab>()
        tab.addPlayers(listOf(playerOne))
        tab.includedPlayerPanel.chckbxPosition.doClick()

        scrn.btnNext.doClick()
        ScreenCache.currentScreen() shouldBe scrn
        dialogFactory.errorsShown.shouldContainExactly("You must select at least one finishing position for player Alice")
    }

    @Test
    fun `Should populate report parameters from both tabs and progress to results screen`()
    {
        val scrn = ReportingSetupScreen()

        val gameTab = scrn.getChild<ReportingGameTab>()
        gameTab.clickChild<JCheckBox>("Game")

        val playersTab = scrn.getChild<ReportingPlayersTab>()
        playersTab.clickChild<JCheckBox>("Exclude games with only AI players")

        scrn.btnNext.doClick()

        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingResultsScreen>()
        val resultsScreen = ScreenCache.currentScreen() as ReportingResultsScreen
        val rp = resultsScreen.rp!!
        rp.gameType shouldBe GameType.X01
        rp.excludeOnlyAi shouldBe true
    }

    private fun ReportingGameTab.getStartDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>().first()
}