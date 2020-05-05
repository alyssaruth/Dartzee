package dartzee.screen

import dartzee.bean.PlayerAvatar
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetup
import dartzee.screen.stats.player.PlayerAchievementsScreen
import dartzee.stats.ParticipantStats
import dartzee.stats.PlayerSummaryStats
import dartzee.utils.DatabaseUtil
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
    }

    fun refresh(player: PlayerEntity?)
    {
        this.player = player

        lblPlayerName.text = player?.name ?: ""

        //Only show this for AIs
        btnEdit.isVisible = player?.isAi() == true
        btnRunSimulation.isVisible = player?.isAi() == true

        btnDelete.isEnabled = player != null
        btnAchievements.isEnabled = player != null

        panelCenter.removeAll()

        player?.let {
            avatar.init(player, true)
            panelCenter.add(avatar, "cell 0 0 3 1,alignx center")
            addSummaryPanels(player)
        }

        repaint()
        revalidate()
    }

    private fun addSummaryPanels(player: PlayerEntity)
    {
        val stats = runSql(player)

        panelCenter.add(makeSummaryPanel(player, stats, GameType.X01), "cell 0 1 2 1,grow")
        panelCenter.add(makeSummaryPanel(player, stats, GameType.GOLF), "cell 0 2 2 1,grow")
        panelCenter.add(makeSummaryPanel(player, stats, GameType.ROUND_THE_CLOCK), "cell 0 3 2 1,grow")
    }

    private fun runSql(player: PlayerEntity): List<ParticipantStats>
    {
        val list = mutableListOf<ParticipantStats>()

        val query = "SELECT g.GameType, pt.FinishingPosition, pt.FinalScore FROM Participant pt, Game g WHERE pt.GameId = g.RowId AND pt.PlayerId = '${player.rowId}'"
        DatabaseUtil.executeQuery(query).use { rs ->
            while (rs.next())
            {
                val gameType = GameType.valueOf(rs.getString("GameType"))
                val finishingPosition = rs.getInt("FinishingPosition")
                val finalScore = rs.getInt("FinalScore")

                list.add(ParticipantStats(gameType, finalScore, finishingPosition))
            }
        }

        return list.toList()
    }

    private fun makeSummaryPanel(player: PlayerEntity, participantStats: List<ParticipantStats>, gameType: GameType): PlayerSummaryPanel
    {
        val filteredPts = participantStats.filter { it.gameType == gameType }

        val gamesPlayed = filteredPts.size
        val gamesWon = filteredPts.count { it.finishingPosition == 1 }

        val bestScore: Int = filteredPts.filter { it.finalScore > -1 }.minBy { it.finalScore }?.finalScore ?: 0

        val stats = PlayerSummaryStats(gamesPlayed, gamesWon, bestScore)
        return PlayerSummaryPanel(player, gameType, stats)
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
