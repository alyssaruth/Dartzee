package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.getColourForPointAndSegment
import java.awt.Color

class DartboardRuleVerifier(width: Int = 300, height: Int = 300): Dartboard(width, height)
{
    private var validSegments = listOf<DartboardSegment>()

    fun refreshValidSegments(segments: List<DartboardSegment>)
    {
        this.validSegments = segments

        getAllSegments().forEach{
            colourSegment(it, false)
        }
    }

    override fun colourSegment(segment: DartboardSegment, highlight: Boolean)
    {
        val reallyHighlight = highlight && !segment.isMiss() && validSegments.contains(segment)
        val hoveredColour = getColourForPointAndSegment(null, segment, reallyHighlight, colourWrapper) ?: return
        colourSegment(segment, hoveredColour)
    }
    override fun colourSegment(segment: DartboardSegment, col: Color)
    {
        if (!isValidSegment(segment))
        {
            val newCol = Color(col.red, col.green, col.blue, 20)
            super.colourSegment(segment, newCol)
        }
        else
        {
            super.colourSegment(segment, col)
        }
    }
    private fun isValidSegment(segment: DartboardSegment): Boolean
    {
        val validBecauseMiss =  validSegments.any { it.isMiss() } && segment.isMiss()

        return validSegments.contains(segment) || validBecauseMiss
    }
}