package dartzee.achievements.dartzee

import dartzee.achievements.ACHIEVEMENT_REF_DARTZEE_GAMES_WON
import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.db.GAME_TYPE_DARTZEE
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementDartzeeGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_DARTZEE_GAMES_WON
    override val gameType = GAME_TYPE_DARTZEE
    override val name = "Dartzee Winner"
    override val desc = "Total number of wins in Dartzee"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_DARTZEE_GAMES_WON
}