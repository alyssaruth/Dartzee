package dartzee.achievements.x01

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.game.GameType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01GamesWon : TestAbstractAchievementGamesWon<AchievementX01GamesWon>() {
    override fun factoryAchievement() = AchievementX01GamesWon()

    @Test
    fun `Game type should be correct`() {
        AchievementX01GamesWon().gameType shouldBe GameType.X01
    }
}
