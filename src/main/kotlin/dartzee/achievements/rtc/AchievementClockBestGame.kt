package dartzee.achievements.rtc

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BEST_GAME
import dartzee.achievements.AbstractAchievementBestGame
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.game.GameType
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementClockBestGame : AbstractAchievementBestGame()
{
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_BEST_GAME
    override val name = "Stop the Clock!"
    override val desc = "Best game of Round the Clock (Standard)"
    override val gameType = GameType.ROUND_THE_CLOCK
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