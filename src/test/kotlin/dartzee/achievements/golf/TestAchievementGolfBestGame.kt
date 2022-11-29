package dartzee.achievements.golf

import dartzee.achievements.TestAbstractAchievementBestGame
import dartzee.game.GameType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementGolfBestGame: TestAbstractAchievementBestGame<AchievementGolfBestGame>()
{
    override fun factoryAchievement() = AchievementGolfBestGame()

    @Test
    fun `Should only count 18-hole games`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GameType.GOLF
        achievement.gameParams shouldBe "18"
    }
}