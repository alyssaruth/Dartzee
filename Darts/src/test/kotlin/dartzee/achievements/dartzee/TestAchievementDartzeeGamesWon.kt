package dartzee.achievements.dartzee

import dartzee.achievements.dartzee.AchievementDartzeeGamesWon
import dartzee.db.GAME_TYPE_DARTZEE
import dartzee.achievements.TestAbstractAchievementGamesWon
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