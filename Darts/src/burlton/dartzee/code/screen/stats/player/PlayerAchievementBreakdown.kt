package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.bean.ScrollTableDartsGame
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import java.awt.BorderLayout

class PlayerAchievementBreakdown : EmbeddedScreen()
{
    var achievement: AbstractAchievement? = null

    private val tableBreakdown = ScrollTableDartsGame()

    override fun initialise()
    {
        add(tableBreakdown, BorderLayout.CENTER)
    }

    override fun getScreenName(): String
    {
        return "${achievement?.name} Breakdown - ${achievement?.player?.name}"
    }


    fun setState(achievement: AbstractAchievement)
    {
        this.achievement = achievement

        tableBreakdown.model = achievement.tmBreakdown!!
    }

    override fun getBackTarget(): EmbeddedScreen
    {
        return ScreenCache.getScreen(PlayerAchievementsScreen::class.java)
    }

}

