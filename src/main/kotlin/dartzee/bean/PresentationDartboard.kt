package dartzee.bean

import dartzee.logging.CODE_RENDERED_DARTBOARD
import dartzee.logging.KEY_DURATION
import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputedPoint
import dartzee.`object`.DartboardSegment
import dartzee.`object`.IDartboard
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.computeEdgePoints
import dartzee.utils.factoryFontMetrics
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getAnglesForScore
import dartzee.utils.getColourFromHashMap
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getFontForDartboardLabels
import dartzee.utils.getNeighbours
import dartzee.utils.translatePoint
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.math.roundToInt

open class PresentationDartboard(
    protected val colourWrapper: ColourWrapper = getColourWrapperFromPrefs(),
    private val renderScoreLabels: Boolean = false
) : JComponent(), IDartboard
{
    private val overriddenSegmentColours = mutableMapOf<DartboardSegment, Color>()
    private val dirtySegments = mutableListOf<DartboardSegment>()
    private var lastPaintImage: BufferedImage? = null

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

    override fun paintComponent(g: Graphics)
    {
        super.paintComponent(g)

        if (lastPaintImage != null && lastPaintImage?.width == width && lastPaintImage?.height == height)
        {
            if (dirtySegments.isNotEmpty())
            {
                dirtySegments.forEach { paintSegment(it, lastPaintImage!!) }
                dirtySegments.clear()
            }

            g.drawImage(lastPaintImage!!, 0, 0, this)
        }
        else
        {
            val timer = DurationTimer()
            val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val biGraphics = bi.createGraphics()
            paintOuterBoard(biGraphics)
            getAllPossibleSegments().forEach { paintSegment(it, bi) }
            paintScoreLabels(biGraphics)

            g.drawImage(bi, 0, 0, this)
            lastPaintImage = bi

            val duration = timer.getDuration()
            logger.info(
                CODE_RENDERED_DARTBOARD, "Rendered dartboard[$width, $height] in ${duration}ms",
                KEY_DURATION to duration)
        }
    }

    private fun paintSegment(segment: DartboardSegment, bi: BufferedImage)
    {
        val colour = overriddenSegmentColours[segment] ?: getColourFromHashMap(segment, colourWrapper)
        colourSegment(segment, colour, bi)
    }

    fun overrideSegmentColour(segment: DartboardSegment, colour: Color)
    {
        overriddenSegmentColours[segment] = colour
        dirtySegments.add(segment)

        repaint()
    }

    fun revertOverriddenSegmentColour(segment: DartboardSegment)
    {
        overriddenSegmentColours.remove(segment)
        dirtySegments.add(segment)

        repaint()
    }

    private fun colourSegment(segment: DartboardSegment, color: Color, bi: BufferedImage)
    {
        val pts = getPointsForSegment(segment)
        val edgePts = computeEdgePoints(pts)

        pts.forEach { bi.setRGB(it.x, it.y, color.rgb) }

        colourWrapper.edgeColour?.let { edgeColour ->
            edgePts.forEach { bi.setRGB(it.x, it.y, edgeColour.rgb) }
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

    private fun paintScoreLabels(g: Graphics2D)
    {
        if (!renderScoreLabels) return

        val radius = computeRadius()
        val outerRadius = UPPER_BOUND_OUTSIDE_BOARD_RATIO * radius
        val lblHeight = ((outerRadius - radius) / 2).roundToInt()

        val fontToUse = getFontForDartboardLabels(lblHeight)
        (1..20).forEach { paintScoreLabel(it, g, fontToUse, lblHeight)}
    }

    private fun paintScoreLabel(score: Int, g: Graphics2D, fontToUse: Font, lblHeight: Int)
    {
        //Create a label with standard properties
        val lbl = JLabel(score.toString())
        lbl.foreground = Color.WHITE
        lbl.horizontalAlignment = SwingConstants.CENTER
        lbl.font = fontToUse

        //Work out the width for this label, based on the text
        val metrics = factoryFontMetrics(fontToUse)
        val lblWidth = metrics.stringWidth(score.toString()) + 5
        lbl.setSize(lblWidth, lblHeight)

        //Work out where to place the label
        val angle = getAnglesForScore(score).toList().average()
        val radiusForLabel = computeRadius() + lblHeight
        val avgPoint = translatePoint(computeCenter(), radiusForLabel, angle)

        val lblX = avgPoint.getX().toInt() - lblWidth / 2
        val lblY = avgPoint.getY().toInt() - lblHeight / 2

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.translate(lblX, lblY)
        lbl.paint(g)
        g.translate(-lblX, -lblY)
    }
}