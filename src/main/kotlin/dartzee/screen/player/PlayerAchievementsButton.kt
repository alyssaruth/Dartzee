package dartzee.screen.player

import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.getGamesWonIcon
import dartzee.achievements.getPlayerAchievementScore
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementsScreen
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.ImageIcon
import javax.swing.JButton

class PlayerAchievementsButton(private val player: PlayerEntity,
                               private val achievementRows: List<AchievementEntity>): JButton(), ActionListener, MouseListener
{
    private val iconUrl = getGamesWonIcon(GameType.X01)
    private val statsText = makeStatsText()

    init
    {
        preferredSize = Dimension(275, 100)

        icon = ImageIcon(iconUrl)
        text = statsText

        addActionListener(this)
        addMouseListener(this)
    }

    private fun makeStatsText(): String
    {
        val score = getPlayerAchievementScore(achievementRows, player)
        val lineOne = "<h3>Achievements</h3>"
        val lineTwo = "$score / ${getAchievementMaximum()}"
        return "<html><center>$lineOne<br>$lineTwo</center></html>"
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        val scrn = ScreenCache.get<PlayerAchievementsScreen>()
        scrn.player = player
        scrn.previousScrn = ScreenCache.get<PlayerManagementScreen>()

        ScreenCache.switch(scrn)

        text = statsText
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?)
    {
        if (isEnabled)
        {
            text = "<html><h3>Achievements &gt;</h3></html>"
        }
    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = statsText
    }
}