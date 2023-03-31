package dartzee.`object`

import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.getPotentialAimPoints
import dartzee.utils.translatePoint
import java.awt.Point

class ComputationalDartboard(private val width: Int, private val height: Int): IDartboard
{
    override fun computeRadius() = computeRadius(width, height)
    override fun computeCenter() = Point(width / 2, height / 2)

    fun getDeliberateMissPoint() = translatePoint(computeCenter(), computeRadius() * UPPER_BOUND_OUTSIDE_BOARD_RATIO, 180.0)

    fun getPotentialAimPoints() = getPotentialAimPoints(computeCenter(), 2 * computeRadius())
}