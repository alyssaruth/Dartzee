
import dartzee.ai.AI_DARTBOARD
import dartzee.ai.AimDart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.utils.getAverage
import java.awt.Point

/**
 * Given the single/double/treble required, calculate the physical coordinates of the optimal place to aim
 */
fun getPointForScore(drt: AimDart): Point
{
    val score = drt.score
    val segmentType = drt.getSegmentType()
    return getPointForScore(score, segmentType)
}

fun getPointForScore(score: Int, type: SegmentType): Point =
    AI_DARTBOARD.getPointToAimAt(DartboardSegment(type, score))

/**
 * Get the application-wide default thing to aim for, which applies to any score of 60 or less
 */
fun getDefaultDartToAimAt(score: Int): AimDart
{
    //Aim for the single that puts you on double top
    if (score > 40)
    {
        val single = score - 40
        return AimDart(single, 1)
    }

    //Aim for the double
    if (score % 2 == 0)
    {
        return AimDart(score / 2, 2)
    }

    //On an odd number, less than 40. Aim to put ourselves on the highest possible power of 2.
    val scoreToLeaveRemaining = getHighestPowerOfTwoLessThan(score)
    val singleToAimFor = score - scoreToLeaveRemaining
    return AimDart(singleToAimFor, 1)
}

private fun getHighestPowerOfTwoLessThan(score: Int): Int
{
    var i = 2
    while (i < score)
    {
        i *= 2
    }

    return i / 2
}
