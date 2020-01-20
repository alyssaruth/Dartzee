package burlton.dartzee.code.utils

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.`object`.Quadrant
import java.awt.Point
import java.util.*

private val TOP_RIGHT = Quadrant(0, 90, true, yIsPositive = false)
private val BOTTOM_RIGHT = Quadrant(90, 180, true, yIsPositive = true)
private val BOTTOM_LEFT = Quadrant(180, 270, false, yIsPositive = true)
private val TOP_LEFT = Quadrant(270, 360, false, yIsPositive = false)

private val QUADRANTS = arrayOf(TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT)

fun translatePoint(pt: Point, radius: Double, degrees: Double, logging: Boolean): Point
{
    Debug.appendBanner("Translating $pt by $radius at angle $degrees", logging)

    val quadrant = getQuadrantForAngle(degrees) ?: return translatePointAlongAxis(pt, radius, degrees, logging)

    //Need radians for trig functions
    val theta = Math.toRadians(degrees)
    val dSin = Math.abs(radius * Math.sin(theta))
    val dCos = Math.abs(radius * Math.cos(theta))

    Debug.appendWithoutDate("dSin = $dSin, dCos = $dCos", logging)

    var x = dSin
    var y = dCos

    if (!quadrant.xIsPositive)
    {
        x *= -1.0
    }

    if (!quadrant.yIsPositive)
    {
        y *= -1.0
    }

    Debug.appendWithoutDate("Translating x: $x", logging)
    Debug.appendWithoutDate("Translating y: $y", logging)

    x += pt.getX()
    y += pt.getY()

    val ret = Point()
    ret.setLocation(x, y)

    Debug.appendWithoutDate("New point: $ret", logging)
    return ret
}

private fun translatePointAlongAxis(pt: Point, radius: Double, degrees: Double, logging: Boolean): Point
{
    Debug.appendWithoutDate("Translating along axis", logging)

    val x = pt.getX()
    val y = pt.getY()

    val ret = Point()
    when (degrees)
    {
        0.0 -> ret.setLocation(x, y - radius)
        90.0 -> ret.setLocation(x + radius, y)
        180.0 -> ret.setLocation(x, y + radius)
        270.0 -> ret.setLocation(x - radius, y)
    }

    Debug.appendWithoutDate("New point: $ret", logging)
    return ret
}

fun getDistance(dartPt: Point, centerPt: Point): Double
{
    val xLength = Math.abs(dartPt.getX() - centerPt.getX())
    val yLength = Math.abs(dartPt.getY() - centerPt.getY())
    return Math.sqrt(xLength * xLength + yLength * yLength)
}

/**
 * Compute the clockwise angle for the point, relative to the center
 */
fun getAngleForPoint(dartPt: Point, centerPt: Point): Double
{
    val xLength = dartPt.getX() - centerPt.getX()
    val yLength = dartPt.getY() - centerPt.getY()
    val hypotenuse = Math.sqrt(xLength * xLength + yLength * yLength)

    return if (xLength == 0.0)
    {
        (if (yLength > 0) 180 else 0).toDouble()
    }
    else if (yLength == 0.0)
    {
        (if (xLength > 0) 90 else 270).toDouble()
    }
    else
    {
        //We're not on an axis
        val xIsPositive = xLength > 0
        val yIsPositive = yLength > 0

        val quadrant = getQuadrant(xIsPositive, yIsPositive)
        val angleToAdd = quadrant!!.minimumAngle

        val lengthForCalculation = if (quadrant.sinForX) Math.abs(yLength) else Math.abs(xLength)

        var arcCosValue = Math.acos(lengthForCalculation / hypotenuse)
        arcCosValue = Math.abs(Math.toDegrees(arcCosValue))

        angleToAdd + arcCosValue
    }
}

/**
 * For the given angle, return the Quadrant. Returns null if there is none (because we're on an axis).
 */
private fun getQuadrantForAngle(angle: Double): Quadrant?
{
    return QUADRANTS.find { it.minimumAngle < angle && angle < it.maximumAngle }
}

private fun getQuadrant(xIsPositive: Boolean, yIsPositive: Boolean): Quadrant?
{
    return QUADRANTS.find{ it.xIsPositive == xIsPositive && it.yIsPositive == yIsPositive }
}

/**
 * For a group of points, calculate the average point
 */
fun getAverage(points: List<Point>): Point
{
    val xAvg = points.map{ it.x }.average()
    val yAvg = points.map{ it.y }.average()

    val ret = Point()
    ret.setLocation(xAvg, yAvg)
    return ret
}

fun generateRandomAngle(): Double
{
    val rand = Random()
    return rand.nextDouble() * 360
}
