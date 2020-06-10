package dartzee.dartzee

import dartzee.`object`.DartboardSegment
import dartzee.screen.Dartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.getAverage
import dartzee.utils.translatePoint
import java.awt.Point

class DartzeeAimCalculator
{
    private val dartboard = Dartboard(150, 150)

    init
    {
        dartboard.paintDartboard()
    }

    fun getSegmentToAimFor(segmentStatus: SegmentStatus): DartboardSegment?
    {
        val validSegments = segmentStatus.validSegments.map { dartboard.getSegment(it.score, it.type)!! }
        val validPointSet = validSegments.flatMap { dartboard.getPointsForSegment(it.score, it.type) }.toSet()

        val radiusToSegments = validSegments.groupBy { it.getMaxCircleSize(validPointSet) }
        println(radiusToSegments)
        val contendingSegments = radiusToSegments.entries.maxBy { it.key }?.value

        return contendingSegments?.maxBy { it.getTotal() }
    }
    private fun DartboardSegment.getMaxCircleSize(validPoints: Set<Point>): Double
    {
        val centerPoint = getAverage(points)

        val pointsInCircle = mutableSetOf(centerPoint)
        var radius = 1.0
        while (validPoints.containsAll(pointsInCircle))
        {
            for (i in 0..359)
            {
                pointsInCircle.add(translatePoint(centerPoint, radius, i.toDouble()))
            }

            radius++
        }

        return radius
    }
}