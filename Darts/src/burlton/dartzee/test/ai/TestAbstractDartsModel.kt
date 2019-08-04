package burlton.dartzee.test.ai

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.CLOCK_TYPE_DOUBLES
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.CLOCK_TYPE_TREBLES
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.utils.getCheckoutScores
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test
import org.w3c.dom.Element
import java.awt.Point

class TestAbstractDartsModel: AbstractDartsTest()
{
    /**
     * Serialisation
     */
    @Test
    fun `Should serialise and deserialise correctly with default values`()
    {
        val model = DummyDartsModel()

        val xml = model.writeXml()

        val newModel = DummyDartsModel()
        newModel.readXml(xml)

        model.scoringDart shouldBe newModel.scoringDart
        model.hmScoreToDart shouldContainExactly newModel.hmScoreToDart
        model.hmDartNoToSegmentType shouldContainExactly newModel.hmDartNoToSegmentType
        model.hmDartNoToStopThreshold shouldContainExactly newModel.hmDartNoToStopThreshold
        model.mercyThreshold shouldBe newModel.mercyThreshold
        model.foo shouldBe newModel.foo
    }

    @Test
    fun `Should serialise and deserialise non-defaults correctly`()
    {
        val model = DummyDartsModel()
        model.scoringDart = 25
        model.hmScoreToDart[50] = Dart(25, 2)
        model.hmScoreToDart[60] = Dart(10, 1)
        model.hmDartNoToSegmentType[1] = SEGMENT_TYPE_TREBLE
        model.hmDartNoToStopThreshold[1] = 1
        model.hmDartNoToStopThreshold[2] = 2
        model.mercyThreshold = 18
        model.foo = "bar"

        val xml = model.writeXml()

        val newModel = DummyDartsModel()
        newModel.readXml(xml)

        newModel.scoringDart shouldBe 25
        newModel.mercyThreshold shouldBe 18
        newModel.foo shouldBe "bar"
        model.hmScoreToDart shouldContainExactly newModel.hmScoreToDart
        model.hmDartNoToSegmentType shouldContainExactly newModel.hmDartNoToSegmentType
        model.hmDartNoToStopThreshold shouldContainExactly newModel.hmDartNoToStopThreshold
    }

    /**
     * X01 test
     */
    @Test
    fun `Should aim for the overridden value if one is set for the current setup score`()
    {
        val model = DummyDartsModel()
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
        val model = DummyDartsModel()
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
        val model = DummyDartsModel()
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
        val model = DummyDartsModel()
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
        val model = DummyDartsModel()

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
        val model = DummyDartsModel()

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
        val model = DummyDartsModel()

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
        val model = DummyDartsModel()

        val dartboard = Dartboard(100, 100)
        dartboard.paintDartboard()

        val listener = mockk<DartboardListener>(relaxed = true)
        dartboard.addDartboardListener(listener)

        model.throwX01Dart(7, dartboard)
        verify { listener.dartThrown(Dart(3, 1))}
    }

    @Test
    fun `Should aim for the double when on an even number`()
    {
        val model = DummyDartsModel()

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
        val model = DummyDartsModel()

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
        val model = DummyDartsModel()
        model.hmDartNoToSegmentType[1] = SEGMENT_TYPE_TREBLE
        model.hmDartNoToSegmentType[2] = SEGMENT_TYPE_OUTER_SINGLE
        model.hmDartNoToSegmentType[3] = SEGMENT_TYPE_DOUBLE

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
        val model = DummyDartsModel()

        model.getStopThresholdForDartNo(1) shouldBe 2
        model.getStopThresholdForDartNo(2) shouldBe 3
    }

    @Test
    fun `Overridden stop thresholds should be adhered to`()
    {
        val model = DummyDartsModel()
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
        val model = DummyDartsModel()

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
     * Misc
     */
    @Test
    fun `Should aim at the average point for the relevant segment`()
    {
        val dartboard = FudgedDartboard()
        dartboard.paintDartboard()

        val model = DummyDartsModel()
        model.getPointForScore(Dart(20, 3), dartboard) shouldBe Point(3, 4)
    }

    class FudgedDartboard : Dartboard(100, 100)
    {
        override fun paintDartboard(colourWrapper: ColourWrapper?, listen: Boolean, cached: Boolean)
        {
            super.paintDartboard(colourWrapper, listen, cached)

            val segmentKey = "20_$SEGMENT_TYPE_TREBLE"
            val segment = DartboardSegmentKt(segmentKey)
            segment.addPoint(Point(1, 7))
            segment.addPoint(Point(3, 3))
            segment.addPoint(Point(5, 2))

            hmSegmentKeyToSegment[segmentKey] = segment
        }
    }

    class DummyDartsModel: AbstractDartsModel()
    {
        var foo = ""

        override fun getModelName() = "Test"
        override fun getType() = 100
        override fun writeXmlSpecific(rootElement: Element) { rootElement.setAttribute("Foo", foo)}
        override fun readXmlSpecific(root: Element) { foo = root.getAttribute("Foo")}
        override fun throwDartAtPoint(pt: Point, dartboard: Dartboard) = pt
        override fun getProbabilityWithinRadius(radius: Double) = 1.0
    }
}