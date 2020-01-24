package dartzee.achievements.dartzee

import dartzee.achievements.TestAbstractAchievementGamesWon
import dartzee.db.GAME_TYPE_DARTZEE
import io.kotlintest.shouldBe
import org.junit.Test


class TestAchievementDartzeeGamesWon: TestAbstractAchievementGamesWon<AchievementDartzeeGamesWon>()
{
    override fun factoryAchievement() = AchievementDartzeeGamesWon()

    @Test
    fun `Game type should be correct`()
    {
        factoryAchievement().gameType shouldBe GAME_TYPE_DARTZEE
    }
}