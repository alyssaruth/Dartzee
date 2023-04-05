package dartzee.bean

import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputedPoint
import dartzee.`object`.IDartboard
import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.computeEdgePoints
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getColourFromHashMap
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getNeighbours
import dartzee.utils.translatePoint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import javax.swing.JLabel

class PresentationDartboard(private val colourWrapper: ColourWrapper = getColourWrapperFromPrefs()) : JLabel(), IDartboard
{
    override fun computeRadius() = computeRadius(width, height)
    override fun computeCenter() = Point(width / 2, height / 2)

    fun interpretPoint(pt: ComputedPoint): Point
    {
        val newPoint = translatePoint(
            computeCenter(),
            pt.radius * computeRadius(),
            pt.angle
        )

        rationalisePoint(newPoint)

        val desiredSegment = pt.segment
        val candidatePoints = mutableSetOf(newPoint)
        while (candidatePoints.none { getSegmentForPoint(it) == desiredSegment })
        {
            val neighbours = candidatePoints.flatMap(::getNeighbours)
            candidatePoints.addAll(neighbours)
        }

        return candidatePoints.first { getSegmentForPoint(it) == desiredSegment }
    }

    private fun rationalisePoint(pt: Point)
    {
        val x = pt.x.coerceIn(0, width - 1)
        val y = pt.y.coerceIn(0, height - 1)

        pt.setLocation(x, y)
    }

    override fun paint(g: Graphics)
    {
        super.paint(g)

        val graphics2D = g as Graphics2D

        paintOuterBoard(graphics2D)

        getAllPossibleSegments().forEach { segment ->
            val pts = getPointsForSegment(segment)
            val edgePts = computeEdgePoints(pts)
            val colour = getColourFromHashMap(segment, colourWrapper)

            graphics2D.paint = colour
            pts.forEach { graphics2D.drawLine(it.x, it.y, it.x, it.y) }

            if (colourWrapper.edgeColour != null) {
                graphics2D.paint = colourWrapper.edgeColour
                edgePts.forEach { graphics2D.drawLine(it.x, it.y, it.x, it.y) }
            }
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