package burlton.dartzee.code.screen.game.scorer

import burlton.dartzee.code.dartzee.DartzeeRoundResult

private const val RULE_COLUMN = 3
private const val SCORE_COLUMN = 4

class DartsScorerDartzee: DartsScorer()
{
    override fun getTotalScore(): Int
    {
        val scores = model.getColumnValues(SCORE_COLUMN)

        val lastScore = scores.findLast { it != null } ?: 0
        return lastScore as Int
    }

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, RULE_COLUMN) != null

    override fun getNumberOfColumns() = 5

    override fun initImpl(gameParams: String?)
    {
        for (i in 0 until RULE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }

        tableScores.getColumn(RULE_COLUMN).cellRenderer = DartzeeRoundResultRenderer()
    }

    fun setResult(dartzeeRoundResult: DartzeeRoundResult, score: Int? = null)
    {
        model.setValueAt(dartzeeRoundResult, model.rowCount - 1, RULE_COLUMN)

        if (score != null)
        {
            val newScore = score + getTotalScore()
            model.setValueAt(newScore, model.rowCount - 1, SCORE_COLUMN)
            lblResult.text = "$newScore"
            lblResult.isVisible = true
        }
    }
}