package dartzee.screen.game.dartzee

import dartzee.dartzee.DartzeeRoundResult
import dartzee.screen.game.SegmentStatuses

interface IDartzeeCarouselListener {
    fun hoverChanged(segmentStatuses: SegmentStatuses)

    fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
}
