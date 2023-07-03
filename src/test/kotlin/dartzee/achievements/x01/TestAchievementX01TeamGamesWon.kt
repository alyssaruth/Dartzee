package dartzee.achievements.x01

import dartzee.achievements.TestAbstractAchievementTeamGamesWon

class TestAchievementX01TeamGamesWon : TestAbstractAchievementTeamGamesWon<AchievementX01TeamGamesWon>()
{
    override fun factoryAchievement() = AchievementX01TeamGamesWon()
}