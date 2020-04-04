package dartzee.screen.game.dartzee

import dartzee.`object`.DartboardSegment

data class SegmentStatus(val scoringSegments: Set<DartboardSegment>, val validSegments: Set<DartboardSegment>)
{
    constructor(scoringSegments: List<DartboardSegment>, validSegments: List<DartboardSegment>): this(scoringSegments.toSet(), validSegments.toSet())
}