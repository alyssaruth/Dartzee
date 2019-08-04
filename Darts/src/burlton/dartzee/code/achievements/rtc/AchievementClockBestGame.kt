package burlton.dartzee.code.achievements.rtc

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_CLOCK_BEST_GAME
import burlton.dartzee.code.achievements.AbstractAchievementBestGame
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementClockBestGame : AbstractAchievementBestGame()
{
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_BEST_GAME
    override val name = "Stop the Clock!"
    override val desc = "Best game of Round the Clock (Standard)"
    override val gameType = GAME_TYPE_ROUND_THE_CLOCK
    override val gameParams = CLOCK_TYPE_STANDARD

    override val redThreshold = 120
    override val orangeThreshold = 100
    override val yellowThreshold = 80
    override val greenThreshold = 60
    override val blueThreshold = 40
    override val pinkThreshold = 30
    override val maxValue = 20

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CLOCK_BEST_GAME
}