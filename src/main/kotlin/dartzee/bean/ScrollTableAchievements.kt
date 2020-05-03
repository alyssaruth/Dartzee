package dartzee.bean

import dartzee.core.bean.ScrollTableHyperlink
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen

class ScrollTableAchievements : ScrollTableHyperlink("Player")
{
    override fun linkClicked(value: Any)
    {
        val player = value as PlayerEntity

        val scrn = ScreenCache.get<PlayerAchievementsScreen>()
        scrn.player = player
        scrn.previousScrn = ScreenCache.currentScreen()

        ScreenCache.switch(scrn)
    }
}