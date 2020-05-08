package dartzee.screen.player

import dartzee.achievements.getBestGameAchievement
import dartzee.bean.PlayerAvatar
import dartzee.core.bean.WrapLayout
import dartzee.core.obj.HashMapCount
import dartzee.core.util.DialogUtil
import dartzee.core.util.getSqlDateNow
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AISimulationSetup
import dartzee.stats.getGameCounts
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class PlayerManagementPanel : JPanel(), ActionListener
{
    private var player: PlayerEntity? = null

    private val btnEdit = JButton("Edit")
    private val btnDelete = JButton("Delete")
    val lblPlayerName = JLabel("")
    private val panelNorth = JPanel()
    private val avatar = PlayerAvatar()
    private val panelCenter = JPanel()
    private val btnRunSimulation = JButton("Run Simulation")

    init
    {
        layout = BorderLayout(0, 0)

        val panelOptions = JPanel()
        add(panelOptions, BorderLayout.SOUTH)

        btnEdit.icon = ImageIcon(javaClass.getResource("/buttons/amend.png"))
        btnEdit.font = Font("Tahoma", Font.PLAIN, 16)
        panelOptions.add(btnEdit)
        btnRunSimulation.font = Font("Tahoma", Font.PLAIN, 16)
        btnRunSimulation.icon = ImageIcon(javaClass.getResource("/buttons/stats.png"))

        panelOptions.add(btnRunSimulation)
        btnDelete.font = Font("Tahoma", Font.PLAIN, 16)
        btnDelete.icon = ImageIcon(javaClass.getResource("/buttons/delete.png"))
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
        lblPlayerName.font = ResourceCache.BASE_FONT.deriveFont(Font.BOLD, 20f)
        lblPlayerName.horizontalAlignment = SwingConstants.CENTER

        add(panelCenter, BorderLayout.CENTER)
        panelCenter.border = EmptyBorder(10, 0, 0, 0)
        panelCenter.layout = WrapLayout()
    }

    fun refresh(player: PlayerEntity?)
    {
        this.player = player

        lblPlayerName.text = player?.name ?: ""

        //Only show this for AIs
        btnEdit.isVisible = player?.isAi() == true
        btnRunSimulation.isVisible = player?.isAi() == true

        btnDelete.isVisible = player != null
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
        val counts = getGameCounts(player)
        val achievements = AchievementEntity.retrieveAchievements(player.rowId)

        panelCenter.add(makeStatsButton(player, counts, achievements, GameType.X01))
        panelCenter.add(makeStatsButton(player, counts, achievements, GameType.GOLF))
        panelCenter.add(makeStatsButton(player, counts, achievements, GameType.ROUND_THE_CLOCK))
        panelCenter.add(PlayerAchievementsButton(player, achievements))
    }

    private fun makeStatsButton(player: PlayerEntity, gameCounts: HashMapCount<GameType>, achievements: List<AchievementEntity>, gameType: GameType): PlayerStatsButton
    {
        val gamesPlayed = gameCounts.getCount(gameType)

        val achievement = achievements.find { it.achievementRef == getBestGameAchievement(gameType)?.achievementRef }
        val bestScore: Int = achievement?.achievementCounter ?: 0

        return PlayerStatsButton(player, gameType, gamesPlayed, bestScore)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        val selectedPlayer = player ?: return
        when (arg0.source)
        {
            btnEdit -> InjectedThings.playerManager.amendPlayer(selectedPlayer)
            btnDelete -> confirmAndDeletePlayer(selectedPlayer)
            btnRunSimulation -> InjectedThings.playerManager.runSimulation(selectedPlayer)
        }
    }

    private fun confirmAndDeletePlayer(selectedPlayer: PlayerEntity)
    {
        val option = DialogUtil.showQuestion("Are you sure you want to delete ${selectedPlayer.name}?", false)
        if (option == JOptionPane.YES_OPTION)
        {
            selectedPlayer.dtDeleted = getSqlDateNow()
            selectedPlayer.saveToDatabase()

            //Re-initialise the screen so it updates
            ScreenCache.get<PlayerManagementScreen>().initialise()
        }
    }
}
