package dartzee.`object`

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Point

class TestStatefulSegment: AbstractTest()
{
    @Test
    fun `Should support adding points`()
    {
        val segment = StatefulSegment(SegmentType.DOUBLE, 19)

        val points = listOf(Point(1, 0), Point(0, 1), Point(20, -5))
        points.forEach { segment.addPoint(it) }

        segment.points shouldBe points.toMutableSet()
    }

    /**
     * X X X X
     * X O O X
     * X O O X
     * X X X X
     */
    @Test
    fun `Should report edge points correctly - square`()
    {
        val segment = StatefulSegment(SegmentType.DOUBLE, 19)

        val xRange = 0..3
        val yRange = 0..3

        val pts = xRange.map { x -> yRange.map { y -> Point(x, y) } }.flatten()
        pts.forEach { segment.addPoint(it) }
        segment.computeEdgePoints()

        //Corners
        segment.isEdgePoint(Point(0, 0)) shouldBe true
        segment.isEdgePoint(Point(0, 3)) shouldBe true
        segment.isEdgePoint(Point(3, 0)) shouldBe true
        segment.isEdgePoint(Point(3, 3)) shouldBe true

        //Random other edges
        segment.isEdgePoint(Point(0, 1)) shouldBe true
        segment.isEdgePoint(Point(0, 2)) shouldBe true
        segment.isEdgePoint(Point(3, 1)) shouldBe true
        segment.isEdgePoint(Point(3, 2)) shouldBe true
        segment.isEdgePoint(Point(1, 0)) shouldBe true
        segment.isEdgePoint(Point(2, 0)) shouldBe true
        segment.isEdgePoint(Point(1, 3)) shouldBe true
        segment.isEdgePoint(Point(2, 3)) shouldBe true

        //Inner points
        segment.isEdgePoint(Point(1, 1)) shouldBe false
        segment.isEdgePoint(Point(1, 2)) shouldBe false
        segment.isEdgePoint(Point(2, 1)) shouldBe false
        segment.isEdgePoint(Point(2, 2)) shouldBe false

        //Null case
        segment.isEdgePoint(null) shouldBe false
    }

    /**         X
     *        X X
     *      X O X
     *    X O O X
     *  X X X X X
     *
     *  ^ This.
     */
    @Test
    fun `Should report edge points correctly - triangle`()
    {
        val segment = StatefulSegment(SegmentType.DOUBLE, 19)

        val xRange = 0..4
        val yRange = 0..4

        val pts = xRange.map { x -> yRange.filter{ it <= x }.map { y -> Point(x, y) } }.flatten()
        pts.forEach { segment.addPoint(it) }
        segment.computeEdgePoints()

        //Bottom edge
        segment.isEdgePoint(Point(0, 0)) shouldBe true
        segment.isEdgePoint(Point(1, 0)) shouldBe true
        segment.isEdgePoint(Point(2, 0)) shouldBe true
        segment.isEdgePoint(Point(3, 0)) shouldBe true
        segment.isEdgePoint(Point(4, 0)) shouldBe true

        //Right edge
        segment.isEdgePoint(Point(4, 1)) shouldBe true
        segment.isEdgePoint(Point(4, 2)) shouldBe true
        segment.isEdgePoint(Point(4, 3)) shouldBe true
        segment.isEdgePoint(Point(4, 4)) shouldBe true

        //Diagonal
        segment.isEdgePoint(Point(1, 1)) shouldBe true
        segment.isEdgePoint(Point(2, 2)) shouldBe true
        segment.isEdgePoint(Point(3, 3)) shouldBe true

        //Inner points
        segment.isEdgePoint(Point(2, 1)) shouldBe false
        segment.isEdgePoint(Point(3, 1)) shouldBe false
        segment.isEdgePoint(Point(3, 2)) shouldBe false
    }
}