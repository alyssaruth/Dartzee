package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.ColourWrapper
import burlton.dartzee.code.`object`.DEFAULT_COLOUR_WRAPPER
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.getColourForPointAndSegment
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent

class DartboardSegmentSelector(width: Int = 500, height: Int = 500): Dartboard(width, height)
{
    var selectedSegments = hashSetOf<DartboardSegment>()

    private var lastDraggedSegment: DartboardSegment? = null

    init
    {
        scoreLabelColor = Color.BLACK
        renderScoreLabels = true
    }

    fun initState(initialSelection: HashSet<DartboardSegment>)
    {
        initialSelection.forEach{
            val mySegment = getSegment(it.score, it.type) ?: return
            selectedSegments.add(mySegment)

            val col = getColourForPointAndSegment(null, mySegment, false, DEFAULT_COLOUR_WRAPPER)!!
            colourSegment(mySegment, col)
        }
    }


    fun paintDartboard()
    {
        val wireframe = ColourWrapper(DartsColour.TRANSPARENT)
        wireframe.edgeColour = Color.BLACK
        super.paintDartboard(wireframe, true, cached=false)
    }

    override fun dartThrown(pt: Point)
    {
        val segment = getSegmentForPoint(pt, false)
        toggleSegment(segment)
    }

    private fun toggleSegment(segment: DartboardSegment)
    {
        lastDraggedSegment = segment
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

    override fun mouseMoved(arg0: MouseEvent)
    {
        //Do nothing
    }
    override fun mouseDragged(arg0: MouseEvent)
    {
        val segment = getSegmentForPoint(arg0.point, false)
        if (segment == lastDraggedSegment)
        {
            return
        }

        toggleSegment(segment)
    }

}