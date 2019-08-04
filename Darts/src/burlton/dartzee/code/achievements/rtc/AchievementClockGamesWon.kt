package burlton.dartzee.code.achievements.rtc

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_CLOCK_GAMES_WON
import burlton.dartzee.code.achievements.AbstractAchievementGamesWon
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.utils.ResourceCache
import java.net.URL

class AchievementClockGamesWon : AbstractAchievementGamesWon()
{
    override val achievementRef = ACHIEVEMENT_REF_CLOCK_GAMES_WON
    override val gameType = GAME_TYPE_ROUND_THE_CLOCK
    override val name = "Clock Winner"
    override val desc = "Total number of wins in Round the Clock"

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CLOCK_GAMES_WON
}