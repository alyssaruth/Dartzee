package dartzee.`object`

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.bean.PresentationDartboard
import dartzee.helper.AbstractTest
import dartzee.helper.markPoints
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Dimension
import java.awt.Point

class TestComputationalDartboard: AbstractTest()
{
    @Test
    fun `Should return the correct radius`()
    {
        val dartboard = ComputationalDartboard(100, 100)
        dartboard.computeRadius() shouldBe 35
    }

    @Test
    fun `Should return the correct center`()
    {
        val dartboard = ComputationalDartboard(100, 100)
        dartboard.computeCenter() shouldBe Point(50, 50)
    }

    @Test
    fun `Should return the correct deliberate miss point`()
    {
        val dartboard = ComputationalDartboard(100, 100)
        dartboard.getDeliberateMissPoint() shouldBe Point(50, 96)
    }

    @Test
    @Tag("screenshot")
    fun `Should get all the correct aim points`()
    {
        val dartboard = ComputationalDartboard(400, 400)

        val pts = dartboard.getPotentialAimPoints().map { it.point }
        val presentationDartboard = PresentationDartboard(WIREFRAME_COLOUR_WRAPPER).also { it.size = Dimension(400, 400) }
        val lbl = presentationDartboard.markPoints(pts)
        lbl.shouldMatchImage("aim points")
    }

    @Test
    @Tag("screenshot")
    fun `Should return a sensible individual aim point`()
    {
        val dartboard = ComputationalDartboard(400, 400)

        val pt = dartboard.getPointToAimAt(DartboardSegment(SegmentType.INNER_SINGLE, 6))
        val presentationDartboard = PresentationDartboard(WIREFRAME_COLOUR_WRAPPER).also { it.size = Dimension(400, 400) }
        val lbl = presentationDartboard.markPoints(listOf(pt))
        lbl.shouldMatchImage("inner 6 aim")
    }
}