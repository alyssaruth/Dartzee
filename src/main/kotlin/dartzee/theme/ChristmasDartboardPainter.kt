package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import java.awt.Color
import java.awt.Font

private val darkRed = Color.decode("#800020")
private val darkGreen = Color.decode("#39AD48")

private val dartboardColours =
    ColourWrapper(
        darkRed,
        Color.RED,
        Color.RED,
        Color.GREEN,
        darkGreen,
        darkGreen,
        Color.RED,
        darkGreen,
        fontColor = Color.BLACK,
        outerDartboardColour = Color.decode("#385025"),
    )

data class ChristmasDartboardPainter(override val font: Font = getBaseFont()) : IDartboardPainter {
    override val outerDartboardColour: Color = Color.decode("#385025")
    override val missedBoardColour: Color = DartsColour.TRANSPARENT
    override val edgeColour = null

    override fun getFontColour(score: Int): Color {
        TODO("Not yet implemented")
    }

    override fun getColour(segment: DartboardSegment) = dartboardColours.getColour(segment)

    override fun withFont(font: Font) = copy(font = font)
}
