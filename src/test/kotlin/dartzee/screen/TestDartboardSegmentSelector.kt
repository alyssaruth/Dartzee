package dartzee.screen

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
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
        withSegmentSelectDartboard { dartboard ->
            val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
            dartboard.dartThrown(pt)

            dartboard.selectedSegments.shouldHaveSize(1)

            dartboard.dartThrown(pt)
            dartboard.selectedSegments.shouldBeEmpty()
        }
    }

    @Test
    fun `clicking outside the board should do nothing`()
    {
        withSegmentSelectDartboard { dartboard ->

            val pt = dartboard.getPointsForSegment(20, SegmentType.MISS).first()
            dartboard.dartThrown(pt)

            dartboard.selectedSegments.shouldBeEmpty()
        }
    }

    @Test
    fun `dragging on the same segment should not toggle it again`()
    {
        withSegmentSelectDartboard { dartboard ->
            val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
            dartboard.dartThrown(pt)

            dartboard.selectedSegments.shouldHaveSize(1)

            val me = generateMouseEvent(dartboard, 20, SegmentType.OUTER_SINGLE)
            dartboard.mouseDragged(me)

            dartboard.selectedSegments.shouldHaveSize(1)
        }
    }

    @Test
    fun `dragging on a new segment should toggle it`()
    {
        withSegmentSelectDartboard { dartboard ->
            val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
            dartboard.dartThrown(pt)

            val me = generateMouseEvent(dartboard, 19, SegmentType.OUTER_SINGLE)
            dartboard.mouseDragged(me)

            dartboard.selectedSegments.shouldHaveSize(2)
        }
    }

    @Test
    fun `reloaded state should respond to drags`()
    {
        withSegmentSelectDartboard { dartboard ->
            val segment = DartboardSegment(SegmentType.OUTER_SINGLE, 10)
            dartboard.initState(hashSetOf(segment))

            //Check state has initialised properly
            dartboard.selectedSegments.shouldHaveSize(1)

            val selectedSegment = dartboard.selectedSegments.first()
            selectedSegment.type shouldBe SegmentType.OUTER_SINGLE
            selectedSegment.score shouldBe 10

            //Mock a mouse event
            val me = generateMouseEvent(dartboard, 10, SegmentType.OUTER_SINGLE)
            dartboard.mouseDragged(me)

            dartboard.selectedSegments.shouldBeEmpty()
        }
    }

    @Test
    fun `Should populate selected segments by finding its own copies`()
    {
        withSegmentSelectDartboard { dartboard ->
            dartboard.initState(hashSetOf(doubleNineteen))

            val selectedSegment = dartboard.selectedSegments.first()
            assertNotNull(selectedSegment)

            selectedSegment.points.shouldNotBeEmpty()
            selectedSegment.score shouldBe 19
            selectedSegment.type shouldBe SegmentType.DOUBLE
        }
    }

    @Test
    fun `An unselected segment should have an outline and transparent fill`()
    {
        withSegmentSelectDartboard { dartboard ->
            val doubleNineteenSegment = dartboard.getSegment(19, SegmentType.DOUBLE)
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
    }

    @Test
    fun `A selected segment should have an outline and the appropriate colour fill`()
    {
        withSegmentSelectDartboard { dartboard ->
            dartboard.initState(hashSetOf(doubleNineteen))

            val doubleNineteenSegment = dartboard.getSegment(19, SegmentType.DOUBLE)
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
    }

    private fun withSegmentSelectDartboard(testFn: (dartboard: DartboardSegmentSelector) -> Unit)
    {
        val dartboard = DartboardSegmentSelector(100, 100)

        try
        {
            dartboard.paintDartboard()
            testFn(dartboard)
        }
        finally
        {
            dartboard.cleanUp()
        }
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: SegmentType): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(score, segmentType).first()

        return makeMouseEvent(x = pt.x, y = pt.y)
    }
}