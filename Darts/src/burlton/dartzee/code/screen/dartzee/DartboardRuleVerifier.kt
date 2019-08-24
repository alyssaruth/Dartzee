package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.screen.Dartboard
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

    override fun colourSegment(segment: DartboardSegment, col: Color)
    {
        if (!validSegments.contains(segment))
        {
            super.colourSegment(segment, Color.GRAY)
        }
        else
        {
            super.colourSegment(segment, col)
        }
    }
}