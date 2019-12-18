package burlton.dartzee.test.achievements.dartzee

import burlton.dartzee.code.achievements.dartzee.AchievementDartzeeGamesWon
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.test.achievements.TestAbstractAchievementGamesWon
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