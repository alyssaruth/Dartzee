package dartzee.achievements.rtc

import dartzee.achievements.AbstractAchievementTeamGamesWon
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.ResourceCache

class AchievementClockTeamGamesWon : AbstractAchievementTeamGamesWon()
{
    override val name = "Clock Winners"
    override val desc = "Total number of team wins in Round the Clock"
    override val achievementType = AchievementType.CLOCK_TEAM_GAMES_WON
    override val gameType = GameType.ROUND_THE_CLOCK

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_CLOCK_WINNERS
}