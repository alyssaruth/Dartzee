package dartzee.screen

import dartzee.bean.PlayerAvatar
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetup
import dartzee.screen.stats.player.PlayerAchievementsScreen
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.sql.Timestamp
import javax.swing.*

class PlayerManagementPanel : JPanel(), ActionListener
{
    private var player: PlayerEntity? = null

    private val btnEdit = JButton("Edit")
    private val btnDelete = JButton("Delete")
    private val lblPlayerName = JLabel("")
    private val panel = JPanel()
    private val avatar = PlayerAvatar()
    private val panelCenter = JPanel()
    private val panelX01 = PlayerSummaryPanel(GameType.X01)
    private val panelGolf = PlayerSummaryPanel(GameType.GOLF)
    private val panelClock = PlayerSummaryPanel(GameType.ROUND_THE_CLOCK)
    private val btnRunSimulation = JButton("Run Simulation")
    private val btnAchievements = JButton("Achievements")

    init
    {
        layout = BorderLayout(0, 0)

        val panelOptions = JPanel()
        add(panelOptions, BorderLayout.SOUTH)

        panelOptions.add(btnAchievements)
        btnEdit.font = Font("Tahoma", Font.PLAIN, 16)
        panelOptions.add(btnEdit)
        btnRunSimulation.font = Font("Tahoma", Font.PLAIN, 16)

        panelOptions.add(btnRunSimulation)
        btnDelete.font = Font("Tahoma", Font.PLAIN, 16)
        panelOptions.add(btnDelete)

        btnAchievements.font = Font("Tahoma", Font.PLAIN, 16)

        btnEdit.addActionListener(this)
        btnRunSimulation.addActionListener(this)
        btnDelete.addActionListener(this)
        btnAchievements.addActionListener(this)

        add(panel, BorderLayout.CENTER)
        panel.layout = BorderLayout(0, 0)
        lblPlayerName.preferredSize = Dimension(0, 25)
        panel.add(lblPlayerName, BorderLayout.NORTH)
        lblPlayerName.font = Font("Tahoma", Font.PLAIN, 14)
        lblPlayerName.horizontalAlignment = SwingConstants.CENTER

        panel.add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = MigLayout("", "[grow][]", "[][][][][]")
        panelCenter.add(avatar, "cell 0 0 3 1,alignx center")
        avatar.preferredSize = Dimension(150, 150)
        panelCenter.add(panelX01, "cell 0 1 2 1,grow")
        panelCenter.add(panelGolf, "cell 0 2 2 1,grow")
        panelCenter.add(panelClock, "cell 0 3 2 1,grow")
    }

    fun init(player: PlayerEntity)
    {
        this.player = player

        lblPlayerName.text = player.name

        //Only show this for AIs
        btnEdit.isVisible = player.isAi()
        btnRunSimulation.isVisible = player.isAi()

        btnDelete.isEnabled = true
        btnAchievements.isEnabled = true

        avatar.isVisible = true
        panelX01.init(player)
        panelGolf.init(player)
        panelClock.init(player)
        avatar.init(player, true)
    }

    fun clear()
    {
        this.player = null
        lblPlayerName.text = ""
        btnEdit.isVisible = false
        btnRunSimulation.isVisible = false
        avatar.isVisible = false
        panelX01.isVisible = false
        panelGolf.isVisible = false
        panelClock.isVisible = false

        btnDelete.isEnabled = false
        btnAchievements.isEnabled = false
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnEdit -> AIConfigurationDialog.amendPlayer(player!!)
            btnDelete -> confirmAndDeletePlayer()
            btnRunSimulation -> AISimulationSetup(player!!).isVisible = true
            btnAchievements ->
            {
                val scrn = ScreenCache.get<PlayerAchievementsScreen>()
                scrn.player = player
                scrn.previousScrn = ScreenCache.get<PlayerManagementScreen>()

                ScreenCache.switch(scrn)
            }
        }
    }

    private fun confirmAndDeletePlayer()
    {
        val option = DialogUtil.showQuestion("Are you sure you want to delete ${player!!.name}?", false)
        if (option == JOptionPane.YES_OPTION)
        {
            val timestamp = Timestamp(System.currentTimeMillis())
            player!!.dtDeleted = timestamp
            player!!.saveToDatabase()

            //Re-initialise the screen so it updates
            val screen = ScreenCache.get<PlayerManagementScreen>()
            screen.initialise()
        }
    }
}
