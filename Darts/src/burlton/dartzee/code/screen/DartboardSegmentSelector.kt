package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.ColourWrapper
import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.getColourForPointAndSegment
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent

class DartboardSegmentSelector: Dartboard(500, 500)
{
    private val selectedSegments = hashSetOf<DartboardSegmentKt>()

    init
    {
        scoreLabelColor = Color.BLACK
        renderScoreLabels = true
    }

    fun paintDartboard()
    {
        val wireframe = ColourWrapper(DartsColour.TRANSPARENT)
        wireframe.edgeColour = Color.BLACK
        super.paintDartboard(wireframe, true, cached=false)
    }

    override fun dartThrown(pt: Point)
    {
        val segment = getSegmentForPoint(pt)
        if (segment.isMiss())
        {
            return
        }

        if (selectedSegments.contains(segment))
        {
            selectedSegments.remove(segment)

            colourSegment(segment, DartsColour.TRANSPARENT)
        }
        else
        {
            selectedSegments.add(segment)

            val col = getColourForPointAndSegment(null, segment, false, DEFAULT_COLOUR_WRAPPER)!!
            colourSegment(segment, col)
        }
    }

    private fun colourSegment(segment: DartboardSegmentKt, col: Color)
    {
        val pointsForCurrentSegment = segment.points
        for (i in pointsForCurrentSegment.indices)
        {
            val pt = pointsForCurrentSegment[i]
            if (!segment.isEdgePoint(pt))
            {
                colourPoint(pt, col)
            }
        }

        dartboardLabel.repaint()
    }

    override fun mouseMoved(arg0: MouseEvent) {}
}