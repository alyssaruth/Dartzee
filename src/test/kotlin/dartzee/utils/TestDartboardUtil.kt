package dartzee.utils

import dartzee.core.bean.getPointList
import dartzee.helper.AbstractRegistryTest
import dartzee.`object`.SegmentType
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Point

private const val MAX_DARTBOARD_RADIUS = 325

class TestDartboardUtil : AbstractRegistryTest()
{
    override fun getPreferencesAffected(): MutableList<String>
    {
        return mutableListOf(PREFERENCES_STRING_EVEN_SINGLE_COLOUR,
                PREFERENCES_STRING_EVEN_DOUBLE_COLOUR,
                PREFERENCES_STRING_EVEN_TREBLE_COLOUR,
                PREFERENCES_STRING_ODD_SINGLE_COLOUR,
                PREFERENCES_STRING_ODD_DOUBLE_COLOUR,
                PREFERENCES_STRING_ODD_TREBLE_COLOUR)
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
        val xRange = 0..3
        val yRange = 0..3

        val pts = xRange.map { x -> yRange.map { y -> Point(x, y) } }.flatten()
        val edgePts = computeEdgePoints(pts)

        //Corners
        edgePts.shouldContainAll(Point(0, 0), Point(0, 3), Point(3, 0), Point(3, 3))

        //Random other edges
        edgePts.shouldContainAll(
            Point(0, 1), Point(0, 2),
            Point(3, 1), Point(3, 2),
            Point(1, 0), Point(2, 0),
            Point(1, 3), Point(2, 3)
        )

        // Inner points
        edgePts.shouldNotContainAnyOf(Point(1, 1), Point(1, 2), Point(2, 1), Point(2, 2))
    }

    /**         X
     *        X X
     *      X O X
     *    X O O X
     *  X X X X X
     */
    @Test
    fun `Should report edge points correctly - triangle`()
    {
        val xRange = 0..4
        val yRange = 0..4

        val pts = xRange.map { x -> yRange.filter{ it <= x }.map { y -> Point(x, y) } }.flatten()
        val edgePts = computeEdgePoints(pts)

        //Bottom edge
        edgePts.shouldContainAll(Point(0, 0), Point(1, 0), Point(2, 0), Point(3, 0), Point(4, 0))

        //Right edge
        edgePts.shouldContainAll(Point(4, 0), Point(4, 1), Point(4, 2), Point(4, 3), Point(4, 4))

        //Diagonal
        edgePts.shouldContainAll(Point(1, 1), Point(2, 2), Point(3, 3), Point(4, 4))

        //Inner points
        edgePts.shouldNotContainAnyOf(Point(2, 1), Point(3, 1), Point(3, 2))
    }

    @Test
    fun `Should return the correct angles for a score`() {
        getAnglesForScore(20) shouldBe Pair(-9, 9)
        getAnglesForScore(6) shouldBe Pair(81, 99)
        getAnglesForScore(3) shouldBe Pair(171, 189)
        getAnglesForScore(11) shouldBe (Pair(261, 279))

        getAnglesForScore(18) shouldBe Pair(27, 45)
    }

    @Test
    fun `Should return non-overlapping sets of points`() {
        val allPoints = getAllNonMissSegments().flatMap {
            computePointsForSegment(it, Point(0, 0), MAX_DARTBOARD_RADIUS.toDouble())
        }
        allPoints.size shouldBe allPoints.distinct().size
    }

    @Test
    fun `Should not put the origin in any segment that is not the bullseye`() {
        val centre = Point(0, 0)
        val radius = 50.0

        getAllNonMissSegments().filterNot { it.type == SegmentType.DOUBLE && it.score == 25 }.flatMap { segment ->
            val pts = computePointsForSegment(segment, centre, radius)
            withClue("$segment should not contain the origin point") {
                pts.shouldNotContain(Point(0, 0))
            }
        }
    }

    @Test
    fun `Should cover every single point contained within the circle`() {
        val radius = MAX_DARTBOARD_RADIUS
        val centre = Point(radius, radius)

        val allPointsInCircle = getPointList(radius * 2, radius * 2).filter { it.distance(centre) < radius }.toSet()
        val allPointsInSegments = getAllNonMissSegments().flatMap { computePointsForSegment(it, centre, radius.toDouble()) }.toSet()

        val missedPoints = allPointsInCircle - allPointsInSegments
        missedPoints.forEach {
            println("$it: ${factorySegmentForPoint(it, centre, radius * 2.0)}")
        }
        missedPoints.shouldBeEmpty()
    }

