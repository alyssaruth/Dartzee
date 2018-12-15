package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementGolfGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_GOLF_GAMES_WON
    override val gameType = GameEntity.GAME_TYPE_GOLF
    override val name = "Golf Winner"
    override val desc = "Total number of wins in Golf"

    override fun getIconURL(): URL?
    {
        return ResourceCache.URL_ACHIEVEMENT_GOLF_GAMES_WON
    }
}