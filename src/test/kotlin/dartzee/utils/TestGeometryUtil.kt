package dartzee.utils

import dartzee.helper.AbstractTest
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import java.awt.Point
import org.junit.jupiter.api.Test

/**
 * My first unit test. Woop!
 *
 * 20/04/2018 - for posterity... 14/01/2019 - Now brought to you with Kotlin! 23/04/2019 - Class
 * under test is now Kotlin, and this now uses Kotlintest
 */
class TestGeometryUtil : AbstractTest() {
    @Test
    fun testTranslatePoint() {
        assertPointTranslation(5.0, 0.0, Point(0, -5))
        assertPointTranslation(3.0, 90.0, Point(3, 0))
        assertPointTranslation(-3.0, 90.0, Point(-3, 0))
        assertPointTranslation(3.0, 270.0, Point(-3, 0))
        assertPointTranslation(47.0, 180.0, Point(0, 47))

        assertPointTranslation(5.0, 36.87, Point(3, -4))
        assertPointTranslation(5.0, 53.13, Point(4, -3))
        assertPointTranslation(5.0, 126.87, Point(4, 3))
        assertPointTranslation(5.0, 216.87, Point(-3, 4))
        assertPointTranslation(5.0, 306.87, Point(-4, -3))
        assertPointTranslation(5.0, -53.13, Point(-4, -3))

        // Edge case. We'll mod the angle so we translate it as if it were 0
        assertPointTranslation(100.0, 720.0, Point(0, -100))
    }

    private fun assertPointTranslation(radius: Double, degrees: Double, expected: Point) {
        val result = translatePoint(Point(0, 0), radius, degrees)
        result shouldBe expected
    }

    /**
     * Measures the angle like below:
     *
     * | X | X |^^ X | X | X |X
     * -------------------
     */
    @Test
    fun testGetAngleForPoint() {
        val centerPt = Point(0, 0)

        // Go around in 45 degree increments
        assertAngle(Point(1, 0), centerPt, 90.0)
        assertAngle(Point(1, 1), centerPt, 45.0)
        assertAngle(Point(0, 1), centerPt, 0.0)
        assertAngle(Point(-1, 1), centerPt, 315.0)
        assertAngle(Point(-1, 0), centerPt, 270.0)
        assertAngle(Point(-1, -1), centerPt, 225.0)
        assertAngle(Point(0, -1), centerPt, 180.0)
        assertAngle(Point(1, -1), centerPt, 135.0)

        // Some inexact examples
        assertAngle(Point(1, 2), centerPt, 26.565)
        assertAngle(Point(1, 5), centerPt, 11.310)
    }

    private fun assertAngle(pt: Point, centerPt: Point, expected: Double) {
        // Because Java points are stupid, and so "increasing" y takes you downwards.
        // We've passed in the intuitive value. Transform it to java in here.
        pt.setLocation(pt.getX(), -pt.getY())

        getAngleForPoint(pt, centerPt).shouldBeBetween(expected - 0.01, expected + 0.01, 0.0)
    }

    @Test
    fun testGetAverage() {
        assertAverage(Point(0, 0), Point(0, 0))
        assertAverage(Point(0, 0), Point(1, 0), Point(-1, 0))
        assertAverage(Point(1, 1), Point(0, 0), Point(0, 2), Point(2, 0), Point(2, 2))
    }

    private fun assertAverage(expected: Point, vararg points: Point) {
        val list = points.toSet()
        getAverage(list) shouldBe expected
    }

    @Test
    fun testGenerateRandomAngle() {
        repeat(999) {
            val angle = generateRandomAngle()
            angle.shouldBeGreaterThanOrEqual(0.0)
            angle.shouldBeLessThanOrEqual(360.0)
        }
    }
}
