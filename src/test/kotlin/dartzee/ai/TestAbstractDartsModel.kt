package dartzee.ai

import dartzee.`object`.ColourWrapper
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.ai.AbstractDartsModel.Companion.ATTRIBUTE_STANDARD_DEVIATION
import dartzee.ai.AbstractDartsModel.Companion.ATTRIBUTE_STANDARD_DEVIATION_CENTRAL
import dartzee.ai.AbstractDartsModel.Companion.ATTRIBUTE_STANDARD_DEVIATION_DOUBLES
import dartzee.borrowTestDartboard
import dartzee.core.util.XmlUtil
import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.db.CLOCK_TYPE_TREBLES
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.listener.DartboardListener
import dartzee.screen.Dartboard
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getCheckoutScores
import io.kotlintest.matchers.doubles.shouldBeBetween
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.apache.commons.math3.distribution.NormalDistribution
import org.junit.Test
import org.w3c.dom.Element
import java.awt.Point
import kotlin.math.floor

class TestAbstractDartsModel: AbstractTest()
{
    /**
     * Serialisation
     */
    @Test
    fun `Should serialise and deserialise correctly with default values`()
    {
        val model = AbstractDartsModel()

        val xml = model.writeXml()

        val newModel = AbstractDartsModel()
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
        val model = AbstractDartsModel()
        model.scoringDart = 25
        model.hmScoreToDart[50] = Dart(25, 2)
        model.hmScoreToDart[60] = Dart(10, 1)
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToStopThreshold[1] = 1
        model.hmDartNoToStopThreshold[2] = 2
        model.mercyThreshold = 18
        model.dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE
        model.standardDeviation = 25.6
        model.standardDeviationDoubles = 50.2
        model.standardDeviationCentral = 80.5

        val xml = model.writeXml()

        val newModel = AbstractDartsModel()
        newModel.readXml(xml)

        newModel.scoringDart shouldBe 25
        newModel.mercyThreshold shouldBe 18
        newModel.dartzeePlayStyle shouldBe DartzeePlayStyle.AGGRESSIVE
        newModel.standardDeviation shouldBe 25.6
        newModel.standardDeviationDoubles shouldBe 50.2
        newModel.standardDeviationCentral shouldBe 80.5
        model.hmScoreToDart shouldContainExactly newModel.hmScoreToDart
        model.hmDartNoToSegmentType shouldContainExactly newModel.hmDartNoToSegmentType
        model.hmDartNoToStopThreshold shouldContainExactly newModel.hmDartNoToStopThreshold
    }

    /**
     * Read / write XML etc
     */
    @Test
    fun `Should not write optional values that are unset`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createElement("Test")

