package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01BestGame
import burlton.dartzee.code.db.GAME_TYPE_X01
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