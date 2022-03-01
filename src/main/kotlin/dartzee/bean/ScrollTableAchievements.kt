package dartzee.bean

import dartzee.core.bean.ScrollTableHyperlink
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.screen.stats.overall.LeaderboardAchievements
import dartzee.screen.stats.player.PlayerAchievementsScreen

class ScrollTableAchievements(private val parentScrn: LeaderboardAchievements) : ScrollTableHyperlink("Player")
{
    override fun linkClicked(value: Any)
    {
        val player = value as PlayerEntity

        val gameType = parentScrn.getSelectedAchievement().gameType

        val scrn = ScreenCache.get<PlayerAchievementsScreen>()
        scrn.player = player
        scrn.previousScrn = ScreenCache.currentScreen()

        ScreenCache.switch(scrn)

        if (gameType != null)
        {
            scrn.selectTab(gameType)
        }
    }
}