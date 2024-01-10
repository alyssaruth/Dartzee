package dartzee.`object`

import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getAverage
import dartzee.utils.getPotentialAimPoints
import dartzee.utils.translatePoint
import java.awt.Point

class ComputationalDartboard(private val width: Int, private val height: Int) : IDartboard {
    private val hmSegmentToCenterPoint = constructCenterPointMap()

    override fun computeRadius() = computeRadius(width, height)

    override fun computeCenter() = Point(width / 2, height / 2)

    fun getDeliberateMissPoint(): ComputedPoint {
        val rawPoint =
            translatePoint(
                computeCenter(),
                computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO,
                180.0
            )
        return toComputedPoint(rawPoint)
    }

    fun getPointToAimAt(segment: DartboardSegment) = hmSegmentToCenterPoint.getValue(segment)

    fun getPotentialAimPoints() = getPotentialAimPoints(computeCenter(), computeRadius())

    private fun constructCenterPointMap() =
        getAllNonMissSegments().associateWith { segment ->
            val pt = getAverage(getPointsForSegment(segment))
            toComputedPoint(pt)
        }
}
