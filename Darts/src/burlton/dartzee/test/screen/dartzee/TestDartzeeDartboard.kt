package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.screen.dartzee.DartzeeDartboard
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.test.*
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color

class TestDartzeeDartboard: AbstractTest()
{
    @Test
    fun `It should fade out invalid segments, but leave valid segments opaque`()
    {
        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)

        val validSegments = listOf(trebleNineteen, bullseye)
        dartboard.refreshValidSegments(validSegments)

        val t19pts = dartboard.getPointsForSegment(19, SEGMENT_TYPE_TREBLE)
        t19pts.forEach { dartboard.getColor(it) shouldBe Color.GREEN }

        val bullPts = dartboard.getPointsForSegment(25, SEGMENT_TYPE_DOUBLE)
        bullPts.forEach { dartboard.getColor(it) shouldBe Color.RED }

        val outerBullPts = dartboard.getPointsForSegment(25, SEGMENT_TYPE_OUTER_SINGLE)
        outerBullPts.forEach { dartboard.getColor(it) shouldBe Color(0, 255, 0, 20) }
    }

    @Test
    fun `Should fade out miss segments if valid segments doesn't contain a miss`()
    {
        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(listOf(trebleNineteen))

        val missTwentyPts = dartboard.getPointsForSegment(20, SEGMENT_TYPE_MISS)
        missTwentyPts.forEach { dartboard.getColor(it) shouldBe Color(0, 0, 0, 20) }
    }

    @Test
    fun `Should not fade out miss segments if valid segments contains a miss`()
    {
        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(listOf(trebleNineteen, missTwenty))

        val missTwentyPts = dartboard.getPointsForSegment(20, SEGMENT_TYPE_MISS)
        missTwentyPts.forEach { dartboard.getColor(it) shouldBe Color.BLACK }
    }

    @Test
    fun `Should not highlight invalid segments on hover`()
    {
        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(listOf(trebleNineteen))

        dartboard.ensureListening()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_DOUBLE).first()
        dartboard.highlightDartboard(pt)

        dartboard.getColor(pt) shouldBe Color(255, 0, 0, 20)
    }

    @Test
    fun `Should highlight valid segments on hover`()
    {
        val dartboard = DartzeeDartboard(100, 100)
        dartboard.paintDartboard(DEFAULT_COLOUR_WRAPPER)
        dartboard.refreshValidSegments(listOf(doubleTwenty))

        dartboard.ensureListening()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_DOUBLE).first()
        dartboard.highlightDartboard(pt)

        dartboard.getColor(pt) shouldBe DartsColour.getDarkenedColour(Color.RED)
    }
}