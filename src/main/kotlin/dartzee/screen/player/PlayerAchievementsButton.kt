package dartzee.screen.player

import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.getGamesWonIcon
import dartzee.achievements.getPlayerAchievementScore
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen
import java.awt.event.ActionListener
import javax.swing.ImageIcon

class PlayerAchievementsButton(private val player: PlayerEntity,
                               private val achievementRows: List<AchievementEntity>): PlayerSummaryButton(), ActionListener
{
    override val defaultText = makeDefaultText()
    override val hoverText = "<html><h3>Achievements &gt;</h3></html>"

    private val iconUrl = getGamesWonIcon(GameType.X01)

    init
    {
        icon = ImageIcon(iconUrl)
        text = defaultText

        addActionListener(this)
        addMouseListener(this)
    }

    private fun makeDefaultText(): String
    {
        val score = getPlayerAchievementScore(achievementRows, player)
        val lineOne = "<h3>Achievements</h3>"
        val lineTwo = "$score / ${getAchievementMaximum()}"
        return "<html><center>$lineOne<br>$lineTwo</center></html>"
    }

    override fun buttonPressed()
    {
        val scrn = ScreenCache.get<PlayerAchievementsScreen>()
        scrn.player = player
        scrn.previousScrn = ScreenCache.get<PlayerManagementScreen>()
        ScreenCache.switch(scrn)
    }
}