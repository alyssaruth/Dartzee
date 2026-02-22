package dartzee.bean

import dartzee.logging.CODE_SLOW_DARTBOARD_RENDER
import dartzee.logging.KEY_DURATION
import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputedPoint
import dartzee.`object`.DartboardSegment
import dartzee.`object`.GREY_COLOUR_WRAPPER
import dartzee.`object`.IDartboard
import dartzee.screen.game.SegmentStatuses
import dartzee.screen.game.getSegmentStatus
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.UPPER_BOUND_DOUBLE_RATIO
import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.computeEdgePoints
import dartzee.utils.getAllSegmentsForDartzee
import dartzee.utils.getAnglesForScore
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getFontForDartboardLabels
import dartzee.utils.getNeighbours
import dartzee.utils.translatePoint
import java.awt.Canvas
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants
import kotlin.math.roundToInt

open class PresentationDartboard(
    private val colourWrapper: ColourWrapper = getColourWrapperFromPrefs(),
    private val renderScoreLabels: Boolean = false,
) : JComponent(), IDartboard {
    var segmentStatuses: SegmentStatuses? = null
    private val overriddenSegmentColours = mutableMapOf<DartboardSegment, Color>()
    private val dirtySegments = mutableSetOf<DartboardSegment>()
    private var lastPaintImage: BufferedImage? = null

    override fun computeRadius() = computeRadius(width, height)

    override fun computeCenter() = Point(width / 2, height / 2)

    fun interpretPoint(pt: ComputedPoint): Point {
        val newPoint = translatePoint(computeCenter(), pt.radius * computeRadius(), pt.angle)

        rationalisePoint(newPoint)

        val desiredSegment = pt.segment
        val candidatePoints = mutableSetOf(newPoint)
        while (candidatePoints.none { getSegmentForPoint(it) == desiredSegment }) {
            val neighbours = candidatePoints.flatMap(::getNeighbours)
            candidatePoints.addAll(neighbours)
        }

        return candidatePoints.first { getSegmentForPoint(it) == desiredSegment }
    }

    private fun rationalisePoint(pt: Point) {
        val x = pt.x.coerceIn(0, width - 1)
        val y = pt.y.coerceIn(0, height - 1)

        pt.setLocation(x, y)
    }

    fun overrideSegmentColour(segment: DartboardSegment, colour: Color) {
        overriddenSegmentColours[segment] = colour
        dirtySegments.add(segment)

        repaint()
    }

    fun revertOverriddenSegmentColour(segment: DartboardSegment) {
        overriddenSegmentColours.remove(segment)
        dirtySegments.add(segment)

        repaint()
    }

    fun updateSegmentStatus(segmentStatuses: SegmentStatuses?) {
        val oldSegmentStatus = this.segmentStatuses
        this.segmentStatuses = segmentStatuses

        val changed =
            getAllSegmentsForDartzee().filter {
                oldSegmentStatus.getSegmentStatus(it) != segmentStatuses.getSegmentStatus(it)
            }
        dirtySegments.addAll(changed)

        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val cachedImage = lastPaintImage
        if (cachedImage != null && cachedImage.width == width && cachedImage.height == height) {
            repaintDirtySegments(cachedImage)
            g.drawImage(cachedImage, 0, 0, this)
        } else {
            val timer = DurationTimer()
            val bi = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            paintOuterBoard(bi.createGraphics())
            getAllSegmentsForDartzee().paintAll(bi)

            g.drawImage(bi, 0, 0, this)
            lastPaintImage = bi

            val duration = timer.getDuration()
            if (duration > 500) {
                logger.warn(
                    CODE_SLOW_DARTBOARD_RENDER,
                    "Rendered dartboard[$width, $height] in ${duration}ms",
                    KEY_DURATION to duration,
                )
            }
        }
    }

    private fun repaintDirtySegments(cachedImage: BufferedImage) {
        val dirtySegmentsCopy = dirtySegments.toList()
        if (dirtySegmentsCopy.isNotEmpty()) {
            dirtySegmentsCopy.paintAll(cachedImage)
            dirtySegments.clear()
        }
    }

    private fun List<DartboardSegment>.paintAll(image: BufferedImage) {
        sortedBy { it.type }.forEach { paintSegment(it, image) }
    }

    private fun paintSegment(segment: DartboardSegment, bi: BufferedImage) {
        val colour = overriddenSegmentColours[segment] ?: defaultColourForSegment(segment)
        colourSegment(segment, colour, bi)
    }

    protected fun defaultColourForSegment(segment: DartboardSegment): Color {
        val default = colourWrapper.getColour(segment)
        val status = segmentStatuses ?: return default
        return when {
            status.allowsMissing() && segment.isMiss() -> GREY_COLOUR_WRAPPER.getColour(segment)
            status.scoringSegments.contains(segment) -> default
            status.validSegments.contains(segment) -> GREY_COLOUR_WRAPPER.getColour(segment)
            else -> Color.BLACK
        }
    }

    private fun colourSegment(segment: DartboardSegment, color: Color, bi: BufferedImage) {
        if (segment.isMiss()) {
            paintOuterRing(bi.createGraphics(), color)
            return
        }

        val pts = getPointsForSegment(segment)
        val edgePts = computeEdgePoints(pts)

        pts.forEach { bi.setRGB(it.x, it.y, color.rgb) }

        getEdgeColourForSegment(segment)?.let { edgeColour ->
            edgePts.forEach { bi.setRGB(it.x, it.y, edgeColour.rgb) }
        }
    }

    private fun getEdgeColourForSegment(segment: DartboardSegment): Color? {
        val default = colourWrapper.edgeColour
        val status = segmentStatuses ?: return default
        return if (status.scoringSegments.contains(segment)) Color.GRAY else null
    }

    private fun paintOuterBoard(g: Graphics2D) {
        g.paint = colourWrapper.missedBoardColour
        g.fillRect(0, 0, width, height)

        val borderSize = (computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO).toInt()
        val center = computeCenter()
        g.paint = colourWrapper.outerDartboardColour
        g.fillOval(center.x - borderSize, center.y - borderSize, borderSize * 2, borderSize * 2)
    }

    private fun paintOuterRing(g: Graphics2D, color: Color) {
        g.paint = color
        val ring = createOuterRing()
        g.fill(ring)

        paintScoreLabels(g)
    }

    private fun createOuterRing(): Shape {
        val center = computeCenter()
        val outerRadius = computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO
        val innerRadius = computeRadius() * UPPER_BOUND_DOUBLE_RATIO
        val thickness = outerRadius - innerRadius
        val outer: Ellipse2D =
            Ellipse2D.Double(
                center.x - outerRadius,
                center.y - outerRadius,
                outerRadius + outerRadius,
                outerRadius + outerRadius,
            )
        val inner: Ellipse2D =
            Ellipse2D.Double(
                center.x - outerRadius + thickness,
                center.y - outerRadius + thickness,
                outerRadius + outerRadius - (2 * thickness),
                outerRadius + outerRadius - (2 * thickness),
            )
        val area = Area(outer)
        area.subtract(Area(inner))
        return area
    }

    private fun paintScoreLabels(g: Graphics2D) {
        if (!renderScoreLabels) return

        val radius = computeRadius()
        val outerRadius = UPPER_BOUND_OUTSIDE_BOARD_RATIO * radius
        val lblHeight = ((outerRadius - radius) / 2).roundToInt()

        val fontToUse = getFontForDartboardLabels(lblHeight, g)
        (1..20).forEach { paintScoreLabel(it, g, fontToUse, lblHeight) }
    }

    private fun paintScoreLabel(score: Int, g: Graphics2D, fontToUse: Font, lblHeight: Int) {
        // Create a label with standard properties
        val lbl = JLabel(score.toString())
        lbl.foreground = InjectedThings.theme?.fontColor ?: Color.WHITE
        lbl.horizontalAlignment = SwingConstants.CENTER
        lbl.font = fontToUse

        // Work out the width for this label, based on the text
        val metrics = Canvas().getFontMetrics(fontToUse)
        val lblWidth = metrics.stringWidth(score.toString()) + 5
        lbl.setSize(lblWidth, lblHeight)

        // Work out where to place the label
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
