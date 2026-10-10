package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import dartzee.utils.hmScoreToOrdinal
import java.awt.Color
import java.awt.Font

private val darkRed = Color.decode("#800020")
private val darkGreen = Color.decode("#39AD48")
// private val medRed = Color.red.darker()

private val dartboardColours =
    ColourWrapper(
        darkGreen,
        darkRed,
        darkRed,
        darkRed,
        darkGreen,
        darkGreen,
        darkRed,
        darkGreen,
        fontColor = Color.BLACK,
        outerDartboardColour = Color.decode("#2F431F"),
    )

data class ChristmasDartboardPainter(
    override val font: Font = getBaseFont(),
    private val litNumbers: List<Int> = emptyList(),
) : IDartboardPainter {
    override val outerDartboardColour: Color = dartboardColours.outerDartboardColour
    override val missedBoardColour: Color = DartsColour.TRANSPARENT
    override val edgeColour: Color = Color.BLACK

    override fun getFontColour(score: Int): Color {
        val offColour = if (hmScoreToOrdinal.getValue(score)) darkRed else darkGreen
        return if (litNumbers.contains(score)) getFlashColour(offColour) else offColour
    }

    override fun getColour(segment: DartboardSegment) = dartboardColours.getColour(segment)

    fun getFlashColour(segment: DartboardSegment): Color =
        getFlashColour(dartboardColours.getColour(segment))

    private fun getFlashColour(offColour: Color): Color =
        when (offColour) {
            darkRed -> Color.RED
            darkGreen -> Color.decode("#65FF6B")
            else -> Color.WHITE
        }

    override fun withFont(font: Font) = copy(font = font)
}
