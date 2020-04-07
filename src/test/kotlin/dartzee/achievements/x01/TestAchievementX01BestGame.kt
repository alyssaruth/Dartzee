package dartzee.achievements.x01

import dartzee.achievements.TestAbstractAchievementBestGame
import dartzee.game.GameType
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01BestGame: TestAbstractAchievementBestGame<AchievementX01BestGame>()
{
    override fun factoryAchievement() = AchievementX01BestGame()

    @Test
    fun `Should only count games of 501`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GameType.X01
        achievement.gameParams shouldBe "501"
    }
}