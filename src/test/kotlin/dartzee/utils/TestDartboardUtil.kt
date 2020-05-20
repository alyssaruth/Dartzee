package dartzee.utils

import dartzee.`object`.*
import dartzee.helper.AbstractRegistryTest
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Color
import java.awt.Point

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

    @Test
    fun testFactorySegmentKeyForPoint()
    {
        clearPreferences()

        resetCachedDartboardValues()

        //Bullseyes
        assertSegment(Point(0, 0), SEGMENT_TYPE_DOUBLE, 25, 2, DartsColour.DARTBOARD_RED)
        assertSegment(Point(37, 0), SEGMENT_TYPE_DOUBLE, 25, 2, DartsColour.DARTBOARD_RED)
        assertSegment(Point(38, 0), SEGMENT_TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN)
        assertSegment(Point(48, 55), SEGMENT_TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN)
        assertSegment(Point(0, -93), SEGMENT_TYPE_OUTER_SINGLE, 25, 1, DartsColour.DARTBOARD_GREEN)

        //Boundary conditions for varying radius
        assertSegment(Point(0, 94), SEGMENT_TYPE_INNER_SINGLE, 3, 1, DartsColour.DARTBOARD_BLACK)
        assertSegment(Point(0, 581), SEGMENT_TYPE_INNER_SINGLE, 3, 1, DartsColour.DARTBOARD_BLACK)
        assertSegment(Point(0, -582), SEGMENT_TYPE_TREBLE, 20, 3, DartsColour.DARTBOARD_RED)
        assertSegment(Point(0, -628), SEGMENT_TYPE_TREBLE, 20, 3, DartsColour.DARTBOARD_RED)
        assertSegment(Point(629, 0), SEGMENT_TYPE_OUTER_SINGLE, 6, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(-952, 0), SEGMENT_TYPE_OUTER_SINGLE, 11, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(953, 0), SEGMENT_TYPE_DOUBLE, 6, 2, DartsColour.DARTBOARD_GREEN)
        assertSegment(Point(0, -999), SEGMENT_TYPE_DOUBLE, 20, 2, DartsColour.DARTBOARD_RED)
        assertSegment(Point(0, -1000), SEGMENT_TYPE_MISS, 20, 0, Color.black)
        assertSegment(Point(0, -1299), SEGMENT_TYPE_MISS, 20, 0, Color.black)
        assertSegment(Point(0, -1300), SEGMENT_TYPE_MISSED_BOARD, 20, 0, DartsColour.TRANSPARENT)

        //Test 45 degrees etc
        assertSegment(Point(100, -100), SEGMENT_TYPE_INNER_SINGLE, 4, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(-100, -100), SEGMENT_TYPE_INNER_SINGLE, 9, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(-100, 100), SEGMENT_TYPE_INNER_SINGLE, 7, 1, DartsColour.DARTBOARD_BLACK)
        assertSegment(Point(100, 100), SEGMENT_TYPE_INNER_SINGLE, 15, 1, DartsColour.DARTBOARD_WHITE)
    }

    private fun assertSegment(pt: Point, segmentType: Int, score: Int, multiplier: Int, expectedColor: Color)
    {
        val segment = factorySegmentForPoint(pt, Point(0, 0), 2000.0)

        val segmentStr = "" + segment
        segmentStr shouldBe "$score ($segmentType)"

        val drt = getDartForSegment(pt, segment)

        drt.score shouldBe score
        drt.multiplier shouldBe multiplier
        drt.segmentType shouldBe segmentType

        assertColourForPointAndSegment(pt, segment, null, expectedColor)
    }

    @Test
    fun testResetCachedValues()
    {
        resetCachedDartboardValues()
        val pink = Color.pink
        PreferenceUtil.saveString(PREFERENCES_STRING_EVEN_SINGLE_COLOUR, DartsColour.toPrefStr(pink))
        assertSegment(Point(0, -629), SEGMENT_TYPE_OUTER_SINGLE, 20, 1, pink)
    }

    @Test
    fun testWireframe()
    {
        val wrapper = ColourWrapper(DartsColour.TRANSPARENT)
        wrapper.edgeColour = Color.YELLOW

        val fakeSegment = DartboardSegment(SegmentType.OUTER_SINGLE, 20)
        for (x in 0..200)
        {
            for (y in 0..200)
            {
                fakeSegment.addPoint(Point(x, y))
            }
        }

        fakeSegment.points.shouldHaveSize(40401)

        //Four corners and four edge mid-points
        assertColourForPointAndSegment(Point(0, 0), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(0, 100), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(100, 0), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(0, 200), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(200, 0), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(200, 100), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(100, 200), fakeSegment, wrapper, Color.YELLOW)
        assertColourForPointAndSegment(Point(200, 200), fakeSegment, wrapper, Color.YELLOW)

        //Non-edge boundary cases
        assertColourForPointAndSegment(Point(1, 1), fakeSegment, wrapper, DartsColour.TRANSPARENT)
        assertColourForPointAndSegment(Point(199, 1), fakeSegment, wrapper, DartsColour.TRANSPARENT)
        assertColourForPointAndSegment(Point(1, 199), fakeSegment, wrapper, DartsColour.TRANSPARENT)
        assertColourForPointAndSegment(Point(199, 199), fakeSegment, wrapper, DartsColour.TRANSPARENT)

        //Another non-edge. Let's say we'll highlight this one, to check theres no NPE
        assertColourForPointAndSegment(Point(100, 100), fakeSegment, wrapper, DartsColour.TRANSPARENT)

        //Now assign this to be a "miss" segment. We should no longer get the wireframe, even for an edge
        val missSegment = DartboardSegment(SegmentType.MISS, 20)
        for (x in 0..200)
        {
            for (y in 0..200)
            {
                missSegment.addPoint(Point(x, y))
            }
        }

        assertColourForPointAndSegment(Point(0, 0), missSegment, wrapper, DartsColour.TRANSPARENT)
        assertColourForPointAndSegment(Point(1, 1), missSegment, wrapper, DartsColour.TRANSPARENT)
    }

    private fun assertColourForPointAndSegment(pt: Point, segment: DartboardSegment, wrapper: ColourWrapper?, expected: Color)
    {
        val color = getColourForPointAndSegment(pt, segment, wrapper)
        color shouldBe expected
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

        adjacentTo20.shouldContainAll(1, 5)
        adjacentTo3.shouldContainAll(19, 17)
        adjacentTo6.shouldContainAll(10, 13)
        adjacentTo11.shouldContainAll(14, 8)
    }

    @Test
    fun `Should return the right number of segments`()
    {
        getAllPossibleSegments().size shouldBe (20 * 6) + 2
    }

}
