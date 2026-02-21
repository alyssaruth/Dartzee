package dartzee.`object`

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.bean.PresentationDartboard
import dartzee.helper.AbstractTest
import dartzee.helper.markPoints
import io.kotest.matchers.shouldBe
import java.awt.Dimension
import java.awt.Point
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestComputationalDartboard : AbstractTest() {
    @Test
    fun `Should return the correct radius`() {
        val dartboard = ComputationalDartboard(100, 100)
        dartboard.computeRadius() shouldBe 35
    }

    @Test
    fun `Should return the correct center`() {
        val dartboard = ComputationalDartboard(100, 100)
        dartboard.computeCenter() shouldBe Point(50, 50)
    }

    @Test
    fun `Should return the correct deliberate miss point`() {
        val dartboard = ComputationalDartboard(100, 100)
        val computedPoint = dartboard.getDeliberateMissPoint()
        computedPoint.pt shouldBe Point(50, 96)
        computedPoint.segment shouldBe DartboardSegment(SegmentType.MISS, 3)
        computedPoint.angle shouldBe 180.0
    }

    @Test
    fun `Should convert to a computed point`() {
        val dartboard = ComputationalDartboard(100, 100)
        val center = dartboard.toComputedPoint(Point(50, 50))
        center.pt shouldBe Point(50, 50)
        center.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 25)
        center.radius shouldBe 0.0

        val outerSix = dartboard.toComputedPoint(Point(75, 50))
        outerSix.pt shouldBe Point(75, 50)
        outerSix.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 6)
        outerSix.radius * dartboard.computeRadius() shouldBe 25.0
        outerSix.angle shouldBe 90.0
    }

    @Test
    @Tag("screenshot")
    fun `Should get all the correct aim points`() {
        val dartboard = ComputationalDartboard(400, 400)

        val pts = dartboard.getPotentialAimPoints().map { it.point }
        val presentationDartboard =
            PresentationDartboard(WIREFRAME_COLOUR_WRAPPER).also { it.size = Dimension(400, 400) }
        val lbl = presentationDartboard.markPoints(pts)
        lbl.shouldMatchImage("aim points")
    }

    @Test
    @Tag("screenshot")
    fun `Should return a sensible individual aim point`() {
        val dartboard = ComputationalDartboard(400, 400)

        val pt = dartboard.getPointToAimAt(DartboardSegment(SegmentType.INNER_SINGLE, 6))
        val presentationDartboard =
            PresentationDartboard(WIREFRAME_COLOUR_WRAPPER).also { it.size = Dimension(400, 400) }
        val lbl = presentationDartboard.markPoints(listOf(pt.pt))
        lbl.shouldMatchImage("inner 6 aim")
    }
}
