package burlton.dartzee.code.`object`

import burlton.core.code.obj.HashMapList
import java.awt.Point
import java.util.*

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

class DartboardSegment(val scoreAndType : String)
{
    var type : Int
    var score : Int

    //The Points this segment contains
    val points = ArrayList<Point>()
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

        var canBeYMax = true
        var canBeYMin = true
        var canBeXMax = true
        var canBeXMin = true

        val otherXPts = hmXCoordToPoints.getOrDefault(pt.x, mutableListOf())
        for (otherPt in otherXPts)
        {
            if (otherPt.getY() < pt.getY())
            {
                canBeYMin = false
            }

            if (otherPt.getY() > pt.getY())
            {
                canBeYMax = false
            }
        }

        val otherYPts = hmYCoordToPoints.getOrDefault(pt.y, mutableListOf<Point>())
        for (otherPt in otherYPts)
        {
            if (otherPt.getX() < pt.getX())
            {
                canBeXMin = false
            }

            if (otherPt.getX() > pt.getX())
            {
                canBeXMax = false
            }
        }

        return canBeYMax || canBeYMin || canBeXMax || canBeXMin
    }
}
