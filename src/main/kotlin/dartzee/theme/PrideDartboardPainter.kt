package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import dartzee.utils.numberOrder
import java.awt.Color
import java.awt.Font

data class PrideDartboardPainter(override val font: Font = getBaseFont()) : IDartboardPainter {
    override val outerDartboardColour: Color = Color.BLACK
    override val missedBoardColour = DartsColour.TRANSPARENT
    override val edgeColour = null
    override val fontColor: Color = Color.WHITE

    override fun getColour(segment: DartboardSegment): Color =
        if (segment.isMiss()) Color.BLACK
        else if (segment.score == 25) {
            if (segment.getMultiplier() == 1) Color.black else Color.white
        } else {
            val index = numberOrder.indexOf(segment.score)
            val rawColour = PrideColors.forIndex(index)
            if (segment.getMultiplier() > 1) rawColour.darker() else rawColour
        }

    override fun withFont(font: Font) = copy(font = font)
}
