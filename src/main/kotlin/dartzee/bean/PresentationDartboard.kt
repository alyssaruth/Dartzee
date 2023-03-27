package dartzee.bean

import dartzee.`object`.ColourWrapper
import dartzee.`object`.IDartboard
import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getColourFromHashMap
import dartzee.utils.getColourWrapperFromPrefs
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import javax.swing.JLabel

class PresentationDartboard(private val colourWrapper: ColourWrapper = getColourWrapperFromPrefs()) : JLabel(), IDartboard
{
    override fun computeRadius() = 0.7 * minOf(width, height) / 2.0
    override fun computeCenter() = Point(width / 2, height / 2)

    override fun paint(g: Graphics)
    {
        super.paint(g)

        val graphics2D = g as Graphics2D

        paintOuterBoard(graphics2D)

        getAllPossibleSegments().forEach { segment ->
            val pts = getPointsForSegment(segment)
            val colour = getColourFromHashMap(segment, colourWrapper)

            graphics2D.paint = colour
            pts.forEach { graphics2D.drawLine(it.x, it.y, it.x, it.y) }
        }
    }

    private fun paintOuterBoard(g: Graphics2D)
    {
        g.paint = colourWrapper.missedBoardColour
        g.fillRect(0, 0, width, height)

        val borderSize = (computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO).toInt()
        val center = computeCenter()
        g.paint = colourWrapper.outerDartboardColour
        g.fillOval(center.x - borderSize, center.y - borderSize, borderSize * 2, borderSize * 2)
    }
}