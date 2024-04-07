package dartzee.utils

import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import java.awt.Color
import java.awt.Component
import java.lang.Float.max

object DartsColour {
    val TRANSPARENT = Color(0, 0, 0, 0)

    val DARTBOARD_RED: Color = Color.red
    val DARTBOARD_GREEN: Color = Color.green
    val DARTBOARD_BLACK: Color = Color(32, 42, 68)
    val DARTBOARD_WHITE: Color = Color.white

    val DARTBOARD_LIGHTEST_GREY: Color = Color.getHSBColor(0f, 0f, 0.9.toFloat())
    val DARTBOARD_LIGHTER_GREY: Color = Color.getHSBColor(0f, 0f, 0.75.toFloat())
    val DARTBOARD_LIGHT_GREY: Color = Color.getHSBColor(0f, 0f, 0.6.toFloat())

    val COLOUR_GOLD_TEXT: Color = Color.getHSBColor(5.toFloat() / 36, 1f, 0.8.toFloat())
    val COLOUR_SILVER_TEXT: Color = Color.GRAY.darker().darker()

    val COLOUR_BRONZE: Color = Color.getHSBColor(45.toFloat() / 360, 0.91.toFloat(), 0.49.toFloat())
    val COLOUR_BRONZE_TEXT: Color = COLOUR_BRONZE.darker().darker()

    val COLOUR_ACHIEVEMENT_ORANGE: Color = Color.getHSBColor(0.1f, 1f, 1f)

    val COLOUR_PASTEL_BLUE: Color =
        Color.getHSBColor(242.toFloat() / 360, 0.48.toFloat(), 0.8.toFloat())
    val PURPLE = Color(138, 43, 226)
    val ORANGE = Color(255, 140, 0)
    val DARK_GREEN = Color(1, 75, 32)
    val DARK_RED = Color(139, 0, 0)

    val FIRST_COLOURS = Pair(Color.YELLOW, COLOUR_GOLD_TEXT)
    val SECOND_COLOURS = Pair(Color.GRAY, COLOUR_SILVER_TEXT)
    val THIRD_COLOURS = Pair(COLOUR_BRONZE, COLOUR_BRONZE_TEXT)
    val FOURTH_COLOURS = Pair(Color.BLACK, COLOUR_BRONZE)

    fun getDarkenedColour(colour: Color): Color = colour.darker().darker()

    fun getBrightenedColour(colour: Color): Color {
        var hsbValues = FloatArray(3)
        hsbValues = Color.RGBtoHSB(colour.red, colour.green, colour.blue, hsbValues)

        val decreasedSat = max(0f, (hsbValues[1] - 0.5).toFloat())
        return Color.getHSBColor(hsbValues[0], decreasedSat, hsbValues[2])
    }

    fun setFgAndBgColoursForPosition(c: Component, finishPos: Int, defaultBg: Color? = null) {
        when (finishPos) {
            -1 -> setColors(c, defaultBg, null)
            1 -> setColors(c, Color.YELLOW, COLOUR_GOLD_TEXT)
            2 -> setColors(c, Color.GRAY, COLOUR_SILVER_TEXT)
            3 -> setColors(c, COLOUR_BRONZE, COLOUR_BRONZE_TEXT)
            else -> setColors(c, Color.BLACK, COLOUR_BRONZE)
        }

        c.repaint()
    }

    private fun setColors(c: Component, background: Color?, foreground: Color?) {
        c.background = background
        c.foreground = foreground
    }

    fun getScorerForegroundColour(totalScore: Double): Color {
        val hueFactor = preferenceService.get(Preferences.hueFactor)
        val fgBrightness = preferenceService.get(Preferences.fgBrightness)
        return getScorerColour(totalScore, hueFactor, fgBrightness)
    }

    fun getScorerBackgroundColour(totalScore: Double): Color {
        val hueFactor = preferenceService.get(Preferences.hueFactor)
        val bgBrightness = preferenceService.get(Preferences.bgBrightness)
        return getScorerColour(totalScore, hueFactor, bgBrightness)
    }

    fun getScorerColour(totalScore: Double, multiplier: Double, brightness: Double) =
        getProportionalColour(totalScore, 180, multiplier, brightness)

    private fun getProportionalColour(
        value: Double,
        total: Int,
        multiplier: Double,
        brightness: Double
    ): Color {
        val hue = (value * multiplier).toFloat() / total
        return Color.getHSBColor(hue, 1f, brightness.toFloat())
    }

    fun getProportionalColourRedToGreen(value: Double, total: Int, brightness: Double) =
        getProportionalColour(value, total, 0.4, brightness)
}

fun Color.translucent() = Color(this.red, this.green, this.blue, 120)
