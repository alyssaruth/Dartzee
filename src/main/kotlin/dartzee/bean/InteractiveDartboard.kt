package dartzee.bean

import dartzee.core.util.getParentWindow
import dartzee.`object`.ColourWrapper
import dartzee.`object`.DartboardSegment
import dartzee.utils.getColourWrapperFromPrefs
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

class InteractiveDartboard(colourWrapper: ColourWrapper = getColourWrapperFromPrefs()) : PresentationDartboard(colourWrapper, true), MouseMotionListener
{
    private var lastHoveredSegment: DartboardSegment? = null

    init
    {
        addMouseMotionListener(this)
    }

    fun highlightDartboard(pt: Point, customGraphics: Graphics? = null)
    {
        val hoveredSegment = getSegmentForPoint(pt)
        if (hoveredSegment == lastHoveredSegment)
        {
            //Nothing to do
            return
        }

        val graphics = customGraphics as? Graphics2D ?: graphics as Graphics2D

        lastHoveredSegment?.let { paintSegment(it, graphics, false) }

        lastHoveredSegment = hoveredSegment
        paintSegment(hoveredSegment, graphics, true)
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