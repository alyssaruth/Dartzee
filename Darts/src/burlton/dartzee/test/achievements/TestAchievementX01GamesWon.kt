package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01GamesWon
import burlton.dartzee.code.db.GAME_TYPE_X01
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01GamesWon: AbstractAchievementTest<AchievementX01GamesWon>()
{
    override fun factoryAchievement() = AchievementX01GamesWon()

    @Test
    fun `Game type should be correct`()
    {
        AchievementX01GamesWon().gameType shouldBe GAME_TYPE_X01
    }
}