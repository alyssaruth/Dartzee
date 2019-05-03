package burlton.dartzee.test.achievements.rtc

import burlton.dartzee.code.achievements.rtc.AchievementClockGamesWon
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.test.achievements.TestAbstractAchievementGamesWon
import io.kotlintest.shouldBe
import org.junit.Test


class TestAchievementClockGamesWon: TestAbstractAchievementGamesWon<AchievementClockGamesWon>()
{
    override fun factoryAchievement() = AchievementClockGamesWon()

    @Test
    fun `Game type should be correct`()
    {
        factoryAchievement().gameType shouldBe GAME_TYPE_ROUND_THE_CLOCK
    }
}