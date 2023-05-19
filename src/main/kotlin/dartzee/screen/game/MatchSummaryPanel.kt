package dartzee.screen.game

import dartzee.core.util.runOnEventThread
import dartzee.db.DartsMatchEntity
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.PlayerStateListener
import dartzee.screen.game.scorer.MatchScorer
import java.awt.BorderLayout
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The first tab displayed for any match. Provides a summary of the players' overall scores with (hopefully) nice graphs and stuff
 */
class MatchSummaryPanel<PlayerState: AbstractPlayerState<PlayerState>>(
    val match: DartsMatchEntity,
    private val statsPanel: AbstractGameStatisticsPanel<PlayerState>) : PanelWithScorers<MatchScorer>(),
    PlayerStateListener<PlayerState>
{
    private val gameTabs = CopyOnWriteArrayList<DartsGamePanel<*, PlayerState>>()

    init
    {
        panelCenter.add(statsPanel, BorderLayout.CENTER)
    }

    fun addParticipant(localId: Long, state: PlayerState)
    {
        val participant = state.wrappedParticipant
        val scorer = findOrAssignScorer(participant)

        val row = arrayOf(localId, participant, participant, participant)
        scorer.addRow(row)

        state.addListener(this)
    }

    fun getAllParticipants(): List<IWrappedParticipant>
    {
        val states = gameTabs.map { it.getPlayerStates() }.flatten()
        return states.map { it.wrappedParticipant }
    }

    private fun findOrAssignScorer(participant: IWrappedParticipant) =
        scorersOrdered.find { it.participant.getUniqueParticipantName() == participant.getUniqueParticipantName() } ?: assignScorer(participant)

    private fun updateStats()
    {
        scorersOrdered.forEach { it.updateResult() }

        val states = gameTabs.map { it.getPlayerStates() }.flatten()
        statsPanel.showStats(states)
    }

    override fun factoryScorer(participant: IWrappedParticipant) = MatchScorer(participant, match)

    fun addGameTab(tab: DartsGamePanel<*, PlayerState>)
    {
        gameTabs.add(tab)
    }

    override fun stateChanged(state: PlayerState)
    {
        runOnEventThread { updateStats() }
    }
}
