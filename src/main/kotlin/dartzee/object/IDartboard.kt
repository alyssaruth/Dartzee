package dartzee.`object`

import dartzee.utils.AimPoint
import dartzee.utils.computePointsForSegment
import dartzee.utils.factorySegmentForPoint
import java.awt.Point

interface IDartboard
{
    fun computeRadius(): Double
    fun computeCenter(): Point

    fun getPointsForSegment(segment: DartboardSegment) = computePointsForSegment(segment, computeCenter(), computeRadius())

    fun isDouble(pt: Point) = getSegmentForPoint(pt).isDoubleExcludingBull()
    fun getSegmentForPoint(pt: Point) = factorySegmentForPoint(pt, computeCenter(), computeRadius() * 2)

    fun translateAimPoint(aimPoint: AimPoint) = AimPoint(computeCenter(), computeRadius(), aimPoint.angle, aimPoint.ratio).point
}