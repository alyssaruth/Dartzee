package dartzee.bean

import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.items
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import dartzee.screen.stats.overall.LeaderboardAchievements
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.stats.player.PlayerAchievementsScreen
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import javax.swing.JScrollPane

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
        scrn.player shouldBe player
    }

    @Test
    fun `Should select the right tab based on achievement game type`()
    {
        val leaderboard = LeaderboardAchievements()
        leaderboard.cbSpecificAchievement.doClick()
        val selectedAchievement = leaderboard.comboBox.items().find { it.gameType == GameType.DARTZEE }
        leaderboard.comboBox.selectedItem = selectedAchievement

        val player = insertPlayer()

        val scrollTable = ScrollTableAchievements(leaderboard)
        scrollTable.linkClicked(player)
        flushEdt()

        val scrn = ScreenCache.currentScreen()
        scrn.shouldBeInstanceOf<PlayerAchievementsScreen>()
        val scrollPane = scrn.getChild<JScrollPane>()
        scrollPane.verticalScrollBar.value shouldBeGreaterThan 0
    }
}