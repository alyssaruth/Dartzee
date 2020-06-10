package dartzee.`object`

import dartzee.core.obj.HashMapList
import dartzee.core.util.getAttributeInt
import dartzee.core.util.setAttributeAny
import org.w3c.dom.Element
import java.awt.Point

const val MISS_FUDGE_FACTOR = 1805

/**
 * Data class so that equivalent segments are treated as equal (e.g. DartzeeRuleCalculationResult externalisation)
 */
data class DartboardSegment(val type: SegmentType, val score: Int)
{
    //The Points this segment contains
    val points = mutableListOf<Point>()
    val edgePoints = mutableSetOf<Point>()

    //For tracking edge points
    private val hmXCoordToPoints = HashMapList<Int, Point>()
    private val hmYCoordToPoints = HashMapList<Int, Point>()

    /**
     * Helpers
     */
    fun isMiss() = type == SegmentType.MISS || type == SegmentType.MISSED_BOARD
    fun isDoubleExcludingBull() = type == SegmentType.DOUBLE && score != 25
    fun getMultiplier() = type.getMultiplier()
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

    fun getRoughProbability(): Double {
        return type.getRoughSize(score).toDouble() / getRoughScoringArea()
    }

    fun writeXml(root: Element, name: String)
    {
        val element = root.ownerDocument.createElement(name)
        element.setAttributeAny("Score", score)
        element.setAttributeAny("Type", type)

        root.appendChild(element)
    }

    companion object
    {
        fun readList(root: Element, itemName: String): List<DartboardSegment>
        {
            val list = root.getElementsByTagName(itemName)

            return (0 until list.length).map {
                val node = list.item(it) as Element
                val score = node.getAttributeInt("Score")
                val type = SegmentType.valueOf(node.getAttribute("Type"))
                DartboardSegment(type, score)
            }
        }
    }
}

