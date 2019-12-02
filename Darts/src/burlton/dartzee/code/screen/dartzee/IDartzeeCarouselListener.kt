package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRoundResult

interface IDartzeeCarouselListener
{
    fun hoverChanged(validSegments: List<DartboardSegment>)
    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}