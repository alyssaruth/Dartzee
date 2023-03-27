package dartzee.`object`

import dartzee.utils.computePointsForSegment
import java.awt.Point

interface IDartboard
{
    fun computeRadius(): Double
    fun computeCenter(): Point

    fun getPointsForSegment(segment: DartboardSegment) = computePointsForSegment(segment, computeCenter(), computeRadius())
}