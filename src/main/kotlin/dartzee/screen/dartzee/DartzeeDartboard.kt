package dartzee.screen.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.`object`.GREEN_COLOUR_WRAPPER
import dartzee.`object`.RED_COLOUR_WRAPPER
import dartzee.screen.Dartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.DartsColour
import dartzee.utils.getColourFromHashMap
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
        val colourWithAlpha = Color(col.red, col.green, col.blue, 50)
        val colour = when {
            status == null -> col
            status.scoringSegments.contains(segment) -> getColourFromHashMap(segment, GREEN_COLOUR_WRAPPER)
            isValidSegment(status, segment) -> colourWithAlpha
            else -> getColourFromHashMap(segment, RED_COLOUR_WRAPPER)
        }

        super.colourSegment(segment, colour)
    }
    private fun isValidSegment(status: SegmentStatus, segment: DartboardSegment): Boolean
    {
        val validBecauseMiss =  status.validSegments.any { it.isMiss() } && segment.isMiss()

        return status.validSegments.contains(segment) || validBecauseMiss
    }
}