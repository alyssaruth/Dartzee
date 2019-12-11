package burlton.dartzee.test.screen

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.DartboardSegmentSelector
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.test.doubleNineteen
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.test.helpers.makeMouseEvent
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import java.awt.event.MouseEvent
import kotlin.test.assertNotNull

class TestDartboardSegmentSelector: AbstractDartsTest()
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
            dartboard.getColor(it.x, it.y) shouldBe Color.BLACK
        }

        innerPoints.forEach {
            dartboard.getColor(it.x, it.y) shouldBe DartsColour.TRANSPARENT
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
            dartboard.getColor(it.x, it.y) shouldBe Color.BLACK
        }

        innerPoints.forEach {
            dartboard.getColor(it.x, it.y) shouldBe Color.GREEN
        }
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: Int): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(score, segmentType).first()

        return makeMouseEvent(x = pt.x, y = pt.y)
    }

    private fun Dartboard.getColor(x: Int, y: Int) = Color(dartboardImage!!.getRGB(x, y))
}