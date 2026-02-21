package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievementTeamGamesWon
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.ResourceCache

class AchievementGolfTeamGamesWon : AbstractAchievementTeamGamesWon() {
    override val name = "Golf Winners"
    override val desc = "Total number of team wins in Golf"
    override val achievementType = AchievementType.GOLF_TEAM_GAMES_WON
    override val gameType = GameType.GOLF

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_GOLF_WINNERS
}
