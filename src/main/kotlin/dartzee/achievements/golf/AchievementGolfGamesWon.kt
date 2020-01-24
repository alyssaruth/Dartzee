package dartzee.achievements.golf

import dartzee.achievements.ACHIEVEMENT_REF_GOLF_GAMES_WON
import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.db.GAME_TYPE_GOLF
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementGolfGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_GOLF_GAMES_WON
    override val gameType = GAME_TYPE_GOLF
    override val name = "Golf Winner"
    override val desc = "Total number of wins in Golf"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_GOLF_GAMES_WON
}