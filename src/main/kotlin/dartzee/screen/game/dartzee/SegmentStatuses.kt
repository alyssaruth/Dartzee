package dartzee.screen.game.dartzee

import dartzee.`object`.DartboardSegment

enum class SegmentStatus {
    SCORING, VALID, INVALID
}

data class SegmentStatuses(val scoringSegments: Set<DartboardSegment>, val validSegments: Set<DartboardSegment>)
{
    constructor(scoringSegments: List<DartboardSegment>, validSegments: List<DartboardSegment>): this(scoringSegments.toSet(), validSegments.toSet())
}

fun SegmentStatuses?.getSegmentStatus(segment: DartboardSegment): SegmentStatus
{
    if (this == null)
    {
        return SegmentStatus.SCORING
    }

    return when
    {
        scoringSegments.contains(segment) -> SegmentStatus.SCORING
        validSegments.contains(segment) -> SegmentStatus.VALID
        else -> SegmentStatus.INVALID
    }
}