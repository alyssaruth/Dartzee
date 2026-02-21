package dartzee.bean

import dartzee.core.bean.ScrollTableHyperlink
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache

class ScrollTableAchievements : ScrollTableHyperlink("Player") {
    override fun linkClicked(value: Any) {
        val player = value as PlayerEntity
        ScreenCache.switchToAchievementsScreen(player)
    }
}
