package dartzee.dartzee

import dartzee.`object`.SegmentType
import dartzee.screen.Dartboard
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.AimPoint
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.translatePoint
import java.awt.Point

class DartzeeAimCalculator
{
    private val miniDartboard = Dartboard(300, 300)

    init
    {
        miniDartboard.paintDartboard()
    }

    fun getPointToAimFor(dartboard: Dartboard, segmentStatus: SegmentStatus, aggressive: Boolean): Point
    {
        val scoringSegments = segmentStatus.scoringSegments.map { miniDartboard.getSegment(it.score, it.type)!! }
        val validSegments = segmentStatus.validSegments.map { miniDartboard.getSegment(it.score, it.type)!! }

        println(scoringSegments.size)
        println(validSegments.size)

        val segmentsToConsiderAimingFor = if (aggressive) scoringSegments else validSegments

        //Shortcut straight to the bullseye if all outer singles, inner singles, trebles and bull are valid
        val innerSegments = getAllPossibleSegments().filter { !it.isMiss() && it.type != SegmentType.DOUBLE }
        if (segmentsToConsiderAimingFor.containsAll(innerSegments))
        {
            return dartboard.centerPoint
        }

        val aimingPointSet = segmentsToConsiderAimingFor.flatMap { miniDartboard.getPointsForSegment(it.score, it.type) }.toSet()
        val validPointSet = validSegments.flatMap { miniDartboard.getPointsForSegment(it.score, it.type) }.toSet()

        val potentialPointsToAimFor = miniDartboard.getPotentialAimPoints().filter { aimingPointSet.contains(it.point) }
        val circleSizeToPoints = potentialPointsToAimFor.groupBy { it.getMaxCircleSize(validPointSet) }
        val contendingPoints = circleSizeToPoints.entries.maxBy { it.key }?.value

        val bestPoint = contendingPoints?.maxBy { miniDartboard.getSegmentForPoint(it.point).getTotal() }!!
        return dartboard.translateAimPoint(bestPoint)
    }
    private fun AimPoint.getMaxCircleSize(validPoints: Set<Point>): Double
    {
        val pointsInCircle = mutableSetOf(this.point)
        var radius = 1.0
        while (validPoints.containsAll(pointsInCircle))
        {
            for (i in 0..359)
            {
                pointsInCircle.add(translatePoint(this.point, radius, i.toDouble()))
            }

            radius += 1
        }

        return radius
    }
}