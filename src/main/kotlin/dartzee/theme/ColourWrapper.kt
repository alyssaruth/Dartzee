package dartzee.theme

import dartzee.`object`.DartboardSegment
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache
import dartzee.utils.hmScoreToOrdinal
import java.awt.Color
import java.awt.Font

val DEFAULT_COLOUR_WRAPPER =
    ColourWrapper(
        DartsColour.DARTBOARD_BLACK,
        Color.RED,
        Color.RED,
        Color.WHITE,
        Color.GREEN,
        Color.GREEN,
        Color.RED,
        Color.GREEN,
    )

val WIREFRAME_COLOUR_WRAPPER = ColourWrapper(DartsColour.TRANSPARENT).copy(edgeColour = Color.BLACK)

val GREY_COLOUR_WRAPPER =
    makeMonochromeWrapper(Color.GRAY.brighter(), Color.LIGHT_GRAY)
        .copy(outerDartboardColour = Color.GRAY)

fun makeMonochromeWrapper(dark: Color, light: Color) =
    ColourWrapper(dark, light, light, light, dark, dark, dark, light)

data class ColourWrapper(
    private val evenSingleColour: Color,
    private val evenDoubleColour: Color,
    private val evenTrebleColour: Color,
    private val oddSingleColour: Color,
    private val oddDoubleColour: Color,
    private val oddTrebleColour: Color,
    private val innerBullColour: Color,
    private val outerBullColour: Color,
    override val outerDartboardColour: Color = Color.black,
    override val missedBoardColour: Color = DartsColour.TRANSPARENT,
    override val edgeColour: Color? = null,
    override val fontColor: Color = Color.white,
    override val font: Font = ResourceCache.BASE_FONT,
) : IDartboardPainter {

    constructor(
        singleColour: Color
    ) : this(
        singleColour,
        singleColour,
        singleColour,
        singleColour,
        singleColour,
        singleColour,
        singleColour,
        singleColour,
        singleColour,
    )

    override fun withFont(font: Font) = copy(font = font)

    /** Helpers */
    private fun getBullColour(multiplier: Int): Color {
        return when (multiplier) {
            1 -> outerBullColour
            else -> innerBullColour
        }
    }

    override fun getColour(segment: DartboardSegment) =
        getColour(segment.getMultiplier(), segment.score)

    private fun getColour(multiplier: Int, score: Int): Color {
        if (score == 25) {
            return getBullColour(multiplier)
        }

        val even = hmScoreToOrdinal[Integer.valueOf(score)] ?: false
        return when (multiplier) {
            1 -> getSingleColour(even)
            2 -> getDoubleColour(even)
            3 -> getTrebleColour(even)
            else -> outerDartboardColour
        }
    }

    private fun getSingleColour(even: Boolean) = if (even) evenSingleColour else oddSingleColour

    private fun getDoubleColour(even: Boolean) = if (even) evenDoubleColour else oddDoubleColour

    private fun getTrebleColour(even: Boolean) = if (even) evenTrebleColour else oddTrebleColour
}
