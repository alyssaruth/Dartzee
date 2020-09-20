package dartzee.screen.game.scorer

import dartzee.game.state.DartzeePlayerState
import dartzee.game.state.PlayerStateListener
import dartzee.logging.LoggingCode
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.utils.InjectedThings.logger
import dartzee.utils.factoryHighScoreResult
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

private const val RULE_COLUMN = 3
private const val SCORE_COLUMN = 4

class DartsScorerDartzee(private val parent: GamePanelDartzee): DartsScorer(), MouseListener, PlayerStateListener<DartzeePlayerState>
{
    init
    {
        lblAvatar.addMouseListener(this)
    }

    override fun stateChanged(state: DartzeePlayerState)
    {
        logger.info(LoggingCode("stateChanged"), "woop woop")

        //Here we go
        model.clear()

        state.completedRounds.forEachIndexed { ix, round ->
            round.forEach { addDart(it) }

            val roundNumber = ix + 1
            val roundResult = state.roundResults.find { it.roundNumber == roundNumber }?.toDto()
            val cumulativeScore = state.getCumulativeScore(roundNumber)

            model.setValueAt(roundResult ?: factoryHighScoreResult(round), model.rowCount - 1, RULE_COLUMN)
            model.setValueAt(cumulativeScore, model.rowCount - 1, SCORE_COLUMN)
        }

        state.currentRound.forEach { addDart(it) }

        tableScores.getColumn(SCORE_COLUMN).cellRenderer = DartzeeScoreRenderer(state.getPeakScore() ?: 0)
        tableScores.scrollToBottom()

        // Misc (will probably live below on DartsScorer eventually)
        val scoreSoFar = state.getScoreSoFar()
        lblResult.text = if (scoreSoFar > 0) "$scoreSoFar" else ""
        updateResultColourForPosition(state.pt.finishingPosition)

        tableScores.repaint()
        lblResult.repaint()
        repaint()
    }

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, RULE_COLUMN) != null

    override fun getNumberOfColumns() = 5

    override fun initImpl()
    {
        for (i in 0 until RULE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }

        tableScores.getColumn(RULE_COLUMN).cellRenderer = DartzeeRoundResultRenderer()
    }

    override fun mouseReleased(e: MouseEvent?)
    {
        if (parent.gameEntity.isFinished())
        {
            parent.scorerSelected(this)
        }
    }

    override fun mouseEntered(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseExited(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}
}