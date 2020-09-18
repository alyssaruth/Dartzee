package dartzee.screen.game.scorer

import dartzee.dartzee.DartzeeRoundResult
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.DartzeePlayerState
import dartzee.game.state.PlayerStateListener
import dartzee.screen.game.dartzee.GamePanelDartzee
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

    private fun getTotalScore(): Int
    {
        val scores = model.getColumnValues(SCORE_COLUMN)

        val lastScore = scores.findLast { it != null } ?: 0
        return lastScore as Int
    }

    override fun stateChanged(state: DartzeePlayerState)
    {

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

    fun setResult(dartzeeRoundResult: DartzeeRoundResult)
    {
        model.setValueAt(dartzeeRoundResult, model.rowCount - 1, RULE_COLUMN)

        val newScore = dartzeeRoundResult.score + getTotalScore()
        model.setValueAt(newScore, model.rowCount - 1, SCORE_COLUMN)
        lblResult.text = "$newScore"

        val maxScore = getMaxScoreSoFar() ?: dartzeeRoundResult.score
        tableScores.getColumn(SCORE_COLUMN).cellRenderer = DartzeeScoreRenderer(maxScore)
        tableScores.repaint()
    }

    private fun getMaxScoreSoFar() = model.getColumnValues(SCORE_COLUMN).filterIsInstance<Int>().max()

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