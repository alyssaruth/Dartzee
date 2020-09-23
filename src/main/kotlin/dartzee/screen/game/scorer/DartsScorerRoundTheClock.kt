package dartzee.screen.game.scorer

import dartzee.`object`.DartNotThrown
import dartzee.game.ClockType
import dartzee.game.state.ClockPlayerState
import dartzee.screen.game.GamePanelPausable

class DartsScorerRoundTheClock(parent: GamePanelPausable<*, *>, private val clockType: ClockType) : DartsScorerPausable<ClockPlayerState>(parent)
{
    //Always start at 1. Bit of an abuse to stick this here, it just avoids having another hmPlayerNumber->X.
    private var clockTarget = 1
    private var currentClockTarget = 1

    override fun confirmCurrentRound()
    {
        clockTarget = currentClockTarget
    }

    override fun getNumberOfColumns(): Int
    {
        return 4 //3 darts, plus bonus for hitting three consecutive
    }

    override fun getNumberOfColumnsForAddingNewDart(): Int
    {
        return getNumberOfColumns() //They're all for containing darts
    }

    override fun initImpl()
    {
        for (i in 0..BONUS_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = RoundTheClockDartRenderer(clockType)
        }
    }

    fun incrementCurrentClockTarget()
    {
        currentClockTarget++
    }

    fun disableBrucey()
    {
        val row = model.rowCount - 1
        model.setValueAt(DartNotThrown(), row, BONUS_COLUMN)
    }

    companion object
    {
        private const val BONUS_COLUMN = 3
    }
}
