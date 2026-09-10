package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import java.awt.Color
import java.awt.Font

private val darkRed = Color.decode("#800020")
private val darkGreen = Color.decode("#39AD48")

// private val dartboardColours =
//    ColourWrapper(
//        darkRed,
//        Color.RED,
//        Color.RED,
//        Color.GREEN,
//        darkGreen,
//        darkGreen,
//        Color.RED,
//        darkGreen,
//        fontColor = Color.BLACK,
//        outerDartboardColour = Color.decode("#385025"),
//    )

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

    private val baubleColours = listOf(gold, Color.decode("#C0C0C0"), Color.red)
    private val lightColours = listOf(Color.RED, Color.WHITE, Color.BLUE, Color.YELLOW)

    override fun getFontColour(score: Int): Color {
        val index = score % baubleColours.size
        return baubleColours[index]
    }

    override fun getColour(segment: DartboardSegment): Color =
        //        if (segment.score == 25) {
        //            if (segment.type == SegmentType.DOUBLE) {
        //                Color.decode("#B59410")
        //            } else {
        //                Color.decode("#43B446")
        //            }
        //        } else if (segment.getMultiplier() > 1) {
        //            val offset = if (segment.getMultiplier() == 2) 0 else 1
        //            val index = (offset + numberOrder.indexOf(segment.score)) % lightColours.size
        //            lightColours[index]
        //        } else if (segment.type == SegmentType.MISS) {
        //            Color.decode("#056608")
        //        } else if (segment.type == SegmentType.OUTER_SINGLE) {
        //            Color.decode("#248D27")
        //        } else if (segment.type == SegmentType.INNER_SINGLE) {
        //            Color.decode("#33A036")
        //        } else {
        dartboardColours.getColour(segment)

    //        }

    override fun withFont(font: Font) = copy(font = font)
}
