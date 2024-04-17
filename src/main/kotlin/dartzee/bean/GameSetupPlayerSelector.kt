package dartzee.bean

import dartzee.core.bean.ScrollTableOrdered
import dartzee.core.util.DialogUtil
import dartzee.core.util.setMargins
import dartzee.db.MAX_PLAYERS
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JToggleButton

class GameSetupPlayerSelector : AbstractPlayerSelector<ScrollTableOrdered>() {
    override val tablePlayersSelected = ScrollTableOrdered()
    private val btnPairs = JToggleButton("")

    init {
        super.render()

        if (InjectedThings.partyMode) {
            val font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 24f)
            tablePlayersToSelectFrom.setTableFont(font)
            tablePlayersSelected.setTableFont(font)
        }


        btnPairs.icon = ImageIcon(javaClass.getResource("/buttons/teams.png"))
        btnPairs.toolTipText = "Play in pairs"
        tablePlayersSelected.addButtonToOrderingPanel(btnPairs, 3)
        lblSelected.setMargins(0, 0, 0, 60)

        btnPairs.addActionListener(this)
    }

    override fun init() {
        super.init()

        val nimbusRenderer = tablePlayersSelected.getBuiltInRenderer()
        tablePlayersSelected.setTableRenderer(TeamRenderer(nimbusRenderer) { btnPairs.isSelected })
    }

    fun pairMode(): Boolean = btnPairs.isSelected

    /** Is this selection valid for a game/match? */
    fun valid(match: Boolean): Boolean {
        val selectedPlayers = getSelectedPlayers()
        val rowCount = selectedPlayers.size
        if (rowCount < 1) {
            DialogUtil.showError("You must select at least 1 player.")
            return false
        }

        val playerOrTeamDesc = if (btnPairs.isSelected) "teams" else "players"
        val matchMinimum = if (btnPairs.isSelected) 4 else 2
        if (match && rowCount < matchMinimum) {
            DialogUtil.showError("You must select at least 2 $playerOrTeamDesc for a match.")
            return false
        }

        val maxPlayers = if (btnPairs.isSelected) MAX_PLAYERS * 2 else MAX_PLAYERS
        if (rowCount > maxPlayers) {
            DialogUtil.showError("You cannot select more than $MAX_PLAYERS $playerOrTeamDesc.")
            return false
        }

        return true
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            btnPairs -> tablePlayersSelected.repaint()
            else -> super.actionPerformed(e)
        }
    }
}
