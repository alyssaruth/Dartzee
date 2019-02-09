package burlton.dartzee.test

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.utils.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Point
import kotlin.test.assertTrue

class TestDartboardUtil : DartsRegistry
{

    @Test
    fun testFactorySegmentKeyForPoint()
    {
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_EVEN_SINGLE_COLOUR)
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_EVEN_DOUBLE_COLOUR)
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_EVEN_TREBLE_COLOUR)
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_ODD_SINGLE_COLOUR)
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_ODD_DOUBLE_COLOUR)
        PreferenceUtil.deleteSetting(DartsRegistry.PREFERENCES_STRING_ODD_TREBLE_COLOUR)

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
        assertSegment(Point(0, -1300), SEGMENT_TYPE_MISSED_BOARD, 20, 0, null)

        //Test 45 degrees etc
        assertSegment(Point(100, -100), SEGMENT_TYPE_INNER_SINGLE, 4, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(-100, -100), SEGMENT_TYPE_INNER_SINGLE, 9, 1, DartsColour.DARTBOARD_WHITE)
        assertSegment(Point(-100, 100), SEGMENT_TYPE_INNER_SINGLE, 7, 1, DartsColour.DARTBOARD_BLACK)
        assertSegment(Point(100, 100), SEGMENT_TYPE_INNER_SINGLE, 15, 1, DartsColour.DARTBOARD_WHITE)
    }

    private fun assertSegment(pt: Point, segmentType: Int, score: Int, multiplier: Int, expectedColor: Color?)
    {
        val key = factorySegmentKeyForPoint(pt, Point(0, 0), 2000.0)
        val expectedKey = score.toString() + "_" + segmentType
        Assert.assertEquals("SegmentKey for point $pt for dartboard with radius 1000", expectedKey, key)

        val segment = DartboardSegmentKt(key)

        val segmentStr = "" + segment
        Assert.assertEquals("DartboardSegment.toString() for point $pt", score.toString() + " (" + segmentType + ")", segmentStr)

        val drt = getDartForSegment(pt, segment)

        Assert.assertEquals("Dart score for point $pt", score.toLong(), drt.score.toLong())
        Assert.assertEquals("Dart multiplier for point $pt", multiplier.toLong(), drt.multiplier.toLong())
        Assert.assertEquals("SegmentType for point $pt", segmentType.toLong(), drt.segmentType.toLong())

        assertColourForPointAndSegment(pt, segment, null, expectedColor, false)
    }

    @Test
    fun testResetCachedValues()
    {
        resetCachedDartboardValues()
        val pink = Color.pink
        PreferenceUtil.saveString(DartsRegistry.PREFERENCES_STRING_EVEN_SINGLE_COLOUR, DartsColour.toPrefStr(pink))
        assertSegment(Point(0, -629), SEGMENT_TYPE_OUTER_SINGLE, 20, 1, pink)
    }


    @Test
    fun testHighlights()
    {
        val wrapper = DEFAULT_COLOUR_WRAPPER
        val segment = DartboardSegmentKt("20_4")

        assertColourForPointAndSegment(Point(0, 0), segment, wrapper, Color.BLACK, false)
        assertColourForPointAndSegment(Point(0, 0), segment, wrapper, Color.BLACK.darker().darker(), true)
    }

    @Test
    fun testWireframe()
    {
        val wrapper = ColourWrapper(DartsColour.TRANSPARENT)
        wrapper.edgeColour = Color.YELLOW

        val fakeSegment = DartboardSegmentKt("20_1")
        for (x in 0..200)
        {
            for (y in 0..200)
            {
                fakeSegment.addPoint(Point(x, y))
            }
        }

        val pointsTotal = fakeSegment.points.size
        Assert.assertEquals("Total points in [0, 200] x [0, 200]", 40401, pointsTotal.toLong())

        //Four corners and four edge mid-points
        assertColourForPointAndSegment(Point(0, 0), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(0, 100), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(100, 0), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(0, 200), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(200, 0), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(200, 100), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(100, 200), fakeSegment, wrapper, Color.YELLOW, false)
        assertColourForPointAndSegment(Point(200, 200), fakeSegment, wrapper, Color.YELLOW, false)

        //Non-edge boundary cases
        assertColourForPointAndSegment(Point(1, 1), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)
        assertColourForPointAndSegment(Point(199, 1), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)
        assertColourForPointAndSegment(Point(1, 199), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)
        assertColourForPointAndSegment(Point(199, 199), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)

        //Another non-edge. Let's say we'll highlight this one, to check theres no NPE
        assertColourForPointAndSegment(Point(100, 100), fakeSegment, wrapper, DartsColour.TRANSPARENT, true)

        //Now assign this to be a "miss" segment. We should no longer get the wireframe, even for an edge
        fakeSegment.type = SEGMENT_TYPE_MISS
        assertColourForPointAndSegment(Point(0, 0), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)
        assertColourForPointAndSegment(Point(1, 1), fakeSegment, wrapper, DartsColour.TRANSPARENT, false)
    }

    private fun assertColourForPointAndSegment(pt: Point, segment: DartboardSegmentKt, wrapper: ColourWrapper?, expected: Color?, highlight: Boolean)
    {
        val color = getColourForPointAndSegment(pt, segment, highlight, wrapper)
        Assert.assertEquals("Color for point $pt and segment $segment", expected, color)
    }

    @Test
    fun testGetAdjacentNumbersSize()
    {
        for (i in 1..20)
        {
            val adjacents = getAdjacentNumbers(i)

            assertThat(adjacents.size, equalTo(2))
        }
    }

    @Test
    fun testGetAdjacentNumbers()
    {
        val adjacentTo20 = getAdjacentNumbers(20)
        val adjacentTo3 = getAdjacentNumbers(3)
        val adjacentTo6 = getAdjacentNumbers(6)
        val adjacentTo11 = getAdjacentNumbers(11)

        assertTrue(adjacentTo20.contains(1))
        assertTrue(adjacentTo20.contains(5))
        assertTrue(adjacentTo3.contains(19))
        assertTrue(adjacentTo3.contains(17))
        assertTrue(adjacentTo6.contains(10))
        assertTrue(adjacentTo6.contains(13))
        assertTrue(adjacentTo11.contains(14))
        assertTrue(adjacentTo11.contains(8))
    }

}
