package burlton.dartzee.test.achievements.rtc

import burlton.dartzee.code.achievements.rtc.AchievementClockBestGame
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.test.achievements.TestAbstractAchievementBestGame
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementClockBestGame: TestAbstractAchievementBestGame<AchievementClockBestGame>()
{
    override fun factoryAchievement() = AchievementClockBestGame()

    @Test
    fun `Should only count standard games of RTC`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GAME_TYPE_ROUND_THE_CLOCK
        achievement.gameParams shouldBe CLOCK_TYPE_STANDARD
    }
}