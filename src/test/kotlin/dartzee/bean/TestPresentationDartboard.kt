package dartzee.bean

import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.core.bean.getPointList
import dartzee.helper.AbstractTest
import dartzee.`object`.ColourWrapper
import dartzee.`object`.ComputationalDartboard
import dartzee.`object`.WIREFRAME_COLOUR_WRAPPER
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Point

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
    @Tag("screenshot")
    fun `Should match snapshot - default`()
    {
        val dartboard = PresentationDartboard()
        dartboard.setBounds(0, 0, 400, 400)
        dartboard.shouldMatchImage("default")
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
}