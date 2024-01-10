package dartzee.achievements.dartzee

import dartzee.achievements.TestAbstractAchievementTeamGamesWon

class TestAchievementDartzeeTeamGamesWon :
    TestAbstractAchievementTeamGamesWon<AchievementDartzeeTeamGamesWon>() {
    override fun factoryAchievement() = AchievementDartzeeTeamGamesWon()
}
