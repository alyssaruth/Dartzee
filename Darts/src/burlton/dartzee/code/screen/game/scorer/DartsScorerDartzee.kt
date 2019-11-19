package burlton.dartzee.code.screen.game.scorer

import burlton.dartzee.code.screen.dartzee.DartzeeRoundResult

private const val RULE_COLUMN = 3
private const val SCORE_COLUMN = 4

class DartsScorerDartzee: DartsScorer()
{
    override fun getTotalScore(): Int
    {
        val rowCount = model.rowCount
        if (rowCount == 0)
        {
            return 0
        }

        return model.getValueAt(rowCount - 1, SCORE_COLUMN) as Int
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
        model.setValueAt(score, model.rowCount - 1, SCORE_COLUMN)
    }
}