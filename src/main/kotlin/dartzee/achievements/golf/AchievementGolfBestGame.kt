package dartzee.achievements.golf

import dartzee.achievements.ACHIEVEMENT_REF_GOLF_BEST_GAME
import dartzee.achievements.AbstractAchievementBestGame
import dartzee.game.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementGolfBestGame : AbstractAchievementBestGame()
{
    override val achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME
    override val name = "Career Round"
    override val desc = "Best game of golf (18 holes)"
    override val gameType = GameType.GOLF
    override val gameParams = "18"

    override val redThreshold = 81    //4.5 per hole
    override val orangeThreshold = 72 //4 per hole
    override val yellowThreshold = 63 //3.5 per hole
    override val greenThreshold = 54  //3 hole
    override val blueThreshold = 45   //2.5 per hole
    override val pinkThreshold = 36   //2 per hole
    override val maxValue = 18

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_GOLF_BEST_GAME
}