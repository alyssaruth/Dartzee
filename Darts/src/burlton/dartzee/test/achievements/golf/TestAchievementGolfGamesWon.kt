package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.achievements.golf.AchievementGolfGamesWon
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.test.achievements.TestAbstractAchievementGamesWon
import io.kotlintest.shouldBe
import org.junit.Test


class TestAchievementGolfGamesWon: TestAbstractAchievementGamesWon<AchievementGolfGamesWon>()
{
    override fun factoryAchievement() = AchievementGolfGamesWon()

    @Test
    fun `Game type should be correct`()
    {
        factoryAchievement().gameType shouldBe GAME_TYPE_GOLF
    }
}