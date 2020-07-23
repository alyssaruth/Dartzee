import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.screen.Dartboard
import dartzee.utils.getAverage
import java.awt.Point

/**
 * Given the single/double/treble required, calculate the physical coordinates of the optimal place to aim
 */
fun getPointForScore(drt: Dart, dartboard: Dartboard): Point
{
    val score = drt.score
    val segmentType = drt.getSegmentTypeToAimAt()
    return getPointForScore(score, dartboard, segmentType)
}

fun getPointForScore(score: Int, dartboard: Dartboard, type: SegmentType): Point
{
    val points = dartboard.getPointsForSegment(score, type)
    val avgPoint = getAverage(points)

    //Need to rationalise here as we may have adjusted outside of the bounds
    //Shouldn't need this anymore, but can't hurt to leave it here anyway!
    dartboard.rationalisePoint(avgPoint)

    return avgPoint
}