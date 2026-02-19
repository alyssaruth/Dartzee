package dartzee.ai

import dartzee.game.FinishType
import dartzee.`object`.ComputedPoint
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import java.awt.Point

/**
 * Given the single/double/treble required, calculate the physical coordinates of the optimal place
 * to aim
 */
fun getPointForScore(drt: AimDart): Point {
    val score = drt.score
    val segmentType = drt.getSegmentType()
    return getPointForScore(score, segmentType)
}

fun getPointForScore(score: Int, type: SegmentType): Point =
    AI_DARTBOARD.getPointToAimAt(DartboardSegment(type, score)).pt

fun getComputedPointForScore(score: Int, type: SegmentType): ComputedPoint =
    AI_DARTBOARD.getPointToAimAt(DartboardSegment(type, score))

/** Get the application-wide default thing to aim for, which applies to any score of 60 or less */
fun getX01AimDart(score: Int, finishType: FinishType, dartsRemaining: Int) =
    when (finishType) {
        FinishType.Doubles -> getX01AimDartDoublesMode(score, dartsRemaining)
        FinishType.Any -> getX01AimDartRelaxedMode(score)
    }

private fun getX01AimDartDoublesMode(score: Int, dartsRemaining: Int): AimDart {
    if (score == 50 && dartsRemaining <= 2) {
        return AimDart(25, 2)
    }

    // Aim for the single that puts you on double top
    if (score > 40) {
        val single = score - 40
        return AimDart(single, 1)
    }

    // Aim for the double
    if (score % 2 == 0) {
        return AimDart(score / 2, 2)
    }

    // On an odd number, less than 40. Aim to put ourselves on the highest possible power of 2.
    val scoreToLeaveRemaining = getHighestPowerOfTwoLessThan(score)
    val singleToAimFor = score - scoreToLeaveRemaining
    return AimDart(singleToAimFor, 1)
}

private fun getX01AimDartRelaxedMode(score: Int): AimDart {
    // Finish on single if possible
    if (score <= 20) {
        return AimDart(score, 1)
    }

    // Go for treble finishes if possible
    if (score % 3 == 0) {
        return AimDart(score / 3, 3)
    }

    // Aim to put ourselves on single 20
    if (score <= 40) {
        return AimDart(score - 20, 1)
    }

    // Just go for single 20
    return AimDart(20, 1)
}

private fun getHighestPowerOfTwoLessThan(score: Int): Int {
    var i = 2
    while (i < score) {
        i *= 2
    }

    return i / 2
}
