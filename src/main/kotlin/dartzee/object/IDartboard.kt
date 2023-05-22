package dartzee.`object`

import dartzee.utils.AimPoint
import dartzee.utils.computePointsForSegment
import dartzee.utils.factorySegmentForPoint
import dartzee.utils.getAngleForPoint
import java.awt.Point

interface IDartboard
{
    fun computeRadius(): Double
    fun computeCenter(): Point

    fun computeRadius(width: Int, height: Int) = 0.7 * minOf(width, height) / 2.0
    fun getPointsForSegment(segment: DartboardSegment) = computePointsForSegment(segment, computeCenter(), computeRadius())

    fun isDouble(pt: Point) = getSegmentForPoint(pt).isDoubleExcludingBull()
    fun getSegmentForPoint(pt: Point) = factorySegmentForPoint(pt, computeCenter(), computeRadius())

    fun translateAimPoint(aimPoint: AimPoint) = AimPoint(computeCenter(), computeRadius(), aimPoint.angle, aimPoint.ratio).point

    fun toComputedPoint(pt: Point): ComputedPoint {
        val segment = getSegmentForPoint(pt)
        val distance = pt.distance(computeCenter()) / computeRadius()
        val angle = getAngleForPoint(pt, computeCenter())

        return ComputedPoint(pt, segment, distance, angle)
    }
}