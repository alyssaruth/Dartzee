package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievementTeamGamesWon
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.ResourceCache

class AchievementDartzeeTeamGamesWon : AbstractAchievementTeamGamesWon()
{
    override val name = "Dartzee Winners"
    override val desc = "Total number of team wins in Dartzee"
    override val achievementType = AchievementType.DARTZEE_TEAM_GAMES_WON
    override val gameType = GameType.DARTZEE

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_WINNERS
}