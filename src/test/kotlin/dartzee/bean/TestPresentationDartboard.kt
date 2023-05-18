package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.core.bean.getPointList
import dartzee.helper.AbstractTest
import dartzee.helper.makeSegmentStatuses
import dartzee.logging.CODE_RENDERED_DARTBOARD
import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputationalDartboard
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.`object`.WIREFRAME_COLOUR_WRAPPER
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.image.BufferedImage

class TestPresentationDartboard : AbstractTest()
{
    @Test
    fun `Should preserve the segment type when mapping between dartboards`()
    {
        val computationalDartboard = ComputationalDartboard(200, 200)
        val smallDartboard = PresentationDartboard().also { it.size = Dimension(100, 100) }
        val bigDartboard = PresentationDartboard().also { it.size = Dimension(400, 400) }

        getPointList(200, 200).forEach { pt ->
            val computedPoint = computationalDartboard.toComputedPoint(pt)

            val smallPt = smallDartboard.interpretPoint(computedPoint)
            smallDartboard.getSegmentForPoint(smallPt) shouldBe computedPoint.segment

            val bigPt = bigDartboard.interpretPoint(computedPoint)
            bigDartboard.getSegmentForPoint(bigPt) shouldBe computedPoint.segment
        }
    }

    @Test
    fun `Should rationalise points to within the bounds of the dartboard`()
    {
        val computationalDartboard = ComputationalDartboard(400, 400)
        val wildPoint = computationalDartboard.toComputedPoint(Point(1000, 1000))

        val smallDartboard = PresentationDartboard().also { it.size = Dimension(100, 100) }
        val result = smallDartboard.interpretPoint(wildPoint)
        result shouldBe Point(99, 99)
    }

    @Test
    fun `Should reuse cached image if dimensions are unchanged`()
    {
        val bi = BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)

        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.paint(bi.graphics)
        clearLogs()

        dartboard.paint(bi.graphics)
        verifyNoLogs(CODE_RENDERED_DARTBOARD)

        dartboard.setBounds(0, 0, 401, 401)
        dartboard.paint(bi.graphics)
        verifyLog(CODE_RENDERED_DARTBOARD)
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - default`()
    {
        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("default")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - with numbers`()
    {
        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER, renderScoreLabels = true)
        dartboard.setBounds(0, 0, 500, 500)
        dartboard.shouldMatchImage("scores")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - wireframe`()
    {
        val dartboard = PresentationDartboard(WIREFRAME_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 250, 250)
        dartboard.shouldMatchImage("wireframe")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - custom colours`()
    {
        val colourWrapper = ColourWrapper(
            Color.PINK.darker(), Color.PINK, Color.PINK,
            Color.YELLOW.brighter(), Color.YELLOW.darker(), Color.YELLOW.darker(),
            Color.PINK, Color.YELLOW.darker()
        )

        val dartboard = PresentationDartboard(colourWrapper)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("custom")
    }

    @Test
    @Tag("screenshot")
    fun `Should respect overridden segments and retain them on resize`()
    {
        val segment = DartboardSegment(SegmentType.OUTER_SINGLE, 11)

        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 200, 200)
        dartboard.overrideSegmentColour(segment, Color.PINK)
        dartboard.shouldMatchImage("overridden-small")

        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("overridden-resized")

        dartboard.revertOverriddenSegmentColour(segment)
        dartboard.shouldMatchImage("default")
    }

    @Test
    @Tag("screenshot")
    fun `Should draw edges with segment statuses set`()
    {
        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.updateSegmentStatus(makeSegmentStatuses())

        dartboard.shouldMatchImage("segment-statuses-all-scoring")
    }

    @Test
    @Tag("screenshot")
    fun `Should respect segment statuses`()
    {
        val dartboard = PresentationDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.setBounds(0, 0, 600, 600)

        val scoringSegments = getAllNonMissSegments().filter { listOf(1, 20, 5).contains(it.score) }
        val validSegments = getAllNonMissSegments().filter { !listOf(19, 3, 17).contains(it.score) }

        dartboard.updateSegmentStatus(SegmentStatuses(scoringSegments, validSegments))
        dartboard.shouldMatchImage("segment-statuses")
    }
}