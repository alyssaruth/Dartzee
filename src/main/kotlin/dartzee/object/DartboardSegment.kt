package dartzee.`object`

import dartzee.core.obj.HashMapList
import java.awt.Point

const val SEGMENT_TYPE_DOUBLE = 1
const val SEGMENT_TYPE_TREBLE = 2
const val SEGMENT_TYPE_OUTER_SINGLE = 3
const val SEGMENT_TYPE_INNER_SINGLE = 4
const val SEGMENT_TYPE_MISS = 5
const val SEGMENT_TYPE_MISSED_BOARD = 6

const val MISS_FUDGE_FACTOR = 1805

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
 * Hard-coded values based on counting the points in a 500x500 rendered dartboard.
 */
private fun getRoughScoringArea(): Double = 96173.0 + MISS_FUDGE_FACTOR
private fun getRoughSize(type: Int, score: Int): Int
{
    if (score == 25) return if (type == SEGMENT_TYPE_DOUBLE) 137 else 716
    return when (type)
    {
        SEGMENT_TYPE_DOUBLE -> 441 //8820
        SEGMENT_TYPE_TREBLE -> 275 //5500
        SEGMENT_TYPE_OUTER_SINGLE -> 2464 //49280
        SEGMENT_TYPE_INNER_SINGLE -> 1586 //31720
        SEGMENT_TYPE_MISS -> MISS_FUDGE_FACTOR
        else -> 3321
    }
}

/**
 * Data class so that equivalent segments are treated as equal (e.g. DartzeeRuleCalculationResult externalisation)
 */
data class DartboardSegment(val scoreAndType : String)
{
    val type : Int
    val score : Int

    //The Points this segment contains
    val points = mutableListOf<Point>()

    //For tracking edge points
    private val hmXCoordToPoints = HashMapList<Int, Point>()
    private val hmYCoordToPoints = HashMapList<Int, Point>()
    private val edgePoints = mutableListOf<Point>()

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

        if (edgePoints.isEmpty())
        {
            cacheEdgePoints()
        }

        return edgePoints.contains(pt)
    }

    private fun cacheEdgePoints()
    {
        val yMins: List<Point> = hmXCoordToPoints.values.map { points -> points.minBy { it.y }!! }
        val yMaxes: List<Point> = hmXCoordToPoints.values.map { points -> points.maxBy { it.y }!! }
        val xMins: List<Point> = hmYCoordToPoints.values.map { points -> points.minBy { it.x }!! }
        val xMaxes: List<Point> = hmYCoordToPoints.values.map { points -> points.maxBy { it.x }!! }
        edgePoints.addAll(yMins + yMaxes + xMins + xMaxes)
    }

//    private fun calculateIsEdgePoint(pt: Point): Boolean
//    {
//        val otherXPts = hmXCoordToPoints.getOrDefault(pt.x, mutableListOf())
//        val otherYPts = hmYCoordToPoints.getOrDefault(pt.y, mutableListOf())
//
//        val yMin = otherXPts.map { it.y }.min() ?: return true
//        val yMax = otherXPts.map { it.y }.max() ?: return true
//        val xMin = otherYPts.map { it.x }.min() ?: return true
//        val xMax = otherYPts.map { it.x }.max() ?: return true
//
//        return pt.x == xMax || pt.x == xMin || pt.y == yMax || pt.y == yMin
//    }

    fun getRoughProbability(): Double {
        return getRoughSize(type, score).toDouble() / getRoughScoringArea()
    }
}
