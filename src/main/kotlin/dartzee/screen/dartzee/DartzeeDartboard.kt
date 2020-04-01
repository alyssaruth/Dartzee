package dartzee.screen.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.screen.Dartboard
import dartzee.screen.game.dartzee.SegmentStatus
import java.awt.Color

class DartzeeDartboard(width: Int = 400, height: Int = 400): Dartboard(width, height)
{
    var segmentStatus: SegmentStatus? = SegmentStatus(emptySet(), emptySet())

    fun refreshValidSegments(segmentStatus: SegmentStatus?)
    {
        this.segmentStatus = segmentStatus

        getAllSegments().forEach{
            colourSegment(it, false)
        }
    }

    override fun shouldActuallyHighlight(segment: DartboardSegment): Boolean {
        val status = segmentStatus
        return status == null || status.validSegments.contains(segment)
    }

    override fun colourSegment(segment: DartboardSegment, col: Color)
    {
        val status = segmentStatus
        if (status == null)
        {
            super.colourSegment(segment, col)
        }
        else if (status.scoringSegments.contains(segment))
        {
            val newCol = Color(col.red / 2, 255, col.blue / 2)
            super.colourSegment(segment, newCol)
        }
        else if (isValidSegment(status, segment))
        {
            val newCol = Color(col.red, col.green, col.blue, 20)
            super.colourSegment(segment, newCol)
        }
        else
        {
            val newCol = Color(255, col.green, col.blue)
            super.colourSegment(segment, newCol)
        }
    }
    private fun isValidSegment(status: SegmentStatus, segment: DartboardSegment): Boolean
    {
        val validBecauseMiss =  status.validSegments.any { it.isMiss() } && segment.isMiss()

        return status.validSegments.contains(segment) || validBecauseMiss
    }
}