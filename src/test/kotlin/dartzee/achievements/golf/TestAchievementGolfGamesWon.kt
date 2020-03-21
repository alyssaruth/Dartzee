package dartzee.achievements.golf

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.db.GAME_TYPE_GOLF
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