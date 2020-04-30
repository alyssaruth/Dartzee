package dartzee.screen.stats.player

import dartzee.achievements.AbstractAchievement
import dartzee.bean.ScrollTableDartsGame
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
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

    override fun getBackTarget() = ScreenCache.getScreen<PlayerAchievementsScreen>()

}

