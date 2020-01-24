package dartzee.achievements.golf

import dartzee.achievements.TestAbstractAchievementBestGame
import dartzee.db.GAME_TYPE_GOLF
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