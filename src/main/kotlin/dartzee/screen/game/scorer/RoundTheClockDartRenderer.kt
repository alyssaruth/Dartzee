package dartzee.screen.game.scorer

import dartzee.core.bean.AbstractTableRenderer
import dartzee.game.ClockType
import dartzee.`object`.Dart
import dartzee.`object`.DartNotThrown
import dartzee.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import dartzee.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import dartzee.utils.PreferenceUtil
import java.awt.Color
import java.awt.Font
import javax.swing.SwingConstants

class RoundTheClockDartRenderer(private val clockType: ClockType) : AbstractTableRenderer<Dart>() {
    override fun setFontsAndAlignment() {
        horizontalAlignment = SwingConstants.CENTER
        font = Font("Trebuchet MS", Font.BOLD, 15)
    }

    override fun setCellColours(typedValue: Dart?, isSelected: Boolean) {
        if (typedValue == null) {
            foreground = null
            background = null
        } else if (typedValue is DartNotThrown) {
            foreground = Color.BLACK
            background = Color.BLACK
        } else {
            val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
            val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

            var hue = 0f // Red
            if (typedValue.hitClockTarget(clockType)) {
                hue = 0.3f // Green
            } else if (typedValue.hitAnyClockTarget(clockType)) {
                hue = 0.15f // orangey
            }

            foreground = Color.getHSBColor(hue, 1f, fgBrightness.toFloat())
            background = Color.getHSBColor(hue, 1f, bgBrightness.toFloat())
        }
    }

    override fun getReplacementValue(value: Dart) =
        if (!value.hitAnyClockTarget(clockType)) "X" else "$value"

    override fun allowNulls() = true
}
