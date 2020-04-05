package dartzee.`object`

import dartzee.utils.DartsColour
import java.awt.Color

val DEFAULT_COLOUR_WRAPPER = ColourWrapper(DartsColour.DARTBOARD_BLACK, Color.RED, Color.RED, Color.WHITE, Color.GREEN, Color.GREEN, Color.RED, Color.GREEN)

val GREY_COLOUR_WRAPPER = makeMonochromeWrapper(Color.GRAY.brighter(), Color.LIGHT_GRAY)

fun makeMonochromeWrapper(dark: Color, light: Color): ColourWrapper
{
    return ColourWrapper(dark, light, light, light, dark, dark, dark, light)
}

class ColourWrapper constructor(private var evenSingleColour : Color, private var evenDoubleColour : Color,
                                private var evenTrebleColour : Color, private var oddSingleColour : Color,
                                private var oddDoubleColour : Color, private var oddTrebleColour : Color,
                                private var innerBullColour : Color, private var outerBullColour : Color,
                                var outerDartboardColour : Color = Color.black)
{
    var missedBoardColour: Color = DartsColour.TRANSPARENT
    var edgeColour: Color? = null

    constructor(singleColour: Color) : this(singleColour, singleColour, singleColour, singleColour, singleColour,
                                            singleColour, singleColour, singleColour, singleColour)

    /**
     * Helpers
     */
    fun getBullColour(multiplier: Int): Color
    {
        return when (multiplier)
        {
            1 -> outerBullColour
            else -> innerBullColour
        }

    }

    fun getColour(multiplier: Int, even: Boolean): Color
    {
        return when (multiplier)
        {
            1 -> getSingleColour(even)
            2 -> getDoubleColour(even)
            else -> getTrebleColour(even)
        }
    }

    private fun getSingleColour(even: Boolean): Color
    {
        return if (even) evenSingleColour else oddSingleColour
    }

    private fun getDoubleColour(even: Boolean): Color
    {
        return if (even) evenDoubleColour else oddDoubleColour
    }

    private fun getTrebleColour(even: Boolean): Color
    {
        return if (even) evenTrebleColour else oddTrebleColour
    }
}
