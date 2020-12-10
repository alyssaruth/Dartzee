package dartzee.bean

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.MenuScreen
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestScrollTableAchievements: AbstractTest()
{
    @Test
    fun `Should switch to the player achievements screen on click`()
    {
        val currentScreen = MenuScreen()
        ScreenCache.switch(currentScreen)

        val player = insertPlayer()

        val scrollTable = ScrollTableAchievements()
        scrollTable.linkClicked(player)

        val scrn = ScreenCache.currentScreen()
        scrn.getBackTarget() shouldBe currentScreen
        scrn.shouldBeInstanceOf<PlayerAchievementsScreen>()
        (scrn as PlayerAchievementsScreen).player shouldBe player
    }
}