package dartzee.achievements.rtc

import dartzee.achievements.AchievementType
import dartzee.achievements.AbstractAchievementBestGame
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import dartzee.utils.ResourceCache
import java.net.URL

class AchievementClockBestGame : AbstractAchievementBestGame()
{
    override val achievementType = AchievementType.CLOCK_BEST_GAME
    override val name = "Stop the Clock!"
    override val desc = "Best game of Round the Clock (Standard)"
    override val gameType = GameType.ROUND_THE_CLOCK
    override val gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson()

    override val redThreshold = 120
    override val orangeThreshold = 100
    override val yellowThreshold = 80
    override val greenThreshold = 60
    override val blueThreshold = 40
    override val pinkThreshold = 30
    override val maxValue = 20

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_CLOCK_BEST_GAME
}