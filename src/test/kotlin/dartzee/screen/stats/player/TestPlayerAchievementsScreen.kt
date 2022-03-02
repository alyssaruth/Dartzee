package dartzee.screen.stats.player

import dartzee.achievements.AchievementType
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.db.AchievementEntity
import dartzee.helper.insertGame
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
        val p = insertPlayer(name = "Bob")
        val g = insertGame()

        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, p.rowId, g.rowId, AchievementX01BestFinish().blueThreshold)
        AchievementEntity.updateAchievement(AchievementType.X01_HIGHEST_BUST, p.rowId, g.rowId, AchievementX01HighestBust().pinkThreshold)

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.getScreenName() shouldBe "Achievements - Bob - 11/${getAchievementMaximum()}"
    }

    @Test
    fun `Should do things on hover`()
    {

    }
}