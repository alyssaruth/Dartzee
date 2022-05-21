package dartzee.screen.game

import dartzee.db.DartsMatchEntity
import dartzee.game.UniqueParticipantName
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.screen.game.scorer.MatchScorer
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
class MatchSummaryPanel<PlayerState: AbstractPlayerState<PlayerState>>(
    val match: DartsMatchEntity,
    private val statsPanel: AbstractGameStatisticsPanel<PlayerState>) : PanelWithScorers<MatchScorer>(), ActionListener
{
    private val hmParticipantNameToScorer = mutableMapOf<UniqueParticipantName, MatchScorer>()
    private val gameTabs = mutableListOf<DartsGamePanel<*, *, PlayerState>>()

    private val refreshPanel = JPanel()
    private val btnRefresh = JButton()

    init
    {
        panelCenter.add(statsPanel, BorderLayout.CENTER)
        panelCenter.add(refreshPanel, BorderLayout.SOUTH)

        refreshPanel.add(btnRefresh)
        btnRefresh.addActionListener(this)
        btnRefresh.preferredSize = Dimension(80, 80)
        btnRefresh.icon = ImageIcon(javaClass.getResource("/buttons/refresh.png"))
        btnRefresh.toolTipText = "Refresh stats"
    }

    fun finaliseParticipants()
    {
        finaliseScorers()
        revalidate()
    }

    fun addParticipant(localId: Long, participant: IWrappedParticipant)
    {
        val participantName = participant.getUniqueParticipantName()
        val scorer = assignScorer(participant)
        hmParticipantNameToScorer[participantName] = scorer

        val row = arrayOf(localId, participant, participant, participant)
        scorer.addRow(row)
    }

    fun updateTotalScores()
    {
        val scorers = hmParticipantNameToScorer.values
        for (scorer in scorers)
        {
            scorer.updateResult()
        }

        updateStats()
    }

    private fun updateStats()
    {
        val states = gameTabs.map { it.getPlayerStates() }.flatten()
        statsPanel.showStats(states)
    }

    override fun factoryScorer(participant: IWrappedParticipant) = MatchScorer(participant, match)

    fun addGameTab(tab: DartsGamePanel<*, *, PlayerState>)
    {
        gameTabs.add(tab)
    }

    override fun actionPerformed(e: ActionEvent)
    {
        updateStats()
    }
}
