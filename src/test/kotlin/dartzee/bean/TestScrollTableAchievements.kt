package dartzee.bean

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import dartzee.screen.stats.overall.LeaderboardsScreen
import dartzee.screen.stats.player.PlayerAchievementsScreen
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestScrollTableAchievements: AbstractTest()
{
    @Test
    fun `Should switch to the player achievements screen on click`()
    {
        val startingScreen = LeaderboardsScreen()
        ScreenCache.switch(startingScreen)

        val player = insertPlayer()

        val scrollTable = ScrollTableAchievements()
        scrollTable.linkClicked(player)

        val scrn = ScreenCache.currentScreen()
        scrn.getBackTarget() shouldBe startingScreen
        scrn.shouldBeInstanceOf<PlayerAchievementsScreen>()
        scrn.player shouldBe player
    }
}