package dartzee.screen

import com.github.alexburlton.swingtest.clickChild
import dartzee.helper.AbstractTest
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.sync.SyncManagementScreen
import dartzee.screen.sync.SyncSummaryPanel
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import javax.swing.JButton

class TestMenuScreen: AbstractTest()
{
    @Test
    fun `Should go to Sync Management screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<SyncSummaryPanel>()
        ScreenCache.currentScreen().shouldBeInstanceOf<SyncManagementScreen>()
    }

    @Test
    fun `Should go to the game setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("New Game")
        ScreenCache.currentScreen().shouldBeInstanceOf<GameSetupScreen>()
    }

    @Test
    fun `Should go to the player management screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Manage Players")
        ScreenCache.currentScreen().shouldBeInstanceOf<PlayerManagementScreen>()
    }

    @Test
    fun `Should go to the reporting setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Game Report")
        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingSetupScreen>()
    }

    @Test
    fun `Should go to the leaderboards screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Leaderboards")
        ScreenCache.currentScreen().shouldBeInstanceOf<LeaderboardsScreen>()
    }

    @Test
    fun `Should go to the utilities screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Utilities")
        ScreenCache.currentScreen().shouldBeInstanceOf<UtilitiesScreen>()
    }

    @Test
    fun `Should go to the preferences screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Preferences")
        ScreenCache.currentScreen().shouldBeInstanceOf<PreferencesScreen>()
    }

    @Test
    fun `Should go to the dartzee template setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>("Dartzee")
        ScreenCache.currentScreen().shouldBeInstanceOf<DartzeeTemplateSetupScreen>()
    }
}