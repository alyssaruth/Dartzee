package dartzee.ai

import dartzee.dartzee.DartzeeAimCalculator
import dartzee.game.ClockType
import dartzee.helper.AbstractTest
import dartzee.helper.beastDartsModel
import dartzee.helper.makeDartsModel
import dartzee.helper.makeSegmentStatuses
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.game.dartzee.SegmentStatuses
import dartzee.utils.InjectedThings
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getCheckoutScores
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Point
import kotlin.math.floor

class TestDartsAiModel: AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        InjectedThings.dartzeeAimCalculator = DartzeeAimCalculator()
    }

    @Test
    fun `Should serialize and deserialize with default values`()
    {
        val model = DartsAiModel.new()
        val result = model.toJson()

        val model2 = DartsAiModel.fromJson(result)
        model shouldBe model2
    }

    @Test
    fun `Should serialize and deserialize with populated values`()
    {
        val model = makePopulatedAiModel()

        val result = model.toJson()
        val model2 = DartsAiModel.fromJson(result)
        model shouldBe model2
    }

    @Test
    fun `Should deserialize from static JSON`()
    {
        val jsonString = javaClass.getResource("/aiModel.json")!!.readText()
        val model = DartsAiModel.fromJson(jsonString)

        model shouldBe makePopulatedAiModel()
    }

    private fun makePopulatedAiModel(): DartsAiModel
    {
        val setupDarts = mapOf(57 to AimDart(17, 1), 97 to AimDart(19, 3))
        val hmDartNoToSegmentType = mapOf(1 to SegmentType.TREBLE, 2 to SegmentType.TREBLE, 3 to SegmentType.OUTER_SINGLE)
        val hmDartNoToThreshold = mapOf(1 to 2, 2 to 3)
        return DartsAiModel(
            50.0,
            40.0,
            35.0,
            345,
            20,
            setupDarts,
            17,
            hmDartNoToSegmentType,
            hmDartNoToThreshold,
            DartzeePlayStyle.CAUTIOUS)
    }

    /**
     * Verified against standard Normal Dist z-tables
     */
    @Test
    fun `Should return the correct density based on the standard deviation`()
    {
        val model = makeDartsModel(standardDeviation = 20.0)

        //P(within 0.5 SD)
        model.getProbabilityWithinRadius(10.0)!!.shouldBeBetween(0.3829, 0.3831, 0.0)

        //P(within 1 SD)
        model.getProbabilityWithinRadius(20.0)!!.shouldBeBetween(0.6826, 0.6827, 0.0)

        //P(within 2 SD)
        model.getProbabilityWithinRadius(40.0)!!.shouldBeBetween(0.9543, 0.9545, 0.0)

        //P(within 3 SD)
        model.getProbabilityWithinRadius(60.0)!!.shouldBeBetween(0.9973, 0.9975, 0.0)
    }

    @Test
    fun `Should return null density if maxOutlierRatio prevents darts going there`()
    {
        val model = makeDartsModel(standardDeviation = 3.0, maxRadius = 50)

        model.getProbabilityWithinRadius(51.0) shouldBe null
        model.getProbabilityWithinRadius(50.0) shouldNotBe null
    }

    /**
     * Actual sampling behaviour
     */
    @Test
    fun `Should use the double distribution if throwing at a double, and the regular distribution otherwise`()
    {
        val model = beastDartsModel(standardDeviationDoubles = 100000.0, maxRadius = 1000)

        val pt = model.throwX01Dart(40)
        pt.segment shouldNotBe DartboardSegment(SegmentType.DOUBLE, 20)
    }

    @Test
    fun `Should revert to the regular distribution for doubles`()
    {
        val model = beastDartsModel(standardDeviationDoubles = null)

        val pt = model.throwX01Dart(40)
        pt.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 20)
    }

    @Test
    fun `Should generate a random angle between 0 - 360 by default`()
    {
        val model = makeDartsModel(standardDeviation = 3.0)

        val pt = Point(0, 0)
        val hsAngles = HashSet<Double>()
        for (i in 0..100000)
        {
            val (_, theta) = model.calculateRadiusAndAngle(pt)
            theta.shouldBeBetween(0.0, 360.0, 0.0)

            hsAngles.add(floor(theta))
        }

        hsAngles.size shouldBe 360
    }

    @Test
    fun `Should not allow the radius to exceed the max outlier ratio`()
    {
        val pt = Point(0, 0)

        val model = makeDartsModel(standardDeviation = 50.0, maxRadius = 50)
        val radii = (1..1000).map { model.calculateRadiusAndAngle(pt).radius }
        radii.forEach { it.shouldBeBetween(-50.0, 50.0, 0.0) }

        val erraticModel = makeDartsModel(standardDeviation = 50.0, maxRadius = 75)
        val moreRadii = (1..1000).map { erraticModel.calculateRadiusAndAngle(pt).radius }
        moreRadii.forEach { it.shouldBeBetween(-75.0, 75.0, 0.0) }
    }

    @Test
    fun `Should just miss the board if told to deliberately miss`()
    {
        val erraticModel = makeDartsModel(standardDeviation = 100.0, maxRadius = 75)

        val mockDartzeeAimCalculator = mockk<DartzeeAimCalculator>()
        every { mockDartzeeAimCalculator.getPointToAimFor(any(), any(), any()) } returns DELIBERATE_MISS
        InjectedThings.dartzeeAimCalculator = mockDartzeeAimCalculator

        repeat(20) {
            val pt = erraticModel.throwDartzeeDart(0, makeSegmentStatuses())
            pt.segment shouldBe DartboardSegment(SegmentType.MISSED_BOARD, 3)
        }
    }

    /**
     * X01 test
     */
    @Test
    fun `Should aim for the overridden value if one is set for the current setup score`()
    {
        val model = beastDartsModel(hmScoreToDart = mapOf(77 to AimDart(17, 2)))

        val pt = model.throwX01Dart(77)
        pt.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 17)
    }

    @Test
    fun `Should aim for the scoring dart when the score is over 60`()
    {
        val model = beastDartsModel(scoringDart = 18)

        val pt = model.throwX01Dart(61)
        pt.segment shouldBe DartboardSegment(SegmentType.TREBLE, 18)
    }

    @Test
    fun `Should throw at inner bull if the scoring dart is 25`()
    {
        val model = beastDartsModel(scoringDart = 25)

        val pt = model.throwX01Dart(501)
        pt.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 25)
    }

    @Test
    fun `Should aim to reduce down to D20 when in the 41 - 60 range`()
    {
        val model = beastDartsModel(scoringDart = 25)

        for (i in 41..60)
        {
            val pt = model.throwX01Dart(i)
            pt.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, i - 40)
        }
    }

    @Test
    fun `Should aim to reduce to 32 when the score is less than 40`()
    {
        val model = beastDartsModel()

        val pt = model.throwX01Dart(37)
        pt.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 5)
    }

    @Test
    fun `Should aim to reduce to 16 when the score is less than 32`()
    {
        val model = beastDartsModel()

        val pt = model.throwX01Dart(31)
        pt.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 15)
    }

    @Test
    fun `Should aim to reduce to 8 when the score is less than 16`()
    {
        val model = beastDartsModel()

        val pt = model.throwX01Dart(15)
        pt.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 7)
    }

    @Test
    fun `Should aim to reduce to 4 when the score is less than 8`()
    {
        val model = beastDartsModel()

        val pt = model.throwX01Dart(7)
        pt.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 3)
    }

    @Test
    fun `Should aim for the double when on an even number`()
    {
        val model = beastDartsModel()

        val scores = getCheckoutScores().filter{ it <= 40 }
        scores.forEach {
            val pt = model.throwX01Dart(it)
            pt.segment shouldBe DartboardSegment(SegmentType.DOUBLE, it/2)
        }
    }

    /**
     * Golf behaviour
     */
    @Test
    fun `Should aim for double, treble, treble by default`()
    {
        val model = beastDartsModel()
        model.getSegmentTypeForDartNo(1) shouldBe SegmentType.DOUBLE
        model.getSegmentTypeForDartNo(2) shouldBe SegmentType.TREBLE
        model.getSegmentTypeForDartNo(3) shouldBe SegmentType.TREBLE

        val pt1 = model.throwGolfDart(1, 1)
        pt1.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 1)

        val pt2 = model.throwGolfDart(1, 2)
        pt2.segment shouldBe DartboardSegment(SegmentType.TREBLE, 1)

        val pt3 = model.throwGolfDart(1, 3)
        pt3.segment shouldBe DartboardSegment(SegmentType.TREBLE, 1)
    }

    @Test
    fun `Should respect overridden targets per dart`()
    {
        val hmDartNoToSegmentType = mapOf(1 to SegmentType.TREBLE, 2 to SegmentType.OUTER_SINGLE, 3 to SegmentType.DOUBLE)
        val model = beastDartsModel(hmDartNoToSegmentType = hmDartNoToSegmentType)

        val pt1 = model.throwGolfDart(1, 1)
        pt1.segment shouldBe DartboardSegment(SegmentType.TREBLE, 1)

        val pt2 = model.throwGolfDart(1, 2)
        pt2.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 1)

        val pt3 = model.throwGolfDart(1, 3)
        pt3.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 1)
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
        val hmDartNoToStopThreshold = mapOf(1 to 3, 2 to 4)
        val model = beastDartsModel(hmDartNoToStopThreshold = hmDartNoToStopThreshold)

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

        val pt1 = model.throwClockDart(1, ClockType.Standard)
        pt1.segment shouldBe DartboardSegment(SegmentType.OUTER_SINGLE, 1)

        val pt2 = model.throwClockDart(13, ClockType.Doubles)
        pt2.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 13)

        val pt3 = model.throwClockDart(11, ClockType.Trebles)
        pt3.segment shouldBe DartboardSegment(SegmentType.TREBLE, 11)
    }

    /**
     * Dartzee
     */
    @Test
    fun `Should aim aggressively if less than 2 darts thrown, and cautiously for the final one`()
    {
        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)

        val segmentStatuses = SegmentStatuses(listOf(DartboardSegment(SegmentType.TREBLE, 20)), getAllPossibleSegments())
        val pt1 = model.throwDartzeeDart(0, segmentStatuses)
        pt1.segment shouldBe DartboardSegment(SegmentType.TREBLE, 20)

        val pt2 = model.throwDartzeeDart(1, segmentStatuses)
        pt2.segment shouldBe DartboardSegment(SegmentType.TREBLE, 20)

        val pt3 = model.throwDartzeeDart(2, segmentStatuses)
        pt3.segment shouldBe DartboardSegment(SegmentType.DOUBLE, 25)
    }

    @Test
    fun `Should throw aggressively for the final dart if player is aggressive`()
    {
        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE)

        val segmentStatuses = SegmentStatuses(listOf(DartboardSegment(SegmentType.TREBLE, 20)), getAllPossibleSegments())
        val pt1 = model.throwDartzeeDart(0, segmentStatuses)
        pt1.segment shouldBe DartboardSegment(SegmentType.TREBLE, 20)

        val pt2 = model.throwDartzeeDart(1, segmentStatuses)
        pt2.segment shouldBe DartboardSegment(SegmentType.TREBLE, 20)

        val pt3 = model.throwDartzeeDart(2, segmentStatuses)
        pt3.segment shouldBe DartboardSegment(SegmentType.TREBLE, 20)
    }
}