package burlton.dartzee.code.screen.game

class DartsScorerDartzee: DartsScorer()
{
    override fun getTotalScore() = 5

    override fun rowIsComplete(rowNumber: Int) = true

    override fun getNumberOfColumns() = 4

    override fun initImpl(gameParams: String?)
    {
        //Fuck knows
    }

}