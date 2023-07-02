package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.findWindow
import dartzee.helper.AbstractTest
import dartzee.screen.dartzee.DartzeeTemplateSetupScreen
import dartzee.screen.player.PlayerManagementScreen
import dartzee.screen.preference.PreferencesScreen
import dartzee.screen.reporting.ReportingSetupScreen
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.sync.SyncManagementScreen
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

class TestMenuScreen: AbstractTest()
{
    @Test
    @Tag("screenshot")
    fun `Should match screenshot - default size`()
    {
        val scrn = wrapInFrame(APP_SIZE)
        scrn.shouldMatchImage("menu-screen-default", pixelTolerance = 0.5)
    }

    @Test
    @Tag("screenshot")
    fun `Should match screenshot - enlarged`()
    {
        val scrn = wrapInFrame(Dimension(1200, 800))
        scrn.shouldMatchImage("menu-screen-larger", pixelTolerance = 0.5)
    }

    private fun wrapInFrame(size: Dimension): MenuScreen
    {
        val app = JFrame()
        app.size = size
        val scrn = MenuScreen()
        app.add(scrn, BorderLayout.CENTER)
        app.isVisible = true

        return scrn
    }

    @Test
    fun `Should go to Sync Management screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Sync Setup")
        ScreenCache.currentScreen().shouldBeInstanceOf<SyncManagementScreen>()
    }

    @Test
    fun `Should go to the game setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "New Game")
        ScreenCache.currentScreen().shouldBeInstanceOf<GameSetupScreen>()
    }

    @Test
    fun `Should go to the player management screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Manage Players")
        ScreenCache.currentScreen().shouldBeInstanceOf<PlayerManagementScreen>()
    }

    @Test
    fun `Should go to the reporting setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Game Report")
        ScreenCache.currentScreen().shouldBeInstanceOf<ReportingSetupScreen>()
    }

    @Test
    fun `Should go to the leaderboards screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Leaderboards")
        ScreenCache.currentScreen().shouldBeInstanceOf<LeaderboardsScreen>()
    }

    @Test
    fun `Should go to the utilities screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Utilities")
        ScreenCache.currentScreen().shouldBeInstanceOf<UtilitiesScreen>()
    }

    @Test
    fun `Should go to the preferences screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Preferences")
        ScreenCache.currentScreen().shouldBeInstanceOf<PreferencesScreen>()
    }

    @Test
    fun `Should go to the dartzee template setup screen`()
    {
        val scrn = MenuScreen()
        scrn.clickChild<JButton>(text = "Dartzee Rules")
        ScreenCache.currentScreen().shouldBeInstanceOf<DartzeeTemplateSetupScreen>()
    }

    @Test
    fun `Should launch the ChangeLog if the link is clicked`()
    {
        val scrn = MenuScreen()

        val lbl = scrn.getChild<JLabel> { it.text.contains("Dartzee v") }
        lbl.doClick()

        val changeLog = findWindow<ChangeLog>()
        changeLog.shouldNotBeNull()
        changeLog.shouldBeVisible()
    }
}