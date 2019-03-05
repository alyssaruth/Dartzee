package burlton.dartzee.code.screen.game

import burlton.dartzee.code.db.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

/**
 * The first tab displayed for any match. Provides a summary of the players' overall scores with (hopefully) nice graphs and stuff
 */
class MatchSummaryPanel : PanelWithScorers<MatchScorer>(), ActionListener
{
    private val hmPlayerIdToScorer = mutableMapOf<Long, MatchScorer>()
    private val participants = mutableListOf<ParticipantEntity>()
    private var match: DartsMatchEntity? = null

    private var statsPanel: GameStatisticsPanel? = null
    private val refreshPanel = JPanel()
    private val btnRefresh = JButton()

    init
    {
        refreshPanel.add(btnRefresh)
        btnRefresh.addActionListener(this)
        btnRefresh.preferredSize = Dimension(80, 80)
        btnRefresh.icon = ImageIcon(javaClass.getResource("/buttons/refresh.png"))
        btnRefresh.toolTipText = "Refresh stats"
    }

    fun init(match: DartsMatchEntity, playersInStartingOrder: MutableList<PlayerEntity>)
    {
        this.match = match

        val statsPanel = factoryStatsPanel()

        if (statsPanel != null)
        {
            statsPanel.gameParams = match.gameParams
            panelCenter.add(statsPanel, BorderLayout.CENTER)
            panelCenter.add(refreshPanel, BorderLayout.SOUTH)

            this.statsPanel = statsPanel
        }

        val totalPlayers = playersInStartingOrder.size
        initScorers(totalPlayers)

        for (player in playersInStartingOrder)
        {
            val playerId = player.rowId
            val scorer = assignScorer(player, hmPlayerIdToScorer, playerId, "")
            scorer!!.setMatch(match)
        }
    }


    fun addParticipant(gameId: Long, participant: ParticipantEntity)
    {
        val playerId = participant.playerId
        val scorer = hmPlayerIdToScorer[playerId]!!

        val row = arrayOf(gameId, participant, participant, participant)
        scorer.addRow(row)

        participants.add(participant)
    }

    fun updateTotalScores()
    {
        val scorers = hmPlayerIdToScorer.values
        for (scorer in scorers)
        {
            scorer.updateResult()
        }

        updateStats()
    }

    fun updateStats()
    {
        if (statsPanel != null && btnRefresh.isEnabled)
        {
            btnRefresh.isEnabled = false

            val participantsCopy = participants.toMutableList()
            val updateRunnable = Runnable{
                statsPanel!!.showStats(participantsCopy)
                btnRefresh.isEnabled = true
            }

            Thread(updateRunnable).start()
        }
    }

    override fun factoryScorer(): MatchScorer
    {
        return MatchScorer()
    }

    private fun factoryStatsPanel(): GameStatisticsPanel?
    {
        val type = match!!.gameType
        return when (type)
        {
            GAME_TYPE_X01 -> MatchStatisticsPanelX01()
            GAME_TYPE_GOLF -> MatchStatisticsPanelGolf()
            GAME_TYPE_ROUND_THE_CLOCK -> MatchStatisticsPanelRoundTheClock()
            else -> null
        }
    }

    override fun actionPerformed(e: ActionEvent)
    {
        updateStats()
    }
}
