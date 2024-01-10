package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievementTeamGamesWon
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.ResourceCache

class AchievementX01TeamGamesWon : AbstractAchievementTeamGamesWon() {
    override val name = "X01 Winners"
    override val desc = "Total number of team wins in X01"
    override val achievementType = AchievementType.X01_TEAM_GAMES_WON
    override val gameType = GameType.X01

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_X01_WINNERS
}
