package dartzee.bean

import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen
import dartzee.core.bean.ScrollTableHyperlink

class ScrollTableAchievements : ScrollTableHyperlink("Player")
{
    override fun linkClicked(value: Any)
    {
        val player = value as PlayerEntity

        val scrn = ScreenCache.getScreen(PlayerAchievementsScreen::class.java)
        scrn.setPlayer(player)
        scrn.previousScrn = ScreenCache.currentScreen()!!

        ScreenCache.switchScreen(scrn)
    }
}