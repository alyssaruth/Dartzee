package dartzee.screen.game.scorer

import dartzee.bean.IMouseListener
import dartzee.game.state.DartzeePlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.utils.factoryHighScoreResult
import java.awt.event.MouseEvent

private const val RULE_COLUMN = 3
private const val SCORE_COLUMN = 4

class DartsScorerDartzee(private val parent: GamePanelDartzee, participant: IWrappedParticipant) :
    AbstractDartsScorer<DartzeePlayerState>(participant), IMouseListener {
    init {
        lblAvatar.addMouseListener(this)
    }

    override fun stateChangedImpl(state: DartzeePlayerState) {
        setScoreAndFinishingPosition(state)

        state.completedRounds.forEachIndexed { ix, round ->
            addDartRound(round)

            val roundNumber = ix + 1
            val roundResult = state.roundResults.find { it.roundNumber == roundNumber }?.toDto()
            val cumulativeScore = state.getCumulativeScore(roundNumber)

            model.setValueAt(roundResult ?: factoryHighScoreResult(round), ix, RULE_COLUMN)
            model.setValueAt(cumulativeScore, ix, SCORE_COLUMN)
        }

        if (state.currentRound.isNotEmpty()) {
            addDartRound(state.currentRound)
        }

        tableScores.getColumn(SCORE_COLUMN).cellRenderer =
            DartzeeScoreRenderer(state.getPeakScore() ?: 0)
    }

    override fun getNumberOfColumns() = 5

    override fun initImpl() {
        for (i in 0 until RULE_COLUMN) {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }

        tableScores.getColumn(RULE_COLUMN).cellRenderer = DartzeeRoundResultRenderer()
    }

    fun togglePostGame(selected: Boolean) {
        lblName.text = participant.getParticipantNameHtml(selected)
        lblAvatar.setSelected(selected, -1, selected)
    }

    override fun mouseReleased(e: MouseEvent) {
        if (parent.gameEntity.isFinished()) {
            parent.scorerSelected(this)
        }
    }
}
