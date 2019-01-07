@file:JvmName("DartboardSegment")
package burlton.dartzee.code.`object`

import burlton.core.code.obj.HashMapList
import burlton.core.code.util.StringUtil
import java.awt.Point
import java.util.*

const val TYPE_DOUBLE = 1
const val TYPE_TREBLE = 2
const val TYPE_OUTER_SINGLE = 3
const val TYPE_INNER_SINGLE = 4
const val TYPE_MISS = 5
const val TYPE_MISSED_BOARD = 6

fun getGolfScoreForSegment(type: Int): Int
{
    return when (type)
    {
        TYPE_DOUBLE -> 1
        TYPE_TREBLE -> 2
        TYPE_INNER_SINGLE -> 3
        TYPE_OUTER_SINGLE -> 4
        else -> 5
    }
}

fun getMultiplier(type: Int): Int
{
    return when(type)
    {
        TYPE_DOUBLE -> 2
        TYPE_TREBLE -> 3
        TYPE_MISS, TYPE_MISSED_BOARD -> 0
        else -> 1
    }
}

class DartboardSegmentKt(scoreAndType : String)
{
    var type : Int
    var score : Int

    //The Points this segment contains
    val points = ArrayList<Point>()
    private val hmXCoordToPoints = HashMapList<Int, Point>()
    private val hmYCoordToPoints = HashMapList<Int, Point>()

    init
    {
        val toks = StringUtil.getListFromDelims(scoreAndType, "_")

        this.score = toks[0].toInt()
        this.type = toks[1].toInt()
    }


    /**
     * Helpers
     */
    fun isMiss() : Boolean
    {
        return type == TYPE_MISS || type == TYPE_MISSED_BOARD
    }
    fun isDoubleExcludingBull() : Boolean
    {
        return type == TYPE_DOUBLE && score != 25
    }
    fun getMultiplier() : Int
    {
        return getMultiplier(type)
    }

    fun addPoint(pt: Point)
    {
        points.add(pt)

        hmXCoordToPoints.putInList(pt.x, pt)
        hmYCoordToPoints.putInList(pt.y, pt)
    }

    override fun toString(): String
    {
        return score.toString() + " (" + type + ")"
    }

    fun isEdgePoint(pt: Point): Boolean
    {
        var canBeYMax = true
        var canBeYMin = true
        var canBeXMax = true
        var canBeXMin = true

        val otherXPts = hmXCoordToPoints.getOrDefault(pt.x, mutableListOf<Point>())
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
