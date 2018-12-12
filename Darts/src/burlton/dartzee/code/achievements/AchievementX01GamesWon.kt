package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementX01GamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_GAMES_WON
    override val gameType = GameEntity.GAME_TYPE_X01
    override val name = "X01 Wins"

    override fun getIconURL(): URL?
    {
        return ResourceCache.URL_ACHIEVEMENT_X01_GAMES_WON
    }
}