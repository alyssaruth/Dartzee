package dartzee.ai

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
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
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test

class TestDartsAiModelMk2: AbstractTest()
{
    @Test
    fun `Should successfully serialize and deserialize`()
    {
        val setupDarts = mapOf(57 to AimDart(17, 1), 97 to AimDart(19, 3))
        val model = DartsAiModelMk2(50.0, 40.0, null, 20, setupDarts, 17, mapOf(), mapOf(), DartzeePlayStyle.CAUTIOUS)

        val result = model.toJson()

        println(result)

        val model2 = DartsAiModelMk2.fromJson(result)
        model shouldBe model2
    }

    /**
     * X01 test
     */
    @Test
    fun `Should aim for the overridden value if one is set for the current setup score`()
    {
        val model = beastDartsModel(hmScoreToDart = mapOf(77 to AimDart(17, 2)))

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
        val model = beastDartsModel(scoringDart = 18)

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
        val model = beastDartsModel(scoringDart = 25)

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
        val model = beastDartsModel(scoringDart = 25)

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
        val hmDartNoToSegmentType = mapOf(1 to SegmentType.TREBLE, 2 to SegmentType.OUTER_SINGLE, 3 to SegmentType.DOUBLE)
        val model = beastDartsModel(hmDartNoToSegmentType = hmDartNoToSegmentType)

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
        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)

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
        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE)

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
}