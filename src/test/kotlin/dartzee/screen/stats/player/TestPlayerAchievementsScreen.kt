package dartzee.screen.stats.player

import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestPlayerAchievementsScreen
{
    @Test
    fun `Should go back to the desired previous screen`()
    {
        val p = insertPlayer()
        val startingScreen = ScreenCache.currentScreen()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.backPressed()

        ScreenCache.currentScreen() shouldBe startingScreen
    }

    @Test
    fun `Should update title with player name and achievement progress`()
    {

    }
}