package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.PlayerAvatar
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ai.AIConfigurationDialog
import burlton.dartzee.code.screen.ai.AISimulationSetup
import burlton.dartzee.code.screen.stats.player.PlayerAchievementsScreen
import burlton.desktopcore.code.util.DialogUtil
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
    private val panelX01 = PlayerSummaryPanel(GAME_TYPE_X01)
    private val panelGolf = PlayerSummaryPanel(GAME_TYPE_GOLF)
    private val panelClock = PlayerSummaryPanel(GAME_TYPE_ROUND_THE_CLOCK)
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
                val scrn = ScreenCache.getScreen(PlayerAchievementsScreen::class.java)
                scrn.setPlayer(player!!)
                scrn.previousScrn = ScreenCache.getPlayerManagementScreen()

                ScreenCache.switchScreen(scrn)
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
            val screen = ScreenCache.getPlayerManagementScreen()
            screen.initialise()
        }
    }
}
