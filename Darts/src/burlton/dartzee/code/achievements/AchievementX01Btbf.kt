package burlton.dartzee.code.achievements

import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES
import java.net.URL

class AchievementX01Btbf: AbstractAchievement()
{
    override val achievementRef = ACHIEVEMENT_REF_X01_BTBF
    override val name = "BTBF"
    override val desc = "Number of games of X01 finished on D1"

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = pinkThreshold

    override fun getIconURL(): URL = URL_ACHIEVEMENT_CLOCK_BRUCEY_BONUSES

    override fun populateForConversion(playerIds: String)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}