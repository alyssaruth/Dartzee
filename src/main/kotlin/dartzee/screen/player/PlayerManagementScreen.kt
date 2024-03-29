package dartzee.screen.player

import dartzee.bean.getSelectedPlayer
import dartzee.bean.initPlayerTableModel
import dartzee.core.bean.ScrollTable
import dartzee.core.util.setMargins
import dartzee.db.PlayerEntity
import dartzee.screen.EmbeddedScreen
import dartzee.screen.HumanConfigurationDialog
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AIConfigurationDialog
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class PlayerManagementScreen : EmbeddedScreen(), ListSelectionListener {
    private val tablePlayers = ScrollTable()
    private val panel = PlayerManagementPanel()
    private val btnNewPlayer = JButton("")
    private val panelNorth = JPanel()
    private val btnNewAi = JButton("")

    init {
        val sideBar = JPanel()
        sideBar.layout = BorderLayout(0, 0)

        val panelPlayers = JPanel()
        panelPlayers.preferredSize = Dimension(200, 300)
        panelPlayers.border =
            TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        sideBar.add(panelPlayers, BorderLayout.CENTER)

        add(sideBar, BorderLayout.WEST)
        panelPlayers.layout = BorderLayout(0, 0)

        panelPlayers.add(tablePlayers, BorderLayout.CENTER)
        panelNorth.setMargins(5)

        sideBar.add(panelNorth, BorderLayout.NORTH)
        panelNorth.layout = FlowLayout(FlowLayout.LEFT, 5, 5)
        btnNewPlayer.icon =
            ImageIcon(PlayerManagementScreen::class.java.getResource("/buttons/addHuman.png"))
        btnNewPlayer.preferredSize = Dimension(30, 30)
        btnNewPlayer.toolTipText = "Add player"
        btnNewPlayer.name = "AddPlayer"
        panelNorth.add(btnNewPlayer)
        btnNewPlayer.border = EmptyBorder(5, 0, 5, 0)
        btnNewAi.icon =
            ImageIcon(PlayerManagementScreen::class.java.getResource("/buttons/addAi.png"))
        btnNewAi.toolTipText = "Add computer"
        btnNewAi.name = "AddAi"
        btnNewAi.preferredSize = Dimension(30, 30)

        panelNorth.add(btnNewAi)
        add(panel, BorderLayout.CENTER)

        val model = tablePlayers.selectionModel
        model.selectionMode = ListSelectionModel.SINGLE_SELECTION
        model.addListSelectionListener(this)

        // Listeners
        btnNewPlayer.addActionListener(this)
        btnNewAi.addActionListener(this)
    }

    override fun initialise() {
        val players = PlayerEntity.retrievePlayers("")
        tablePlayers.initPlayerTableModel(players)
        refreshSummaryPanel()
    }

    override fun getScreenName() = "Player Management"

    override fun valueChanged(arg0: ListSelectionEvent) {
        refreshSummaryPanel()
    }

    private fun refreshSummaryPanel() {
        val player = tablePlayers.getSelectedPlayer()
        panel.refresh(player)
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnNewPlayer -> createPlayer(true)
            btnNewAi -> createPlayer(false)
            else -> super.actionPerformed(arg0)
        }
    }

    private fun createPlayer(human: Boolean) {
        val playerUpdated = { _: PlayerEntity -> initialise() }
        val dlg =
            if (human) HumanConfigurationDialog(playerUpdated)
            else AIConfigurationDialog(playerUpdated)
        dlg.setLocationRelativeTo(ScreenCache.mainScreen)
        dlg.isVisible = true
    }
}
