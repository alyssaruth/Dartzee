package dartzee.screen.game.dartzee

import dartzee.dartzee.DartzeeRoundResult

interface IDartzeeCarouselListener
{
    fun hoverChanged(segmentStatus: SegmentStatus)
    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}