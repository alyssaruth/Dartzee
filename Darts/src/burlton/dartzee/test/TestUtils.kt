package burlton.dartzee.test

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.screen.Dartboard
import java.awt.Color
import java.awt.Point
import javax.swing.SwingUtilities

val bullseye = DartboardSegment("25_$SEGMENT_TYPE_DOUBLE")
val outerBull = DartboardSegment("25_$SEGMENT_TYPE_OUTER_SINGLE")
val innerSingle = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val outerSingle = DartboardSegment("15_$SEGMENT_TYPE_OUTER_SINGLE")
val missTwenty = DartboardSegment("20_$SEGMENT_TYPE_MISS")
val missedBoard = DartboardSegment("15_$SEGMENT_TYPE_MISSED_BOARD")

val singleTwenty = DartboardSegment("20_$SEGMENT_TYPE_INNER_SINGLE")
val doubleTwenty = DartboardSegment("20_$SEGMENT_TYPE_DOUBLE")
val trebleTwenty = DartboardSegment("20_$SEGMENT_TYPE_TREBLE")
val singleNineteen = DartboardSegment("19_$SEGMENT_TYPE_OUTER_SINGLE")
val doubleNineteen = DartboardSegment("19_$SEGMENT_TYPE_DOUBLE")
val trebleNineteen = DartboardSegment("19_$SEGMENT_TYPE_TREBLE")
val singleEighteen = DartboardSegment("18_$SEGMENT_TYPE_OUTER_SINGLE")
val singleTen = DartboardSegment("10_$SEGMENT_TYPE_INNER_SINGLE")
val singleFive = DartboardSegment("5_$SEGMENT_TYPE_INNER_SINGLE")

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

/**
 * Flush the Event Dispatch Thread by invoking an empty fn onto the back of the queue and waiting for it
 */
fun flushEdt()
{
    val lambda = {}
    SwingUtilities.invokeAndWait(lambda)
}


fun Dartboard.getColor(pt: Point): Color = Color(dartboardImage!!.getRGB(pt.x, pt.y), true)