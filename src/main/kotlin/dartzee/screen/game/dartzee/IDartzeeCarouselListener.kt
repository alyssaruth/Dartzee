package dartzee.screen.game.dartzee

import dartzee.dartzee.DartzeeRoundResult

interface IDartzeeCarouselListener
{
    fun hoverChanged(segmentStatuses: SegmentStatuses)
    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}