package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.achievements.golf.AchievementGolfBestGame
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.test.achievements.TestAbstractAchievementBestGame
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementGolfBestGame: TestAbstractAchievementBestGame<AchievementGolfBestGame>()
{
    override fun factoryAchievement() = AchievementGolfBestGame()

    @Test
    fun `Should only count 18-hole games`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GAME_TYPE_GOLF
        achievement.gameParams shouldBe "18"
    }
}