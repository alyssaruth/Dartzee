package dartzee.ai

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.Dartboard
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getAverage
import java.awt.Point

/**
 * Arbitrary-seeming width and height values taken from the size the Dartboard used to be fixed at during gameplay
 */
val AI_DARTBOARD = AiDartboard().also {
    it.paintDartboard()
    it.cacheCenterPoints()
}

class AiDartboard: Dartboard(520, 555)
{
    private val segmentToCenterPoint = mutableMapOf<DartboardSegment, Point>()

    fun cacheCenterPoints()
    {
        getAllNonMissSegments().forEach {
            segmentToCenterPoint[it] = getAverage(getPointsForSegment(it))
        }
    }

    fun getAimPoint(score: Int, segmentType: SegmentType): Point {
        if (segmentType == SegmentType.MISS) {
            return getDeliberateMissPoint()
        }

        return segmentToCenterPoint.getValue(DartboardSegment(segmentType, score))
    }
}