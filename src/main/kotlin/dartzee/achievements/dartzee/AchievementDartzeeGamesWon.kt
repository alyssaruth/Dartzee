package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.achievements.AchievementType
import dartzee.game.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementDartzeeGamesWon : AbstractAchievementGamesWon()
{
    override val achievementType = AchievementType.DARTZEE_GAMES_WON
    override val gameType = GameType.DARTZEE
    override val name = "Dartzee Winner"
    override val desc = "Total number of wins in Dartzee"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_DARTZEE_GAMES_WON
}