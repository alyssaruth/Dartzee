package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.DartboardSegment

interface IDartzeeCarouselHoverListener
{
    fun hoverChanged(validSegments: List<DartboardSegment>)
}