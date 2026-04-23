package dartzee.theme

import dartzee.`object`.DartboardSegment
import java.awt.Color
import java.awt.Font

interface IDartboardPainter {
    val outerDartboardColour: Color
    val missedBoardColour: Color
    val edgeColour: Color?
    val fontColor: Color
    val font: Font

    fun getColour(segment: DartboardSegment): Color

    fun withFont(font: Font): IDartboardPainter
}
