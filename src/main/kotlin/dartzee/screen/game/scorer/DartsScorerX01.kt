package dartzee.screen.game.scorer

import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.X01PlayerState
import dartzee.`object`.CheckoutSuggester
import dartzee.screen.game.GamePanelPausable
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.SwingConstants

class DartsScorerX01(
    parent: GamePanelPausable<*, *>,
    gameParams: String,
    participant: IWrappedParticipant
) : AbstractDartsScorerPausable<X01PlayerState>(parent, participant) {
    private val lblStartingScore = JLabel(gameParams)

    init {
        lblStartingScore.horizontalAlignment = SwingConstants.CENTER
        lblStartingScore.font = Font("Trebuchet MS", Font.PLAIN, 16)
        panelNorth.add(lblStartingScore, BorderLayout.SOUTH)
    }

    override fun initImpl() {
        tableScores.getColumn(SCORE_COLUMN).cellRenderer = X01ScoreRenderer()
        for (i in 0 until SCORE_COLUMN) {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

    override fun stateChangedImpl(state: X01PlayerState) {
        state.completedRounds.forEachIndexed { ix, round ->
            addDartRound(round)

            val roundNumber = ix + 1
            val scoreRemaining = state.getRemainingScoreForRound(roundNumber)

            model.setValueAt(scoreRemaining, ix, SCORE_COLUMN)
        }

        if (state.currentRound.isNotEmpty()) {
            addDartRound(state.currentRound)
        }

        finalisePlayerResult(state)

        addCheckoutSuggestion(state)
    }

    private fun addCheckoutSuggestion(state: X01PlayerState) {
        if (getPaused() || !state.isActive) {
            return
        }

        val dartsRemaining = 3 - state.currentRound.size
        val currentScore = state.getRemainingScore()
        val checkout = CheckoutSuggester.suggestCheckout(currentScore, dartsRemaining) ?: return

        if (state.currentRound.isEmpty()) {
            addRow(makeEmptyRow())
        }

        checkout.forEach(::addDart)
    }

    override fun getNumberOfColumns() = SCORE_COLUMN + 1

    companion object {
        const val SCORE_COLUMN = 3
    }
}
