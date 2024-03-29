package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.helper.AbstractTest
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.stats.overall.SimplifiedLeaderboardScreen
import dartzee.screen.sync.SyncManagementScreen
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import javax.swing.JButton
import javax.swing.JLabel
import org.junit.jupiter.api.Test

class TestMenuScreen : AbstractTest() {
    @Test
    fun `Should go to Sync Management screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Sync Setup")
        ScreenCache.currentScreen().shouldBeInstanceOf<SyncManagementScreen>()
    }

    @Test
    fun `Should go to the game setup screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "New Game")
        ScreenCache.currentScreen().shouldBeInstanceOf<GameSetupScreen>()
    }

    @Test
    fun `Should go to the simplified game setup screen in party mode`() {
        InjectedThings.partyMode = true

        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "New Game")
        ScreenCache.currentScreen().shouldBeInstanceOf<SimplePlayerSelectionScreen>()
    }

    @Test
    fun `Should go to the player management screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Manage Players")
        ScreenCache.currentScreen().shouldBeInstanceOf<PlayerManagementScreen>()
    }

    @Test
    fun `Should go to the reporting setup screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Game Report")
        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingSetupScreen>()
    }

    @Test
    fun `Should go to the leaderboards screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Leaderboards")
        ScreenCache.currentScreen().shouldBeInstanceOf<LeaderboardsScreen>()
    }

    @Test
    fun `Should go to the simplified leaderboards screen in party mode`() {
        InjectedThings.partyMode = true

        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Leaderboards")
        ScreenCache.currentScreen().shouldBeInstanceOf<SimplifiedLeaderboardScreen>()
    }

    @Test
    fun `Should go to the utilities screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Utilities")
        ScreenCache.currentScreen().shouldBeInstanceOf<UtilitiesScreen>()
    }

    @Test
    fun `Should go to the preferences screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Preferences")
        ScreenCache.currentScreen().shouldBeInstanceOf<PreferencesScreen>()
    }

    @Test
    fun `Should go to the dartzee template setup screen`() {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Dartzee Rules")
        ScreenCache.currentScreen().shouldBeInstanceOf<DartzeeTemplateSetupScreen>()
    }

    @Test
    fun `Should launch the ChangeLog if the link is clicked`() {
        val scrn = MenuScreen()

        val lbl = scrn.getChild<JLabel> { it.text.contains("Dartzee v") }
        lbl.doClick()

        val changeLog = findWindow<ChangeLog>()
        changeLog.shouldNotBeNull()
        changeLog.shouldBeVisible()
    }

    @Test
    fun `Should show correct state in party mode`() {
        InjectedThings.partyMode = true

        val scrn = MenuScreen()
        scrn.getChild<JButton>(text = "New Game").shouldBeVisible()
        scrn.getChild<JButton>(text = "Leaderboards").shouldBeVisible()

        scrn.getChild<JButton>(text = "Sync Setup").shouldNotBeVisible()
        scrn.getChild<JButton>(text = "Manage Players").shouldNotBeVisible()
        scrn.getChild<JButton>(text = "Game Report").shouldNotBeVisible()
        scrn.getChild<JButton>(text = "Utilities").shouldNotBeVisible()
        scrn.getChild<JButton>(text = "Preferences").shouldNotBeVisible()
        scrn.getChild<JButton>(text = "Dartzee Rules").shouldNotBeVisible()
    }
}
