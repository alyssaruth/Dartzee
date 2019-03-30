package burlton.dartzee.test.screen

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.DartboardSegmentSelector
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.awt.event.MouseEvent

class TestDartboardSegmentSelector: AbstractTest()
{
    @Test
    fun `clicking the same segment should toggle it on and off`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldHaveSize(1)

        dartboard.dartThrown(pt)
        dartboard.selectedSegments.shouldBeEmpty()
    }

    @Test
    fun `clicking outside the board should do nothing`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_MISS).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldBeEmpty()
    }

    @Test
    fun `dragging on the same segment should not toggle it again`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldHaveSize(1)

        val me = generateMouseEvent(dartboard, 20, SEGMENT_TYPE_OUTER_SINGLE)
        dartboard.mouseDragged(me)

        dartboard.selectedSegments.shouldHaveSize(1)
    }

    @Test
    fun `dragging on a new segment should toggle it`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        val me = generateMouseEvent(dartboard, 19, SEGMENT_TYPE_OUTER_SINGLE)
        dartboard.mouseDragged(me)

        dartboard.selectedSegments.shouldHaveSize(2)
    }

    @Test
    fun `reloaded state should respond to drags`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val segment = DartboardSegmentKt("10_$SEGMENT_TYPE_OUTER_SINGLE")
        dartboard.initState(hashSetOf(segment))

        //Check state has initialised properly
        dartboard.selectedSegments.shouldHaveSize(1)

        val selectedSegment = dartboard.selectedSegments.first()
        selectedSegment.type shouldBe SEGMENT_TYPE_OUTER_SINGLE
        selectedSegment.score shouldBe 10

        //Mock a mouse event
        val me = generateMouseEvent(dartboard, 10, SEGMENT_TYPE_OUTER_SINGLE)
        dartboard.mouseDragged(me)

        dartboard.selectedSegments.shouldBeEmpty()
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: Int): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(score, segmentType).first()

        val me = mockk<MouseEvent>()
        every { me.point } returns pt

        return me
    }

}