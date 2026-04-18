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

    private val colours =
        listOf(
                "#ED1C24", // Red
                "#FF7F27", // Orange
                "#FFF200", // Yellow
                "#B5E61D", // Light green
                "#22B14C", // Dark green
                "#00A2E8", // Dark blue
                "#99D9EA", // Light blue
                "#B19CD7", // Light purple
                "#A349A4", // Purple
                "#FFAEC9", // Pink
            )
            .map(Color::decode)

    override fun getColour(segment: DartboardSegment): Color =
        if (segment.isMiss()) Color.BLACK
        else if (segment.score == 25) {
            if (segment.getMultiplier() == 1) Color.black else Color.white
        } else {
            val index = numberOrder.indexOf(segment.score) % colours.size

            val rawColour = colours[index]
            if (segment.getMultiplier() > 1) rawColour.darker() else rawColour
        }

    override fun withFont(font: Font) = copy(font = font)
}
