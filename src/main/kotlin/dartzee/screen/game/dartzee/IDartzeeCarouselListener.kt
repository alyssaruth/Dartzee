package dartzee.screen.game.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.dartzee.DartzeeRoundResult

interface IDartzeeCarouselListener
{
    fun hoverChanged(validSegments: List<DartboardSegment>)
    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}