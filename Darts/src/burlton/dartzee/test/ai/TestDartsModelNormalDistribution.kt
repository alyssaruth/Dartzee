package burlton.dartzee.test.ai

import burlton.core.code.obj.HashMapCount
import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.ai.*
import burlton.dartzee.test.borrowTestDartboard
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.matchers.numerics.shouldBeBetween
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.math3.distribution.NormalDistribution
import org.junit.Test
import java.awt.Point

class TestDartsModelNormalDistribution: AbstractDartsTest()
{
    /**
     * Read / write XML etc
     */
    @Test
    fun `Should not write optional values that are unset`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        val model = DartsModelNormalDistribution()
        model.standardDeviation = 25.6
        model.radiusAverageCount = 2

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "25.6"
        rootElement.getAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT) shouldBe "2"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe ""
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe ""
    }

    @Test
    fun `Should write optional values that have been set`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        val model = DartsModelNormalDistribution()
        model.standardDeviation = 13.0
        model.radiusAverageCount = 1
        model.standardDeviationCentral = 19.2
        model.standardDeviationDoubles = 3.7

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "13.0"
        rootElement.getAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT) shouldBe "1"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe "19.2"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe "3.7"
    }

    @Test
    fun `Should handle optional values not being present in XML`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")
        rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "3")

        val model = DartsModelNormalDistribution()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
        model.radiusAverageCount shouldBe 3
        model.standardDeviationDoubles shouldBe 0.0
        model.standardDeviationCentral shouldBe 0.0

        model.distribution shouldNotBe null
        model.distribution!!.standardDeviation shouldBe 100.4
        model.distribution!!.mean shouldBe 0.0

        model.distributionDoubles shouldBe null
    }

    @Test
    fun `Should read optional values if present in XML`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()!!
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")
        rootElement.setAttribute(ATTRIBUTE_RADIUS_AVERAGE_COUNT, "3")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "50.2")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "43.5")

        val model = DartsModelNormalDistribution()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
        model.radiusAverageCount shouldBe 3
        model.standardDeviationDoubles shouldBe 50.2
        model.standardDeviationCentral shouldBe 43.5

        model.distribution shouldNotBe null
        model.distribution!!.standardDeviation shouldBe 100.4
        model.distribution!!.mean shouldBe 0.0

        model.distributionDoubles shouldNotBe null
        model.distributionDoubles!!.standardDeviation shouldBe 50.2
        model.distributionDoubles!!.mean shouldBe 0.0
    }

    /**
     * Verified against standard Normal Dist z-tables
     */
    @Test
    fun `Should return the correct density based on the standard deviation`()
    {
        val model = DartsModelNormalDistribution()
        model.populate(20.0, 0.0, 0.0, 1)

        //P(within 0.5 SD)
        model.getProbabilityWithinRadius(10.0).shouldBeBetween(0.3829, 0.3831, 0.0)

        //P(within 1 SD)
        model.getProbabilityWithinRadius(20.0).shouldBeBetween(0.6826, 0.6827, 0.0)

        //P(within 2 SD)
        model.getProbabilityWithinRadius(40.0).shouldBeBetween(0.9543, 0.9545, 0.0)

        //P(within 3 SD)
        model.getProbabilityWithinRadius(60.0).shouldBeBetween(0.9973, 0.9975, 0.0)
    }

    /**
     * Actual sampling behaviour
     */
    @Test
    fun `Should use the double distribution if throwing at a double, and the regular distribution otherwise`()
    {
        val model = DartsModelNormalDistribution()

        val distribution = mockk<NormalDistribution>(relaxed = true)
        every { distribution.sample() } returns 3.0

        val distributionDoubles = mockk<NormalDistribution>(relaxed = true)
        every { distributionDoubles.sample() } returns 6.0

        model.standardDeviationCentral = 0.0
        model.distribution = distribution
        model.distributionDoubles = distributionDoubles

        val dartboard = borrowTestDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        val ptDouble = dartboard.getPointsForSegment(20, SEGMENT_TYPE_DOUBLE).first()

        val (radiusNonDouble) = model.calculateRadiusAndAngle(pt, dartboard)
        radiusNonDouble shouldBe 3.0

        val (radiusDouble) = model.calculateRadiusAndAngle(ptDouble, dartboard)
        radiusDouble shouldBe 6.0
    }

    @Test
    fun `Should revert to the regular distribution for doubles`()
    {
        val model = DartsModelNormalDistribution()

        val distribution = mockk<NormalDistribution>(relaxed = true)
        every { distribution.sample() } returns 3.0

        model.standardDeviationCentral = 0.0
        model.distribution = distribution
        model.distributionDoubles = null

        val dartboard = borrowTestDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        val ptDouble = dartboard.getPointsForSegment(20, SEGMENT_TYPE_DOUBLE).first()

        val (radiusNonDouble) = model.calculateRadiusAndAngle(pt, dartboard)
        radiusNonDouble shouldBe 3.0

        val (radiusDouble) = model.calculateRadiusAndAngle(ptDouble, dartboard)
        radiusDouble shouldBe 3.0
    }

    @Test
    fun `Should sample the specified number of times and take an average`()
    {
        val model = DartsModelNormalDistribution()

        val distribution = mockk<NormalDistribution>(relaxed = true)

        var ix = 0
        val values = listOf(3.0, 10.0, 5.0)
        every { distribution.sample() } answers { values[ix++] }

        model.standardDeviationCentral = 0.0
        model.distribution = distribution
        model.radiusAverageCount = 3

        val dartboard = borrowTestDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()

        val (radius) = model.calculateRadiusAndAngle(pt, dartboard)
        radius shouldBe 6.0
    }

    @Test
    fun `Should generate a random angle between 0 - 360 by default`()
    {
        val model = DartsModelNormalDistribution()
        model.populate(3.0, 0.0, 0.0, 1)

        val dartboard = borrowTestDartboard()
        val pt = Point(0, 0)

        val hmAngleToCount = HashMapCount<Double>()
        for (i in 0..1000000)
        {
            val (_, theta) = model.calculateRadiusAndAngle(pt, dartboard)
            theta.shouldBeBetween(0.0, 360.0, 0.0)

            hmAngleToCount.incrementCount(Math.floor(theta))
        }

        hmAngleToCount.size shouldBe 360
        hmAngleToCount.values.forEach {
            it.shouldBeBetween(2500, 3000)
        }
    }
}