package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01BestGame
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.test.achievements.TestAbstractAchievementBestGame
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01BestGame: TestAbstractAchievementBestGame<AchievementX01BestGame>()
{
    override fun factoryAchievement() = AchievementX01BestGame()

    @Test
    fun `Should only count games of 501`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GAME_TYPE_X01
        achievement.gameParams shouldBe "501"
    }
}