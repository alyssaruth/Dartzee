package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementClockGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_GAMES_WON
    override val gameType = GameEntity.GAME_TYPE_ROUND_THE_CLOCK
    override val name = "Round the Clock Wins"

    override fun getIconURL(): URL?
    {
        return ResourceCache.URL_ACHIEVEMENT_CLOCK_GAMES_WON
    }
}