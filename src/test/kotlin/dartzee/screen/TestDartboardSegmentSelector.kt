package dartzee.screen

import com.github.alexburlton.swingtest.makeMouseEvent
import dartzee.doubleNineteen
import dartzee.getColor
import dartzee.helper.AbstractTest
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.singleNineteen
import dartzee.trebleTwenty
import dartzee.utils.DartsColour
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getAverage
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.event.MouseEvent

class TestDartboardSegmentSelector: AbstractTest()
{
    @Test
    fun `clicking the same segment should toggle it on and off`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldHaveSize(1)

        dartboard.dartThrown(pt)
        dartboard.selectedSegments.shouldBeEmpty()
    }

    @Test
    fun `should be able to select all`()
    {
        val allSegments = getAllNonMissSegments()
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()
        dartboard.initState(setOf(doubleNineteen, trebleTwenty))

        dartboard.selectAll()
        dartboard.selectedSegments shouldBe allSegments.toSet()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        dartboard.dartThrown(pt)
        dartboard.selectAll()
        dartboard.selectedSegments shouldBe allSegments.toSet()
    }

    @Test
    fun `should be able to select none`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()
        dartboard.initState(setOf(doubleNineteen, trebleTwenty))

        dartboard.selectNone()
        dartboard.selectedSegments.shouldBeEmpty()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        dartboard.dartThrown(pt)
        dartboard.selectNone()
        dartboard.selectedSegments.shouldBeEmpty()
    }

    @Test
    fun `clicking outside the board should do nothing`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.MISS).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldBeEmpty()
    }

    @Test
    fun `dragging on the same segment should not toggle it again`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        dartboard.selectedSegments.shouldHaveSize(1)

        val me = generateMouseEvent(dartboard, 20, SegmentType.OUTER_SINGLE)
        dartboard.mouseDragged(me)

        dartboard.selectedSegments.shouldHaveSize(1)
    }

    @Test
    fun `dragging on a new segment should toggle it`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val pt = dartboard.getPointsForSegment(20, SegmentType.OUTER_SINGLE).first()
        dartboard.dartThrown(pt)

        val me = generateMouseEvent(dartboard, 19, SegmentType.OUTER_SINGLE)
        dartboard.mouseDragged(me)

        dartboard.selectedSegments.shouldHaveSize(2)
    }

    @Test
    fun `reloaded state should respond to drags`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

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

    @Test
    fun `Should populate selected segments by finding its own copies`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        dartboard.initState(hashSetOf(singleNineteen))

        val selectedSegment = dartboard.selectedSegments.first()
        selectedSegment.shouldNotBeNull()

        val pt = getAverage(dartboard.getPointsForSegment(19, SegmentType.OUTER_SINGLE))
        val img = dartboard.dartboardImage!!

        Color(img.getRGB(pt.x, pt.y)) shouldBe Color.WHITE
        selectedSegment.score shouldBe 19
        selectedSegment.type shouldBe SegmentType.OUTER_SINGLE
    }

    @Test
    fun `An unselected segment should have an outline and transparent fill`()
    {
        val dartboard = DartboardSegmentSelector(100, 100)
        dartboard.paintDartboard()

        val doubleNineteenSegment = dartboard.getSegment(19, SegmentType.DOUBLE)
        doubleNineteenSegment.shouldNotBeNull()

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

        val doubleNineteenSegment = dartboard.getSegment(19, SegmentType.DOUBLE)
        doubleNineteenSegment.shouldNotBeNull()

        val edgePoints = doubleNineteenSegment.points.filter { doubleNineteenSegment.isEdgePoint(it) }
        val innerPoints = doubleNineteenSegment.points.subtract(edgePoints)

        edgePoints.forEach {
            dartboard.getColor(it) shouldBe Color.BLACK
        }

        innerPoints.forEach {
            dartboard.getColor(it) shouldBe Color.GREEN
        }
    }

    private fun generateMouseEvent(dartboard: Dartboard, score: Int, segmentType: SegmentType): MouseEvent
    {
        val pt = dartboard.getPointsForSegment(score, segmentType).first()

        return makeMouseEvent(dartboard, x = pt.x, y = pt.y)
    }
}