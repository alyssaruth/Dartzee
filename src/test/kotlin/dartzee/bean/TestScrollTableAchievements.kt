package dartzee.bean

import com.github.alexburlton.swingtest.getChild
import dartzee.core.bean.items
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import dartzee.screen.stats.overall.LeaderboardAchievements
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.stats.player.PlayerAchievementsScreen
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JTabbedPane

class TestScrollTableAchievements: AbstractTest()
{
    @Test
    fun `Should switch to the player achievements screen on click`()
    {
        val startingScreen = LeaderboardsScreen()
        ScreenCache.switch(startingScreen)

        val player = insertPlayer()

        val scrollTable = ScrollTableAchievements(LeaderboardAchievements())
        scrollTable.linkClicked(player)

        val scrn = ScreenCache.currentScreen()
        scrn.getBackTarget() shouldBe startingScreen
        scrn.shouldBeInstanceOf<PlayerAchievementsScreen>()
        (scrn as PlayerAchievementsScreen).player shouldBe player
    }

    @Test
    fun `Should select the right tab based on achievement game type`()
    {
        val leaderboard = LeaderboardAchievements()
        leaderboard.cbSpecificAchievement.doClick()
        val selectedAchievement = leaderboard.comboBox.items().find { it.gameType == GameType.DARTZEE }
        leaderboard.comboBox.selectedItem = selectedAchievement

        val startingScreen = LeaderboardsScreen()
        ScreenCache.switch(startingScreen)

        val player = insertPlayer()

        val scrollTable = ScrollTableAchievements(leaderboard)
        scrollTable.linkClicked(player)

        val scrn = ScreenCache.currentScreen()
        scrn.getBackTarget() shouldBe startingScreen
        scrn.shouldBeInstanceOf<PlayerAchievementsScreen>()
        val tabbedPane = scrn.getChild<JTabbedPane>()
        tabbedPane.selectedIndex shouldBe tabbedPane.indexOfTab(GameType.DARTZEE.getDescription())
    }
}