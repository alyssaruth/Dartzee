package dartzee.bean

import dartzee.core.util.getParentWindow
import dartzee.`object`.ColourWrapper
import dartzee.`object`.DartboardSegment
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getHighlightedColour
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

class InteractiveDartboard(colourWrapper: ColourWrapper = getColourWrapperFromPrefs()) : PresentationDartboard(colourWrapper, true), MouseMotionListener
{
    private var hoveredSegment: DartboardSegment? = null
    private var allowInteraction = true

    init
    {
        addMouseMotionListener(this)
    }

    fun highlightDartboard(pt: Point)
    {
        if (!allowInteraction) {
            return
        }

        val newHoveredSegment = getSegmentForPoint(pt)
        if (hoveredSegment == newHoveredSegment)
        {
            //Nothing to do
            return
        }

        hoveredSegment?.let(::revertOverriddenSegmentColour)

        if (newHoveredSegment.isMiss())
        {
            hoveredSegment = null
        }
        else
        {
            hoveredSegment = newHoveredSegment
            overrideSegmentColour(newHoveredSegment, getHighlightedColour(colourWrapper.getColour(newHoveredSegment)))
        }
    }

    fun stopInteraction()
    {
        hoveredSegment?.let(::revertOverriddenSegmentColour)
        hoveredSegment = null

        allowInteraction = false
    }

    fun allowInteraction()
    {
        allowInteraction = true
    }

    override fun mouseMoved(arg0: MouseEvent)
    {
        if (getParentWindow()?.isFocused == true)
        {
            highlightDartboard(arg0.point)
        }
    }

    override fun mouseDragged(arg0: MouseEvent) {}
}