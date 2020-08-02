package dartzee.ai

import dartzee.`object`.SegmentType
import dartzee.borrowTestDartboard
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.math3.distribution.NormalDistribution
import org.junit.Test
import java.awt.Point
import kotlin.math.floor

class TestDartsAiModel: AbstractTest()
{
    /**
     * Serialisation
     */
    @Test
    fun `Should serialise and deserialise correctly with default values`()
    {
        val model = DartsAiModel()

        val xml = model.writeXml()

        val newModel = DartsAiModel()
        newModel.readXml(xml)

        model.scoringDart shouldBe newModel.scoringDart
        model.hmScoreToDart shouldContainExactly newModel.hmScoreToDart
        model.hmDartNoToSegmentType shouldContainExactly newModel.hmDartNoToSegmentType
        model.hmDartNoToStopThreshold shouldContainExactly newModel.hmDartNoToStopThreshold
        model.mercyThreshold shouldBe newModel.mercyThreshold
        model.standardDeviation shouldBe newModel.standardDeviation
        model.standardDeviationDoubles shouldBe newModel.standardDeviationDoubles
        model.standardDeviationCentral shouldBe newModel.standardDeviationCentral
        model.dartzeePlayStyle shouldBe DartzeePlayStyle.CAUTIOUS
    }

    @Test
    fun `Should serialise and deserialise non-defaults correctly`()
    {
        val model = DartsAiModel()
        model.scoringDart = 25
        model.hmScoreToDart[50] = AimDart(25, 2)
        model.hmScoreToDart[60] = AimDart(10, 1)
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToStopThreshold[1] = 1
        model.hmDartNoToStopThreshold[2] = 2
        model.mercyThreshold = 18
        model.dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE
        model.standardDeviation = 25.6
        model.standardDeviationDoubles = 50.2
        model.standardDeviationCentral = 80.5

        val xml = model.writeXml()

        val newModel = DartsAiModel()
        newModel.readXml(xml)

        newModel.scoringDart shouldBe 25
        newModel.mercyThreshold shouldBe 18
        newModel.dartzeePlayStyle shouldBe DartzeePlayStyle.AGGRESSIVE
        newModel.standardDeviation shouldBe 25.6
        newModel.standardDeviationDoubles shouldBe 50.2
        newModel.standardDeviationCentral shouldBe 80.5
        newModel.hmScoreToDart shouldContainExactly model.hmScoreToDart
        newModel.hmDartNoToSegmentType shouldContainExactly model.hmDartNoToSegmentType
        newModel.hmDartNoToStopThreshold shouldContainExactly model.hmDartNoToStopThreshold

        newModel.distribution shouldNotBe null
        newModel.distribution!!.standardDeviation shouldBe 25.6
        newModel.distribution!!.mean shouldBe 0.0

        newModel.distributionDoubles shouldNotBe null
        newModel.distributionDoubles!!.standardDeviation shouldBe 50.2
        newModel.distributionDoubles!!.mean shouldBe 0.0
    }

    /**
     * Verified against standard Normal Dist z-tables
     */
    @Test
    fun `Should return the correct density based on the standard deviation`()
    {
        val model = DartsAiModel()
        model.populate(20.0, 0.0, 0.0)

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
        val model = DartsAiModel()

        val distribution = mockk<NormalDistribution>(relaxed = true)
        every { distribution.sample() } returns 3.0

        val distributionDoubles = mockk<NormalDistribution>(relaxed = true)
        every { distributionDoubles.sample() } returns 6.0

        model.standardDeviationCentral = 0.0
        model.distribution = distribution
        model.distributionDoubles = distributionDoubles

        val dartboard = borrowTestDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        val ptDouble = dartboard.getPointsForSegment(20, SegmentType.DOUBLE).first()

        val (radiusNonDouble) = model.calculateRadiusAndAngle(pt, dartboard)
        radiusNonDouble shouldBe 3.0

        val (radiusDouble) = model.calculateRadiusAndAngle(ptDouble, dartboard)
        radiusDouble shouldBe 6.0
    }

    @Test
    fun `Should revert to the regular distribution for doubles`()
    {
        val model = DartsAiModel()

        val distribution = mockk<NormalDistribution>(relaxed = true)
        every { distribution.sample() } returns 3.0

        model.standardDeviationCentral = 0.0
        model.distribution = distribution
        model.distributionDoubles = null

        val dartboard = borrowTestDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        val ptDouble = dartboard.getPointsForSegment(20, SegmentType.DOUBLE).first()

        val (radiusNonDouble) = model.calculateRadiusAndAngle(pt, dartboard)
        radiusNonDouble shouldBe 3.0

        val (radiusDouble) = model.calculateRadiusAndAngle(ptDouble, dartboard)
        radiusDouble shouldBe 3.0
    }

    @Test
    fun `Should generate a random angle between 0 - 360 by default`()
    {
        val model = DartsAiModel()
        model.populate(3.0, 0.0, 0.0)

        val dartboard = borrowTestDartboard()
        val pt = Point(0, 0)

        val hsAngles = HashSet<Double>()
        for (i in 0..100000)
        {
            val (_, theta) = model.calculateRadiusAndAngle(pt, dartboard)
            theta.shouldBeBetween(0.0, 360.0, 0.0)

            hsAngles.add(floor(theta))
        }

        hsAngles.size shouldBe 360
    }




}