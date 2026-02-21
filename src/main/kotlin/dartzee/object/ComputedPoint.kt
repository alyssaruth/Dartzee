package dartzee.`object`

import java.awt.Point

data class ComputedPoint(
    val pt: Point,
    val segment: DartboardSegment,
    val radius: Double,
    val angle: Double,
)
