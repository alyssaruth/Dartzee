package dartzee.`object`

import dartzee.utils.UPPER_BOUND_OUTSIDE_BOARD_RATIO
import dartzee.utils.getPotentialAimPoints
import dartzee.utils.translatePoint
import java.awt.Point

class ComputationalDartboard(private val radius: Double): IDartboard
{
    constructor(width: Int, height: Int) : this(0.7 * minOf(width, height) / 2.0)

    override fun computeRadius() = radius
    override fun computeCenter() = Point(0, 0)

    fun getDeliberateMissPoint() = translatePoint(computeCenter(), radius * UPPER_BOUND_OUTSIDE_BOARD_RATIO, 180.0)

    fun getPotentialAimPoints() = getPotentialAimPoints(computeCenter(), 2 * computeRadius())
}