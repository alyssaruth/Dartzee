package dartzee.achievements.x01

import dartzee.achievements.x01.AchievementX01GamesWon
import dartzee.db.GAME_TYPE_X01
import dartzee.achievements.TestAbstractAchievementGamesWon
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01GamesWon: TestAbstractAchievementGamesWon<AchievementX01GamesWon>()
{
    override fun factoryAchievement() = AchievementX01GamesWon()

    @Test
    fun `Game type should be correct`()
    {
        AchievementX01GamesWon().gameType shouldBe GAME_TYPE_X01
    }
}