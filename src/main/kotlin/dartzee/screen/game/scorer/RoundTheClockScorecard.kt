package dartzee.screen.game.scorer

import dartzee.core.bean.AbstractTableRenderer
import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.game.state.ClockPlayerState
import dartzee.utils.DartsColour
import dartzee.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import dartzee.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import dartzee.utils.PreferenceUtil
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.SwingConstants

class RoundTheClockScorecard: ScrollTable()
{
    private val tm = TableUtil.DefaultModel()

    init
    {
        model = tm
        (0..3).forEach { _ -> model.addColumn("") }
        (0..3).forEach { ix -> getColumn(ix).cellRenderer = ClockResultRenderer()}
        setRowHeight(25)

        preferredSize = Dimension(100, 140)
        setShowRowCount(false)
    }

    fun stateChanged(state: ClockPlayerState)
    {
        tm.clear()

        val results = (1..20).map { makeClockResult(it, state) }
        results.chunked(4).forEach(::addRow)
    }

    private fun makeClockResult(target: Int, state: ClockPlayerState): ClockResult
    {
        val hit = state.hasHitTarget(target)
        val bruceyChance = state.onTrackForBrucey() && state.getCurrentTarget() == target
        return ClockResult(target, hit, bruceyChance)
    }
}

private data class ClockResult(val value: Int, val hit: Boolean, val bruceyChance: Boolean)

private class ClockResultRenderer: AbstractTableRenderer<ClockResult>()
{
    override fun setFontsAndAlignment()
    {
        horizontalAlignment = SwingConstants.CENTER
        font = Font("Trebuchet MS", Font.BOLD, 15)
    }

    override fun setCellColours(typedValue: ClockResult?, isSelected: Boolean)
    {
        typedValue ?: return

        val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
        val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

        if (typedValue.bruceyChance)
        {
            DartsColour.setFgAndBgColoursForPosition(this, 1)
        }
        else if (typedValue.hit)
        {
            foreground = Color.getHSBColor(0.3f, 1f, fgBrightness.toFloat())
            background = Color.getHSBColor(0.3f, 1f, bgBrightness.toFloat())
        }
        else
        {
            foreground = Color.getHSBColor(0.0f, 1f, fgBrightness.toFloat())
            background = Color.getHSBColor(0.0f, 1f, bgBrightness.toFloat())
        }
    }

    override fun getReplacementValue(value: ClockResult) = "${value.value}"
}