package dartzee.dartzee

import dartzee.ai.DELIBERATE_MISS
import dartzee.core.util.maxOrZero
import dartzee.`object`.ComputationalDartboard
import dartzee.`object`.SegmentType
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.AimPoint
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.translatePoint
import java.awt.Point

class DartzeeAimCalculator {
    private val miniDartboard = ComputationalDartboard(350, 350)

    fun getPointToAimFor(
        dartboard: ComputationalDartboard,
        segmentStatuses: SegmentStatuses,
        aggressive: Boolean
    ): Point {
        val scoringSegments = segmentStatuses.scoringSegments.filter { !it.isMiss() }
        val validSegments = segmentStatuses.validSegments.filter { !it.isMiss() }

        val segmentsToConsiderAimingFor =
            if (aggressive && scoringSegments.isNotEmpty()) scoringSegments else validSegments
        if (segmentsToConsiderAimingFor.isEmpty()) {
            return DELIBERATE_MISS
        }

        // Shortcut straight to the bullseye if all outer singles, inner singles, trebles and bull
        // are valid
        val innerSegments =
            getAllNonMissSegments().filter { it.type != SegmentType.DOUBLE || it.score == 25 }
        if (segmentsToConsiderAimingFor.containsAll(innerSegments)) {
            return dartboard.computeCenter()
        }

        val aimingPointSet =
            segmentsToConsiderAimingFor.flatMap(miniDartboard::getPointsForSegment).toSet()
        val validPointSet = validSegments.flatMap(miniDartboard::getPointsForSegment).toSet()

        val potentialPointsToAimFor =
            miniDartboard.getPotentialAimPoints().filter { aimingPointSet.contains(it.point) }
        val contendingPoints = getMaxCirclePoints(validPointSet, potentialPointsToAimFor)

        val bestScore =
            contendingPoints
                .map { miniDartboard.getSegmentForPoint(it.point).getTotal() }
                .maxOrZero()
        val contendingHighScorePoints =
            contendingPoints.filter {
                miniDartboard.getSegmentForPoint(it.point).getTotal() == bestScore
            }

        // Prefer even angles to odd ones
        val bestPoint = contendingHighScorePoints.minByOrNull { it.angle % 2 }!!
        return dartboard.translateAimPoint(bestPoint)
    }

    /**
     * Optimisation - rather than do a groupBy for all potential points, iterating the circle size
     * up, instead loop over them and keep track of the max circle size so far. Iterate down from
     * this -> 0 to rule out most points more quickly.
     */
    private fun getMaxCirclePoints(
        validPointSet: Set<Point>,
        potentialPointsToAimFor: List<AimPoint>
    ): List<AimPoint> {
        var currentMax = 1
        val maxPoints = mutableListOf<AimPoint>()
        potentialPointsToAimFor.forEach { candidatePt ->
            val myMax = candidatePt.getMaxCircleSize(validPointSet, currentMax)
            if (myMax > currentMax) {
                currentMax = myMax
                maxPoints.clear()
                maxPoints.add(candidatePt)
            } else if (myMax == currentMax) {
                maxPoints.add(candidatePt)
            }
        }

        return maxPoints
    }

    private fun AimPoint.getMaxCircleSize(validPoints: Set<Point>, currentMax: Int): Int {
        val range = (currentMax downTo 1)
        val bigEnough = range.all { validPoints.containsAll(makeCircle(point, it)) }
        if (!bigEnough) {
            return 1
        }

        var radius = currentMax + 1
        var pointsInCircle = makeCircle(this.point, radius)
        while (validPoints.containsAll(pointsInCircle)) {
            radius += 1
            pointsInCircle = makeCircle(this.point, radius)
        }

        return radius - 1
    }

    private fun makeCircle(centerPt: Point, radius: Int) =
        (0..359).map { translatePoint(centerPt, radius.toDouble(), it.toDouble()) }.toSet()
}
