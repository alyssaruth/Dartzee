package burlton.dartzee.test.`object`

import burlton.dartzee.code.`object`.*
import burlton.dartzee.test.*
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Point

class TestDartboardSegment: AbstractDartsTest()
{
    @Test
    fun `Should return the correct multiplier based on segment type`()
    {
        getMultiplier(SEGMENT_TYPE_DOUBLE) shouldBe 2
        getMultiplier(SEGMENT_TYPE_TREBLE) shouldBe 3
        getMultiplier(SEGMENT_TYPE_OUTER_SINGLE) shouldBe 1
        getMultiplier(SEGMENT_TYPE_INNER_SINGLE) shouldBe 1
        getMultiplier(SEGMENT_TYPE_MISS) shouldBe 0
        getMultiplier(SEGMENT_TYPE_MISSED_BOARD) shouldBe 0
    }

    @Test
    fun `Should return the right golf score based on segment type`()
    {
        getGolfScoreForSegment(SEGMENT_TYPE_DOUBLE) shouldBe 1
        getGolfScoreForSegment(SEGMENT_TYPE_TREBLE) shouldBe 2
        getGolfScoreForSegment(SEGMENT_TYPE_INNER_SINGLE) shouldBe 3
        getGolfScoreForSegment(SEGMENT_TYPE_OUTER_SINGLE) shouldBe 4
        getGolfScoreForSegment(SEGMENT_TYPE_MISS) shouldBe 5
        getGolfScoreForSegment(SEGMENT_TYPE_MISSED_BOARD) shouldBe 5
    }

    @Test
    fun `Should correctly report whether a segment type represents a miss`()
    {
        doubleNineteen.isMiss() shouldBe false
        singleTwenty.isMiss() shouldBe false
        missTwenty.isMiss() shouldBe true
        missedBoard.isMiss() shouldBe true
    }

    @Test
    fun `Should correctly report whether a segment is a double excluding bull`()
    {
        doubleNineteen.isDoubleExcludingBull() shouldBe true
        doubleTwenty.isDoubleExcludingBull() shouldBe true
        bullseye.isDoubleExcludingBull() shouldBe false
        outerBull.isDoubleExcludingBull() shouldBe false
        singleTwenty.isDoubleExcludingBull() shouldBe false
        trebleNineteen.isDoubleExcludingBull() shouldBe false
    }

    @Test
    fun `Should report the correct multiplier`()
    {
        doubleNineteen.getMultiplier() shouldBe 2
        trebleNineteen.getMultiplier() shouldBe 3
        singleTwenty.getMultiplier() shouldBe 1
        missedBoard.getMultiplier() shouldBe 0
        missTwenty.getMultiplier() shouldBe 0
    }

    @Test
    fun `Should compute the segment score correctly`()
    {
        doubleNineteen.getTotal() shouldBe 38
        trebleTwenty.getTotal() shouldBe 60
        singleEighteen.getTotal() shouldBe 18
        missedBoard.getTotal() shouldBe 0
        missTwenty.getTotal() shouldBe 0
    }

    @Test
    fun `Should support adding points`()
    {
        val segment = doubleNineteen.copy()

        val points = listOf(Point(1, 0), Point(0, 1), Point(20, -5))
        points.forEach { segment.addPoint(it) }

        segment.points shouldBe points.toMutableList()
    }

    @Test
    fun `Should report edge points correctly`()
    {
        val segment = doubleNineteen.copy()

        val xRange = 0..5
        val yRange = 0..5

        val pts = xRange.map { x -> yRange.map { y -> Point(x, y) } }.flatten()
        pts.forEach { segment.addPoint(it) }

        //Corners
        segment.isEdgePoint(Point(0, 0)) shouldBe true
        segment.isEdgePoint(Point(0, 5)) shouldBe true
        segment.isEdgePoint(Point(5, 0)) shouldBe true
        segment.isEdgePoint(Point(5, 5)) shouldBe true

        //Random other edges
        segment.isEdgePoint(Point(0, 1)) shouldBe true
        segment.isEdgePoint(Point(1, 0)) shouldBe true

        //Inner points
        segment.isEdgePoint(Point(2, 3)) shouldBe false
        segment.isEdgePoint(Point(2, 2)) shouldBe false
        segment.isEdgePoint(Point(4, 4)) shouldBe false

        //Null case
        segment.isEdgePoint(null) shouldBe false
    }

}