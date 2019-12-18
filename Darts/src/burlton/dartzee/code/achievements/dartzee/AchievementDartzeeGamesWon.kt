package burlton.dartzee.code.achievements.dartzee

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_DARTZEE_GAMES_WON
import burlton.dartzee.code.achievements.AbstractAchievementGamesWon
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementDartzeeGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_DARTZEE_GAMES_WON
    override val gameType = GAME_TYPE_DARTZEE
    override val name = "Dartzee Winner"
    override val desc = "Total number of wins in Dartzee"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_DARTZEE_GAMES_WON
}