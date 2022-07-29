package dartzee.achievements.rtc

import dartzee.achievements.AchievementType
import dartzee.achievements.AbstractAchievementGamesWon
import dartzee.game.GameType
import dartzee.utils.ResourceCache

class AchievementClockGamesWon : AbstractAchievementGamesWon()
{
    override val achievementType = AchievementType.CLOCK_GAMES_WON
    override val gameType = GameType.ROUND_THE_CLOCK
    override val name = "Clock Winner"
    override val desc = "Total number of wins in Round the Clock"

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_CLOCK_GAMES_WON
}