package dartzee.bean

import dartzee.core.util.getParentWindow
import dartzee.`object`.ColourWrapper
import dartzee.`object`.DartboardSegment
import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getHighlightedColour
import java.awt.Point
import java.awt.event.MouseEvent

class InteractiveDartboard(colourWrapper: ColourWrapper = getColourWrapperFromPrefs()) :
    PresentationDartboard(colourWrapper, true), IMouseListener {
    private var hoveredSegment: DartboardSegment? = null
    private var allowInteraction = true

    init {
        addMouseMotionListener(this)
    }

    fun highlightDartboard(pt: Point) {
        if (!allowInteraction) {
            return
        }

        val distance = pt.distance(computeCenter())
        val newHoveredSegment =
            if (distance > computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO) {
                null
            } else getSegmentForPoint(pt)

        if (hoveredSegment == newHoveredSegment) {
            // Nothing to do
            return
        }

        hoveredSegment?.let(::revertOverriddenSegmentColour)

        if (newHoveredSegment != null && isValidSegmentForHover(newHoveredSegment)) {
            hoveredSegment = newHoveredSegment
            overrideSegmentColour(
                newHoveredSegment,
                getHighlightedColour(defaultColourForSegment(newHoveredSegment))
            )
        } else {
            hoveredSegment = null
        }
    }

    private fun isValidSegmentForHover(segment: DartboardSegment): Boolean {
        val statuses = segmentStatuses
        if (statuses != null) {
            return statuses.validSegments.contains(segment) ||
                (statuses.allowsMissing() && segment.isMiss())
        }

        return !segment.isMiss()
    }

    fun stopInteraction() {
        clearHover()

        allowInteraction = false
    }

    fun allowInteraction() {
        allowInteraction = true
    }

    fun clearHover() {
        hoveredSegment?.let(::revertOverriddenSegmentColour)
        hoveredSegment = null
    }

    override fun mouseMoved(e: MouseEvent) {
        if (getParentWindow()?.isFocused == true) {
            highlightDartboard(e.point)
        }
    }
}
