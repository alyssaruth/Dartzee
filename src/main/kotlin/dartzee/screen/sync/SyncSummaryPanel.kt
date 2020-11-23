package dartzee.screen.sync

import dartzee.achievements.getGamesWonIcon
import dartzee.game.GameType
import dartzee.sync.SyncSummary
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.ImageIcon
import javax.swing.JButton

class SyncSummaryPanel: JButton(), MouseListener
{
    private val iconUrl = getGamesWonIcon(GameType.X01)
    private val hoverText = "<html><h1>Sync Settings &gt;</h1></html>"
    private var summaryText = ""

    init
    {
        iconTextGap = 25

        icon = ImageIcon(iconUrl)
        text = ""

        rolloverIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))
        selectedIcon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))

        addMouseListener(this)
    }

    fun refreshSummary(syncSummary: SyncSummary)
    {
        val lineOne = "<h2>Sync Status</h2>"
        val lineTwo = "<b>Syncing with: </b> ${syncSummary.remoteName}"
        val lineThree = "<b>Last synced: </b> ${syncSummary.lastSynced}"
        val lineFour = "<b>Pending games: </b> ${syncSummary.pendingGames}"
        summaryText = "<html><center>$lineOne $lineTwo<br>$lineThree<br>$lineFour</center></html>"
        text = summaryText
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?)
    {
        text = hoverText
    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = summaryText
    }
}