        val model = AbstractDartsModel()
        model.standardDeviation = 25.6

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "25.6"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe ""
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe ""
    }

    @Test
    fun `Should write optional values that have been set`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createElement("Test")

        val model = AbstractDartsModel()
        model.standardDeviation = 13.0
        model.standardDeviationCentral = 19.2
        model.standardDeviationDoubles = 3.7

        model.writeXmlSpecific(rootElement)

        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION) shouldBe "13.0"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL) shouldBe "19.2"
        rootElement.getAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES) shouldBe "3.7"
    }

    @Test
    fun `Should handle optional values not being present in XML`()
    {
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")

        val model = AbstractDartsModel()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
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
        val xmlDoc = XmlUtil.factoryNewDocument()
        val rootElement = xmlDoc.createElement("Test")

        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION, "100.4")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_DOUBLES, "50.2")
        rootElement.setAttribute(ATTRIBUTE_STANDARD_DEVIATION_CENTRAL, "43.5")

        val model = AbstractDartsModel()
        model.readXmlSpecific(rootElement)

        model.standardDeviation shouldBe 100.4
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
        val model = AbstractDartsModel()
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
        val model = AbstractDartsModel()

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
        val model = AbstractDartsModel()

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
        val model = AbstractDartsModel()
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

    /**
     * X01 test
     */
    @Test
    fun `Should aim for the overridden value if one is set for the current setup score`()
    {
        val model = beastDartsModel()
        model.hmScoreToDart[77] = Dart(17, 2)

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(77, dartboard)

        verify { listener.dartThrown(Dart(17, 2)) }
    }

    @Test
    fun `Should aim for the scoring dart when the score is over 60`()
    {
        val model = beastDartsModel()
        model.scoringDart = 18

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(61, dartboard)

        verify { listener.dartThrown(Dart(18, 3)) }
    }

    @Test
    fun `Should throw at inner bull if the scoring dart is 25`()
    {
        val model = beastDartsModel()
        model.scoringDart = 25

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(501, dartboard)

        verify { listener.dartThrown(Dart(25, 2)) }
    }

    @Test
    fun `Should aim to reduce down to D20 when in the 41 - 60 range`()
    {
        val model = beastDartsModel()
        model.scoringDart = 25

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        for (i in 41..60)
        {
            dartboard.clearDarts()

            val listener = mockk<DartboardListener>(relaxed = true)
            dartboard.addDartboardListener(listener)

            model.throwX01Dart(i, dartboard)
            verify { listener.dartThrown(Dart(i - 40, 1))}
        }
    }

    @Test
    fun `Should aim to reduce to 32 when the score is less than 40`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(37, dartboard)
        verify { listener.dartThrown(Dart(5, 1))}
    }

    @Test
    fun `Should aim to reduce to 16 when the score is less than 32`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(31, dartboard)
        verify { listener.dartThrown(Dart(15, 1))}
    }

    @Test
    fun `Should aim to reduce to 8 when the score is less than 16`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(15, dartboard)
        verify { listener.dartThrown(Dart(7, 1))}
    }

    @Test
    fun `Should aim to reduce to 4 when the score is less than 8`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(7, dartboard)
        verify { listener.dartThrown(Dart(3, 1)) }
    }

    @Test
    fun `Should aim for the double when on an even number`()
    {
        val model = beastDartsModel()

        val scores = getCheckoutScores().filter{ it <= 40 }
        scores.forEach {
            val dartboard = Dartboard(100, 100)
            dartboard.paintDartboard()

            val listener = mockk<DartboardListener>(relaxed = true)
            dartboard.addDartboardListener(listener)

            model.throwX01Dart(it, dartboard)
            verify { listener.dartThrown(Dart(it/2, 2))}
        }
    }

    /**
     * Golf behaviour
     */
    @Test
    fun `Should aim for double, treble, treble by default`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        for (i in 1..3) { model.throwGolfDart(1, i, dartboard) }

        verifySequence { listener.dartThrown(Dart(1, 2)); listener.dartThrown(Dart(1, 3)); listener.dartThrown(Dart(1, 3)) }
    }

    @Test
    fun `Should respect overridden targets per dart`()
    {
        val model = beastDartsModel()
        model.hmDartNoToSegmentType[1] = SegmentType.TREBLE
        model.hmDartNoToSegmentType[2] = SegmentType.OUTER_SINGLE
        model.hmDartNoToSegmentType[3] = SegmentType.DOUBLE

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        for (i in 1..3) { model.throwGolfDart(1, i, dartboard) }

        verifySequence { listener.dartThrown(Dart(1, 3)); listener.dartThrown(Dart(1, 1)); listener.dartThrown(Dart(1, 2)) }
    }

    @Test
    fun `Stop thresholds should be 2 then 3 by default`()
    {
        val model = beastDartsModel()

        model.getStopThresholdForDartNo(1) shouldBe 2
        model.getStopThresholdForDartNo(2) shouldBe 3
    }

    @Test
    fun `Overridden stop thresholds should be adhered to`()
    {
        val model = beastDartsModel()
        model.hmDartNoToStopThreshold[1] = 3
        model.hmDartNoToStopThreshold[2] = 4

        model.getStopThresholdForDartNo(1) shouldBe 3
        model.getStopThresholdForDartNo(2) shouldBe 4
    }

    /**
     * Clock
     */
    @Test
    fun `Should aim for the right segmentType and target number`()
    {
        val model = beastDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwClockDart(1, CLOCK_TYPE_STANDARD, dartboard)
        model.throwClockDart(13, CLOCK_TYPE_DOUBLES, dartboard)
        model.throwClockDart(11, CLOCK_TYPE_TREBLES, dartboard)

        verifySequence { listener.dartThrown(Dart(1, 1)); listener.dartThrown(Dart(13, 2)); listener.dartThrown(Dart(11, 3)) }
    }

    /**
     * Dartzee
     */
    @Test
    fun `Should aim aggressively if less than 2 darts thrown, and cautiously for the final one`()
    {
        val model = beastDartsModel()
        model.dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS

        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        val segmentStatus = SegmentStatus(listOf(DartboardSegment(SegmentType.TREBLE, 20)), getAllPossibleSegments())
        model.throwDartzeeDart(0, dartboard, segmentStatus)
        model.throwDartzeeDart(1, dartboard, segmentStatus)
        model.throwDartzeeDart(2, dartboard, segmentStatus)

        verifySequence {
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(25, 2))
        }
    }

    @Test
    fun `Should throw aggressively for the final dart if player is aggressive`()
    {
        val model = beastDartsModel()
        model.dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE

        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        val segmentStatus = SegmentStatus(listOf(DartboardSegment(SegmentType.TREBLE, 20)), getAllPossibleSegments())
        model.throwDartzeeDart(0, dartboard, segmentStatus)
        model.throwDartzeeDart(1, dartboard, segmentStatus)
        model.throwDartzeeDart(2, dartboard, segmentStatus)

        verifySequence {
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
        }
    }

    /**
     * Misc
     */
    @Test
    fun `Should aim at the average point for the relevant segment`()
    {
        val dartboard = FudgedDartboard()
        dartboard.paintDartboard()

        val model = beastDartsModel()
        model.getPointForScore(Dart(20, 3), dartboard) shouldBe Point(3, 4)
    }

    class FudgedDartboard : Dartboard(100, 100)
    {
        override fun paintDartboard(colourWrapper: ColourWrapper?, listen: Boolean, cached: Boolean)
        {
            super.paintDartboard(colourWrapper, listen, cached)

            val segment = DartboardSegment(SegmentType.TREBLE, 20)
            segment.addPoint(Point(1, 7))
            segment.addPoint(Point(3, 3))
            segment.addPoint(Point(5, 2))

            hmSegmentKeyToSegment["20_${SegmentType.TREBLE}"] = segment
        }
    }
}