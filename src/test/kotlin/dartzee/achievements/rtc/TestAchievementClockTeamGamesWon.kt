package dartzee.achievements.rtc

import dartzee.achievements.TestAbstractAchievementTeamGamesWon

class TestAchievementClockTeamGamesWon :
    TestAbstractAchievementTeamGamesWon<AchievementClockTeamGamesWon>() {
    override fun factoryAchievement() = AchievementClockTeamGamesWon()
}
