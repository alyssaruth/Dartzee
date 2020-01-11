package burlton.dartzee.code.bean

import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.stats.player.PlayerAchievementsScreen
import burlton.desktopcore.code.bean.ScrollTableHyperlink

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