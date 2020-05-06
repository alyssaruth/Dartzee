package dartzee.screen.player

import dartzee.achievements.getGamesWonIcon
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerStatisticsScreen
import java.awt.Dimension
import java.awt.event.*
import javax.swing.ImageIcon
import javax.swing.JButton

class PlayerStatsButton(private val player: PlayerEntity,
                        private val gameType: GameType,
                        played: Int,
                        highScore: Int) : JButton(), ActionListener, MouseListener
{
    private val iconUrl = getGamesWonIcon(gameType)
    private val statsText = makeStatsText(played, highScore)

    init
    {
        preferredSize = Dimension(275, 100)

        icon = ImageIcon(iconUrl)
        text = statsText

        rolloverIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))
        selectedIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))

        isEnabled = played > 0

        addActionListener(this)
        addMouseListener(this)
    }

    private fun makeStatsText(played: Int, highScore: Int): String
    {
        val lineOne = "<h3>${gameType.getDescription()}</h3>"
        val lineTwo = "<b>P:</b> $played  <b>Best:</b> $highScore"
        return "<html><center>$lineOne<br>$lineTwo</center></html>"
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        val statsScrn = ScreenCache.get<PlayerStatisticsScreen>()
        statsScrn.setVariables(gameType, player)

        ScreenCache.switch(statsScrn)

        text = statsText
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?)
    {
        if (isEnabled)
        {
            text = "<html><h3>${gameType.getDescription()} stats &gt;</h3></html>"
        }
    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = statsText
    }
}
