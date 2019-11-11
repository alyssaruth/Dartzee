package burlton.dartzee.code.screen.game.scorer

private const val RULE_COLUMN = 3

class DartsScorerDartzee: DartsScorer()
{
    override fun getTotalScore() = 5

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, RULE_COLUMN) != null

    override fun getNumberOfColumns() = 5

    override fun initImpl(gameParams: String?)
    {
        for (i in 0 until RULE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

}