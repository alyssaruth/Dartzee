package dartzee.achievements.x01

import dartzee.achievements.TestAbstractAchievementBestGame
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01BestGame : TestAbstractAchievementBestGame<AchievementX01BestGame>() {
    override fun factoryAchievement() = AchievementX01BestGame()

    @Test
    fun `Should only count games of 501`() {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GameType.X01

        val config = X01Config.fromJson(achievement.gameParams)
        config.target shouldBe 501
        config.finishType shouldBe FinishType.Doubles
    }
}
