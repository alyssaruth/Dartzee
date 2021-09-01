package dartzee.helper

import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType

fun outerSingle(score: Int) = DartboardSegment(SegmentType.OUTER_SINGLE, score)
fun double(score: Int) = DartboardSegment(SegmentType.DOUBLE, score)
fun treble(score: Int) = DartboardSegment(SegmentType.TREBLE, score)