package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementClockBestGame

class TestAchievementClockBestGame: AbstractAchievementTest<AchievementClockBestGame>()
{
    override fun factoryAchievement() = AchievementClockBestGame()
}