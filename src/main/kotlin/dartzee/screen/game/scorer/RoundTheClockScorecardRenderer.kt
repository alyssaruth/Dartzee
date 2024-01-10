package dartzee.screen.game.scorer

import dartzee.core.bean.AbstractTableRenderer
import dartzee.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import dartzee.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import dartzee.utils.PreferenceUtil
import java.awt.Color
import java.awt.Font
import javax.swing.SwingConstants
import javax.swing.border.LineBorder

class RoundTheClockScorecardRenderer : AbstractTableRenderer<ClockResult>() {
    override fun setFontsAndAlignment() {
        horizontalAlignment = SwingConstants.CENTER
        font = Font("Trebuchet MS", Font.BOLD, 15)
    }

    override fun setCellColours(typedValue: ClockResult?, isSelected: Boolean) {
        typedValue ?: return

        if (!typedValue.hit) {
            foreground = Color.BLACK
            background = null
        } else {
            val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
            val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

            foreground = Color.getHSBColor(0.3f, 1f, fgBrightness.toFloat())
            background = Color.getHSBColor(0.3f, 1f, bgBrightness.toFloat())
        }

        if (typedValue.isCurrentTarget) {
            border = LineBorder(Color.RED, 2)
        } else {
            border = null
        }
    }

    override fun getReplacementValue(value: ClockResult) = "${value.value}"
}
