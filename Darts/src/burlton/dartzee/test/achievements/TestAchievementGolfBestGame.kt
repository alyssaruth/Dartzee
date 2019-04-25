package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementGolfBestGame

class TestAchievementGolfBestGame: AbstractAchievementTest<AchievementGolfBestGame>()
{
    override fun factoryAchievement() = AchievementGolfBestGame()
}