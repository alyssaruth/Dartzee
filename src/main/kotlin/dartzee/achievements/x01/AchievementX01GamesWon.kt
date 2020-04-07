package dartzee.achievements.x01

import dartzee.achievements.ACHIEVEMENT_REF_X01_GAMES_WON
import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.db.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementX01GamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_GAMES_WON
    override val gameType = GameType.X01
    override val name = "X01 Winner"
    override val desc = "Total number of games won in X01"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_X01_GAMES_WON
}