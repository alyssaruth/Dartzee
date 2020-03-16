package dartzee.screen.game

import dartzee.db.*
import dartzee.screen.game.golf.MatchStatisticsPanelGolf
import dartzee.screen.game.rtc.MatchStatisticsPanelRoundTheClock
import dartzee.screen.game.scorer.MatchScorer
import dartzee.screen.game.x01.MatchStatisticsPanelX01
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
class MatchSummaryPanel(val match: DartsMatchEntity) : PanelWithScorers<MatchScorer>(), ActionListener
{
    private val hmPlayerIdToScorer = mutableMapOf<String, MatchScorer>()
    private val gameTabs = mutableListOf<DartsGamePanel<*, *>>()

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

    fun init(playersInStartingOrder: List<PlayerEntity>)
    {
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
            val scorer = assignScorer(player, "")
            hmPlayerIdToScorer[player.rowId] = scorer
            scorer.setMatch(match)
        }
    }


    fun addParticipant(localId: Long, participant: ParticipantEntity)
    {
        val playerId = participant.playerId
        val scorer = hmPlayerIdToScorer[playerId]!!

        val row = arrayOf(localId, participant, participant, participant)
        scorer.addRow(row)
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
        statsPanel?.let { statsPanel ->
            val states = gameTabs.map { it.getPlayerStates() }.flatten()
            statsPanel.showStats(states)
        }
    }

    override fun factoryScorer() = MatchScorer()

    fun addGameTab(tab: DartsGamePanel<*, *>)
    {
        gameTabs.add(tab)
    }

    private fun factoryStatsPanel(): GameStatisticsPanel?
    {
        return when (match.gameType)
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
