package dartzee.screen

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_MISS
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.core.helper.makeMouseEvent
import dartzee.doubleNineteen
import dartzee.getColor
import dartzee.helper.AbstractTest
import dartzee.utils.DartsColour
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import java.awt.event.MouseEvent
import kotlin.test.assertNotNull

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

        val segment = DartboardSegment("10_$SEGMENT_TYPE_OUTER_SINGLE")
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

    @Test
    fun `Should populate selected segments by finding its own copies`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        dartboard.initState(hashSetOf(doubleNineteen))

        val selectedSegment = dartboard.selectedSegments.first()
        assertNotNull(selectedSegment)

        selectedSegment.points.shouldNotBeEmpty()
        selectedSegment.score shouldBe 19
        selectedSegment.type shouldBe SEGMENT_TYPE_DOUBLE
    }

    @Test
    fun `An unselected segment should have an outline and transparent fill`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val doubleNineteenSegment = dartboard.getSegment(19, SEGMENT_TYPE_DOUBLE)
        assertNotNull(doubleNineteenSegment)

        val edgePoints = doubleNineteenSegment.points.filter { doubleNineteenSegment.isEdgePoint(it) }
        val innerPoints = doubleNineteenSegment.points.subtract(edgePoints)

        edgePoints.forEach {
            dartboard.getColor(it) shouldBe Color.BLACK
        }

        innerPoints.forEach {
            dartboard.getColor(it) shouldBe DartsColour.TRANSPARENT
        }
    }

    @Test
    fun `A selected segment should have an outline and the appropriate colour fill`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        dartboard.initState(hashSetOf(doubleNineteen))

        val doubleNineteenSegment = dartboard.getSegment(19, SEGMENT_TYPE_DOUBLE)
        assertNotNull(doubleNineteenSegment)

        val edgePoints = doubleNineteenSegment.points.filter { doubleNineteenSegment.isEdgePoint(it) }
        val innerPoints = doubleNineteenSegment.points.subtract(edgePoints)

        edgePoints.forEach {
            dartboard.getColor(it) shouldBe Color.BLACK
        }

        innerPoints.forEach {
            dartboard.getColor(it) shouldBe Color.GREEN
        }
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: Int): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(score, segmentType).first()

        return makeMouseEvent(x = pt.x, y = pt.y)
    }
}