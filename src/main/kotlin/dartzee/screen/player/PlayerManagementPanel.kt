package dartzee.screen.player

import dartzee.bean.PlayerAvatar
import dartzee.core.bean.WrapLayout
import dartzee.core.util.DialogUtil
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetup
import dartzee.stats.ParticipantSummary
import dartzee.stats.getParticipantSummaries
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.sql.Timestamp
import javax.swing.*
import javax.swing.border.EmptyBorder

class PlayerManagementPanel : JPanel(), ActionListener
{
    private var player: PlayerEntity? = null

    private val btnEdit = JButton("Edit")
    private val btnDelete = JButton("Delete")
    private val lblPlayerName = JLabel("")
    private val panelNorth = JPanel()
    private val avatar = PlayerAvatar()
    private val panelCenter = JPanel()
    private val btnRunSimulation = JButton("Run Simulation")

    init
    {
        layout = BorderLayout(0, 0)

        val panelOptions = JPanel()
        add(panelOptions, BorderLayout.SOUTH)

        btnEdit.font = Font("Tahoma", Font.PLAIN, 16)
        panelOptions.add(btnEdit)
        btnRunSimulation.font = Font("Tahoma", Font.PLAIN, 16)

        panelOptions.add(btnRunSimulation)
        btnDelete.font = Font("Tahoma", Font.PLAIN, 16)
        panelOptions.add(btnDelete)

        btnEdit.addActionListener(this)
        btnRunSimulation.addActionListener(this)
        btnDelete.addActionListener(this)

        panelNorth.layout = BorderLayout(0, 0)
        val panelName = JPanel()
        panelName.border = EmptyBorder(10, 10, 10, 10)
        panelName.layout = BorderLayout(0, 0)
        panelName.add(lblPlayerName)
        panelNorth.add(panelName, BorderLayout.NORTH)
        val panelAvatar = JPanel()
        panelAvatar.add(avatar)
        panelNorth.add(panelAvatar)

        add(panelNorth, BorderLayout.NORTH)

        lblPlayerName.preferredSize = Dimension(0, 25)
        lblPlayerName.font = Font("Tahoma", Font.PLAIN, 14)
        lblPlayerName.horizontalAlignment = SwingConstants.CENTER

        add(panelCenter, BorderLayout.CENTER)
        panelCenter.border = EmptyBorder(10, 0, 0, 0)
        panelCenter.layout = WrapLayout()
    }

    fun refresh(player: PlayerEntity?)
    {
        this.player = player

        val text = player?.let { "<html><h1>${it.name}</h1></html>" }
        lblPlayerName.text = text ?: ""

        //Only show this for AIs
        btnEdit.isVisible = player?.isAi() == true
        btnRunSimulation.isVisible = player?.isAi() == true

        btnDelete.isEnabled = player != null
        avatar.isVisible = player != null

        panelCenter.removeAll()

        player?.let {
            avatar.init(player, true)
            addSummaryPanels(player)
        }

        repaint()
        revalidate()
    }

    private fun addSummaryPanels(player: PlayerEntity)
    {
        val stats = getParticipantSummaries(player)
        val achievements = AchievementEntity.retrieveAchievements(player.rowId)

        panelCenter.add(makeStatsButton(player, stats, GameType.X01))
        panelCenter.add(makeStatsButton(player, stats, GameType.GOLF))
        panelCenter.add(makeStatsButton(player, stats, GameType.ROUND_THE_CLOCK))
        panelCenter.add(PlayerAchievementsButton(player, achievements))
    }

    private fun makeStatsButton(player: PlayerEntity, participantStats: List<ParticipantSummary>, gameType: GameType): PlayerStatsButton
    {
        val filteredPts = participantStats.filter { it.gameType == gameType }

        val gamesPlayed = filteredPts.size
        val bestScore: Int = filteredPts.filter { it.finalScore > -1 }.minBy { it.finalScore }?.finalScore ?: 0

        return PlayerStatsButton(player, gameType, gamesPlayed, bestScore)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnEdit -> AIConfigurationDialog.amendPlayer(player!!)
            btnDelete -> confirmAndDeletePlayer()
            btnRunSimulation -> AISimulationSetup(player!!).isVisible = true
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
            val screen =
                ScreenCache.get<PlayerManagementScreen>()
            screen.initialise()
        }
    }
}