    @Test
    fun `Should get consistent results when recalculating segment type`()
    {
        val centre = Point(0, 0)
        val radius = 200.0

        // Remove the outer bull because we currently don't calculate edge points for it correctly (the hole in the middle wrecks it)
        getAllNonMissSegments().filterNot { it.score == 25 && it.type == SegmentType.OUTER_SINGLE }.forEach { segment ->
            val pts = computePointsForSegment(segment, centre, radius)

            pts.forEach { pt ->
                val calculatedSegment = factorySegmentForPoint(pt, centre, radius * 2)

                withClue("$pt should produce the same segment as $segment") {
                    calculatedSegment shouldBe segment
                }
            }
        }
    }

    @Test
    fun testFactorySegmentKeyForPoint()
    {
        clearPreferences()

        resetCachedDartboardValues()

        //Bullseyes
        assertSegment(Point(0, 0), SegmentType.DOUBLE, 25, 2)
        assertSegment(Point(37, 0), SegmentType.DOUBLE, 25, 2)
        assertSegment(Point(38, 0), SegmentType.OUTER_SINGLE, 25, 1)
        assertSegment(Point(48, 55), SegmentType.OUTER_SINGLE, 25, 1)
        assertSegment(Point(0, -93), SegmentType.OUTER_SINGLE, 25, 1)

        //Boundary conditions for varying radius
        assertSegment(Point(0, 94), SegmentType.INNER_SINGLE, 3, 1)
        assertSegment(Point(0, 581), SegmentType.INNER_SINGLE, 3, 1)
        assertSegment(Point(0, -582), SegmentType.TREBLE, 20, 3)
        assertSegment(Point(0, -628), SegmentType.TREBLE, 20, 3)
        assertSegment(Point(629, 0), SegmentType.OUTER_SINGLE, 6, 1)
        assertSegment(Point(-952, 0), SegmentType.OUTER_SINGLE, 11, 1)
        assertSegment(Point(953, 0), SegmentType.DOUBLE, 6, 2)
        assertSegment(Point(0, -999), SegmentType.DOUBLE, 20, 2)
        assertSegment(Point(0, -1000), SegmentType.MISS, 20, 0)
        assertSegment(Point(0, -1299), SegmentType.MISS, 20, 0)

        //Test 45 degrees etc
        assertSegment(Point(100, -100), SegmentType.INNER_SINGLE, 4, 1)
        assertSegment(Point(-100, -100), SegmentType.INNER_SINGLE, 9, 1)
        assertSegment(Point(-100, 100), SegmentType.INNER_SINGLE, 7, 1)
        assertSegment(Point(100, 100), SegmentType.INNER_SINGLE, 15, 1)
    }

    private fun assertSegment(pt: Point, segmentType: SegmentType, score: Int, multiplier: Int)
    {
        val segment = factorySegmentForPoint(pt, Point(0, 0), 2000.0)

        val segmentStr = "" + segment
        segmentStr shouldBe "$score ($segmentType)"

        val drt = getDartForSegment(segment)

        drt.score shouldBe score
        drt.multiplier shouldBe multiplier
        drt.segmentType shouldBe segmentType
    }

    @Test
    fun testResetCachedValues()
    {
        resetCachedDartboardValues()
        val pink = Color.pink
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, DartsColour.toPrefStr(pink))
        assertSegment(Point(0, -629), SegmentType.OUTER_SINGLE, 20, 1)
    }

    @Test
    fun testGetAdjacentNumbersSize()
    {
        for (i in 1..20)
        {
            val adjacents = getAdjacentNumbers(i)
            adjacents.shouldHaveSize(2)
        }
    }

    @Test
    fun testGetAdjacentNumbers()
    {
        val adjacentTo20 = getAdjacentNumbers(20)
        val adjacentTo3 = getAdjacentNumbers(3)
        val adjacentTo6 = getAdjacentNumbers(6)
        val adjacentTo11 = getAdjacentNumbers(11)

        adjacentTo20.shouldContainExactlyInAnyOrder(1, 5)
        adjacentTo3.shouldContainExactlyInAnyOrder(19, 17)
        adjacentTo6.shouldContainExactlyInAnyOrder(10, 13)
        adjacentTo11.shouldContainExactlyInAnyOrder(14, 8)
    }

    @Test
    fun `Should return all numbers within N segments`()
    {
        getNumbersWithinN(20, 1).shouldContainExactly(5, 20, 1)
        getNumbersWithinN(20, 2).shouldContainExactly(12, 5, 20, 1, 18)
        getNumbersWithinN(20, 3).shouldContainExactly(9, 12, 5, 20, 1, 18, 4)
        getNumbersWithinN(20, 4).shouldContainExactly(14, 9, 12, 5, 20, 1, 18, 4, 13)
        getNumbersWithinN(20, 5).shouldContainExactly(11, 14, 9, 12, 5, 20, 1, 18, 4, 13, 6)
    }

    @Test
    fun `Should return the right number of segments`()
    {
        getAllPossibleSegments().size shouldBe (20 * 5) + 2
        getAllNonMissSegments().size shouldBe (20 * 4) + 2
    }
}
