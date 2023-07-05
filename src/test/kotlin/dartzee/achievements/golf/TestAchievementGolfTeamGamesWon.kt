package dartzee.achievements.golf

import dartzee.achievements.TestAbstractAchievementTeamGamesWon

class TestAchievementGolfTeamGamesWon : TestAbstractAchievementTeamGamesWon<AchievementGolfTeamGamesWon>()
{
    override fun factoryAchievement() = AchievementGolfTeamGamesWon()
}