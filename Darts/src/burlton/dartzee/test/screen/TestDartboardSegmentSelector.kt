package burlton.dartzee.test.screen

import burlton.core.code.util.Debug
import burlton.core.test.TestDebug
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
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

        val me = Mockito.mock(MouseEvent::class.java)
        whenInvoke(me.point).thenReturn(pt)
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

        val secondPt = dartboard.getPointsForSegment(19, SEGMENT_TYPE_OUTER_SINGLE).first()

        val me = Mockito.mock(MouseEvent::class.java)
        whenInvoke(me.point).thenReturn(secondPt)
        dartboard.mouseDragged(me)

        assertThat(dartboard.selectedSegments, hasSize(equalTo(2)))
    }

}