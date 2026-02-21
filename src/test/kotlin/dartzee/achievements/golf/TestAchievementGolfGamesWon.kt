package dartzee.achievements.golf

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.game.GameType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementGolfGamesWon : TestAbstractAchievementGamesWon<AchievementGolfGamesWon>() {
    override fun factoryAchievement() = AchievementGolfGamesWon()

    @Test
    fun `Game type should be correct`() {
        factoryAchievement().gameType shouldBe GameType.GOLF
    }
}
