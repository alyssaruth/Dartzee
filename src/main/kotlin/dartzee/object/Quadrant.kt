package dartzee.`object`

/**
 * Represents a quadrant on a planar graph. NOTE: The sign of y is different because in Java, (0,0)
 * is in the top left. This means that moving "up" actually DECREASES y. "yIsPositive" means y is
 * positive IN JAVA.
 */
data class Quadrant(
    val minimumAngle: Int,
    val maximumAngle: Int,
    val xIsPositive: Boolean,
    val yIsPositive: Boolean,
) {
    val sinForX = xIsPositive xor yIsPositive
}
