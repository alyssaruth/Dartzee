package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRoundResult

interface IDartzeeTileListener
{
    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}