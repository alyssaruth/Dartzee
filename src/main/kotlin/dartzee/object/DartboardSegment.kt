package dartzee.`object`

import dartzee.core.obj.HashMapList
import java.awt.Point

const val SEGMENT_TYPE_DOUBLE = 1
const val SEGMENT_TYPE_TREBLE = 2
const val SEGMENT_TYPE_OUTER_SINGLE = 3
const val SEGMENT_TYPE_INNER_SINGLE = 4
const val SEGMENT_TYPE_MISS = 5
const val SEGMENT_TYPE_MISSED_BOARD = 6

fun getGolfScoreForSegment(type: Int): Int
{
    return when (type)
    {
        SEGMENT_TYPE_DOUBLE -> 1
        SEGMENT_TYPE_TREBLE -> 2
        SEGMENT_TYPE_INNER_SINGLE -> 3
        SEGMENT_TYPE_OUTER_SINGLE -> 4
        else -> 5
    }
}

fun getMultiplier(type: Int): Int
{
    return when(type)
    {
        SEGMENT_TYPE_DOUBLE -> 2
        SEGMENT_TYPE_TREBLE -> 3
        SEGMENT_TYPE_MISS, SEGMENT_TYPE_MISSED_BOARD -> 0
        else -> 1
    }
}

/**
 * Data class so that equivalent segments are treated as equal (e.g. DartzeeRuleCalculationResult externalisation)
 */
data class DartboardSegment(val scoreAndType : String)
{
    var type : Int
    var score : Int

    //The Points this segment contains
    val points = mutableListOf<Point>()

    //For tracking edge points
    private val hmXCoordToPoints = HashMapList<Int, Point>()
    private val hmYCoordToPoints = HashMapList<Int, Point>()

    init
    {
        val toks = scoreAndType.split("_")

        this.score = toks[0].toInt()
        this.type = toks[1].toInt()
    }


    /**
     * Helpers
     */
    fun isMiss() = type == SEGMENT_TYPE_MISS || type == SEGMENT_TYPE_MISSED_BOARD
    fun isDoubleExcludingBull() = type == SEGMENT_TYPE_DOUBLE && score != 25
    fun getMultiplier() = getMultiplier(type)
    fun getTotal(): Int = score * getMultiplier()

    fun addPoint(pt: Point)
    {
        points.add(pt)

        hmXCoordToPoints.putInList(pt.x, pt)
        hmYCoordToPoints.putInList(pt.y, pt)
    }

    override fun toString() = "$score ($type)"

    fun isEdgePoint(pt: Point?): Boolean
    {
        pt ?: return false

        val otherXPts = hmXCoordToPoints.getOrDefault(pt.x, mutableListOf())
        val otherYPts = hmYCoordToPoints.getOrDefault(pt.y, mutableListOf())

        val yMin = otherXPts.map { it.y }.min() ?: return true
        val yMax = otherXPts.map { it.y }.max() ?: return true
        val xMin = otherYPts.map { it.x }.min() ?: return true
        val xMax = otherYPts.map { it.x }.max() ?: return true

        return pt.x == xMax || pt.x == xMin || pt.y == yMax || pt.y == yMin
    }
}
