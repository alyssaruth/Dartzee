package burlton.dartzee.test.screen

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.DartboardSegmentSelector
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.awt.event.MouseEvent
import org.mockito.Mockito.`when` as whenInvoke

class TestDartboardSegmentSelector
{
    @Before
    fun setup()
    {
        Debug.initialise(TestDebug.SimpleDebugOutput())
    }

    @Test
    fun `clicking the same segment should toggle it on and off`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        assertThat(dartboard.selectedSegments, hasSize(equalTo(1)))

        dartboard.dartThrown(pt)
        assertThat(dartboard.selectedSegments, isEmpty)
    }

    @Test
    fun `clicking outside the board should do nothing`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_MISS).first()
        dartboard.dartThrown(pt)

        assertThat(dartboard.selectedSegments, isEmpty)
    }

    @Test
    fun `dragging on the same segment should not toggle it again`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SEGMENT_TYPE_OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        val me = generateMouseEvent(dartboard, 20, SEGMENT_TYPE_OUTER_SINGLE)
        dartboard.mouseDragged(me)

        assertThat(dartboard.selectedSegments, hasSize(equalTo(1)))
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

        assertThat(dartboard.selectedSegments, hasSize(equalTo(2)))
    }

    @Test
    fun `reloaded state should respond to drags`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val segment = DartboardSegmentKt("10_$SEGMENT_TYPE_OUTER_SINGLE")
        dartboard.initState(hashSetOf(segment))

        //Check state has initialised properly
        assertThat(dartboard.selectedSegments, hasSize(equalTo(1)))

        val selectedSegment = dartboard.selectedSegments.first()
        assertThat(selectedSegment.type, equalTo(SEGMENT_TYPE_OUTER_SINGLE))
        assertThat(selectedSegment.score, equalTo(10))

        //Mock a mouse event
        val me = generateMouseEvent(dartboard, 10, SEGMENT_TYPE_OUTER_SINGLE)
        dartboard.mouseDragged(me)

        assertThat(dartboard.selectedSegments, isEmpty)
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: Int): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(10, SEGMENT_TYPE_OUTER_SINGLE).first()

        val me = Mockito.mock(MouseEvent::class.java)
        whenInvoke(me.point).thenReturn(pt)

        return me
    }

}