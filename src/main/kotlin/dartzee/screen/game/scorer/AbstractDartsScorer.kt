package dartzee.screen.game.scorer

import dartzee.achievements.AbstractAchievement
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.PlayerStateListener
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.border.EmptyBorder

const val SCORER_WIDTH = 210

abstract class AbstractDartsScorer<PlayerState : AbstractPlayerState<PlayerState>>(
    participant: IWrappedParticipant
) : AbstractScorer(participant), PlayerStateListener<PlayerState>, IDartsScorerTable {
    private val overlays = mutableListOf<AchievementOverlay>()

    init {
        preferredSize = Dimension(SCORER_WIDTH, 700)
        panelAvatar.border = EmptyBorder(5, 30, 5, 30)
    }

    override fun stateChanged(state: PlayerState) {
        runOnEventThreadBlocking {
            model.clear()
            updateSelectionState(state)

            stateChangedImpl(state)

            tableScores.scrollToBottom()
            tableScores.repaint()
            lblResult.repaint()
            repaint()
        }
    }

    protected fun setScoreAndFinishingPosition(state: PlayerState) {
        val scoreSoFar = state.getScoreSoFar()
        lblResult.text =
            if (state.hasResigned()) "RESIGNED" else if (scoreSoFar > 0) "$scoreSoFar" else ""
        updateResultColourForPosition(state.wrappedParticipant.participant.finishingPosition)
    }

    private fun updateSelectionState(state: PlayerState) {
        val currentIndividual = state.currentIndividual()
        lblName.text =
            state.wrappedParticipant.getParticipantNameHtml(state.isActive, currentIndividual)
        lblAvatar.setSelected(state.isActive, state.currentRoundNumber())
    }

    fun gameFinished() {
        lblName.text = participant.getParticipantNameHtml(false)
        lblAvatar.setSelected(false, -1, true)
    }

    protected open fun stateChangedImpl(state: PlayerState) {}

    fun achievementUnlocked(achievement: AbstractAchievement, playerId: String) {
        val playerName = getPlayerNameIfNecessary(playerId)
        val overlay = AchievementOverlay(this, achievement, playerName)

        overlays.add(overlay)

        // Let's just only ever have one thing at a time on display. Actually layering them
        // sometimes worked but
        // sometimes caused weird bollocks when things happened close together
        panelCenter.removeAll()
        panelCenter.add(overlay, BorderLayout.CENTER)
        panelCenter.revalidate()
        panelCenter.repaint()
    }

    private fun getPlayerNameIfNecessary(playerId: String): String? {
        if (participant.individuals.size == 1) {
            return null
        }

        val pt = participant.individuals.first { it.playerId == playerId }
        return pt.getPlayerName()
    }

    fun achievementClosed(overlay: AchievementOverlay) {
        panelCenter.removeAll()
        overlays.remove(overlay)

        // If there are more overlays stacked 'beneath', show the next one of them now
        if (overlays.isNotEmpty()) {
            panelCenter.add(overlays.last(), BorderLayout.CENTER)
        } else {
            allAchievementsClosed()
        }

        panelCenter.revalidate()
        panelCenter.repaint()
    }

    open fun allAchievementsClosed() {
        panelCenter.add(tableScores)
    }
}
