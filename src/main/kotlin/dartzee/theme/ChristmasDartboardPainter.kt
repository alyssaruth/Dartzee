package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import java.awt.Color
import java.awt.Font

private val darkRed = Color.decode("#800020")
private val darkGreen = Color.decode("#39AD48")
// private val medRed = Color.red.darker()

private val dartboardColours =
    ColourWrapper(
        Color.GREEN,
        darkRed,
        darkRed,
        Color.RED,
        darkGreen,
        darkGreen,
        darkRed,
        darkGreen,
        fontColor = Color.BLACK,
        outerDartboardColour = Color.decode("#385025"),
    )

data class ChristmasDartboardPainter(override val font: Font = getBaseFont()) : IDartboardPainter {
    override val outerDartboardColour: Color = Color.decode("#385025")
    override val missedBoardColour: Color = DartsColour.TRANSPARENT
    override val edgeColour = null

    private val baubleColours = listOf(gold, Color.decode("#C0C0C0"), Color.red)

    override fun getFontColour(score: Int): Color {
        val index = score % baubleColours.size
        return baubleColours[index]
    }

    override fun getColour(segment: DartboardSegment) = dartboardColours.getColour(segment)

    fun getFlashColour(segment: DartboardSegment): Color =
        when (dartboardColours.getColour(segment)) {
            darkRed -> Color.RED
            darkGreen -> Color.GREEN
            else -> Color.WHITE
        }

    override fun withFont(font: Font) = copy(font = font)
}
