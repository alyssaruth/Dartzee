package burlton.dartzee.test

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.screen.Dartboard

val bullseye = DartboardSegment("25_$SEGMENT_TYPE_DOUBLE")
val outerBull = DartboardSegment("25_$SEGMENT_TYPE_OUTER_SINGLE")
val innerSingle = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val outerSingle = DartboardSegment("15_$SEGMENT_TYPE_OUTER_SINGLE")
val miss = DartboardSegment("20_$SEGMENT_TYPE_MISS")
val missedBoard = DartboardSegment("15_$SEGMENT_TYPE_MISSED_BOARD")

val singleTwenty = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val doubleTwenty = DartboardSegment("20_$SEGMENT_TYPE_DOUBLE")
val trebleTwenty = DartboardSegment("20_$SEGMENT_TYPE_TREBLE")
val singleNineteen = DartboardSegment("19_$SEGMENT_TYPE_OUTER_SINGLE")
val doubleNineteen = DartboardSegment("19_$SEGMENT_TYPE_DOUBLE")
val trebleNineteen = DartboardSegment("19_$SEGMENT_TYPE_TREBLE")

private var dartboard: Dartboard? = null

fun borrowTestDartboard(): Dartboard
{
    if (dartboard == null)
    {
        dartboard = Dartboard(100, 100)
        dartboard!!.paintDartboard()
    }

    return dartboard!!
}