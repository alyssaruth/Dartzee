package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementClockGamesWon
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import io.kotlintest.shouldBe
import org.junit.Test


class TestAchievementClockGamesWon: AbstractAchievementTest<AchievementClockGamesWon>()
{
    override fun factoryAchievement() = AchievementClockGamesWon()

    @Test
    fun `Game type should be correct`()
    {
        factoryAchievement().gameType shouldBe GAME_TYPE_ROUND_THE_CLOCK
    }
}