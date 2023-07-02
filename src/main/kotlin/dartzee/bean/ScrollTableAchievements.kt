package dartzee.bean

import dartzee.achievements.AchievementType
import dartzee.core.bean.ScrollTableHyperlink
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.screen.stats.overall.LeaderboardAchievements
import javax.swing.SwingUtilities

class ScrollTableAchievements(private val parentScrn: LeaderboardAchievements) : ScrollTableHyperlink("Player")
{
    override fun linkClicked(value: Any)
    {
        val player = value as PlayerEntity

        val achievementType = parentScrn.getSelectedAchievement().achievementType

        val scrn = ScreenCache.switchToAchievementsScreen(player)
        if (achievementType != AchievementType.DUMMY_TOTAL)
        {
            SwingUtilities.invokeLater {
                scrn.scrollIntoView(achievementType)
            }
        }
    }
}