package dartzee.achievements.rtc

import dartzee.achievements.TestAbstractAchievementBestGame
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementClockBestGame: TestAbstractAchievementBestGame<AchievementClockBestGame>()
{
    override fun factoryAchievement() = AchievementClockBestGame()

    @Test
    fun `Should only count standard games of RTC`()
    {
        val achievement = factoryAchievement()

        achievement.gameType shouldBe GameType.ROUND_THE_CLOCK
        achievement.gameParams shouldBe RoundTheClockConfig(ClockType.Standard, true).toJson()
    }
}