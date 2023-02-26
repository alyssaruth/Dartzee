package dartzee.screen

import dartzee.`object`.ColourWrapper
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getColourForSegment
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent

class DartboardSegmentSelector(width: Int = 500, height: Int = 500): Dartboard(width, height)
{
    var selectedSegments = mutableSetOf<DartboardSegment>()

    private var lastDraggedSegment: DartboardSegment? = null

    init
    {
        scoreLabelColor = Color.BLACK
        renderScoreLabels = true
    }

    fun initState(initialSelection: Set<DartboardSegment>)
    {
        initialSelection.forEach { dataSegment ->
            selectedSegments.add(dataSegment)

            val col = getColourForSegment(dataSegment, DEFAULT_COLOUR_WRAPPER)
            colourSegment(dataSegment, col)
        }
    }

    fun selectAll()
    {
        val unselectedSegments = getAllNonMissSegments() - selectedSegments
        toggleAll(unselectedSegments)
    }

    fun selectNone()
    {
        toggleAll(selectedSegments.toList())
    }

    private fun toggleAll(segments: List<DartboardSegment>)
    {
        segments.forEach(::toggleSegment)
    }


    fun paintDartboard()
    {
        val wireframe = ColourWrapper(DartsColour.TRANSPARENT)
        wireframe.edgeColour = Color.BLACK
        super.paintDartboard(wireframe)
    }

    override fun dartThrown(pt: Point)
    {
        val segment = getDataSegmentForPoint(pt)
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

            val col = getColourForSegment(segment, DEFAULT_COLOUR_WRAPPER)
            colourSegment(segment, col)
        }
    }

    override fun mouseMoved(arg0: MouseEvent)
    {
        //Do nothing
    }
    override fun mouseDragged(arg0: MouseEvent)
    {
        val segment = getDataSegmentForPoint(arg0.point)
        if (segment == lastDraggedSegment)
        {
            return
        }

        toggleSegment(segment)
    }

}