package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.`object`.DartNotThrown
import dartzee.core.bean.AbstractTableRenderer
import dartzee.game.ClockType
import dartzee.screen.game.GamePanelPausable
import dartzee.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import dartzee.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import dartzee.utils.PreferenceUtil
import java.awt.Color
import java.awt.Font
import javax.swing.SwingConstants

class DartsScorerRoundTheClock(parent: GamePanelPausable<out DartsScorerPausable>, private val clockType: ClockType) : DartsScorerPausable(parent)
{
    //Always start at 1. Bit of an abuse to stick this here, it just avoids having another hmPlayerNumber->X.
    private var clockTarget = 1
    var currentClockTarget = 1
        private set

    init
    {
        for (i in 0..BONUS_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

    override fun confirmCurrentRound()
    {
        clockTarget = currentClockTarget
    }

    override fun clearRound(roundNumber: Int)
    {
        super.clearRound(roundNumber)
        currentClockTarget = clockTarget
    }

    override fun playerIsFinished(): Boolean
    {
        return clockTarget > 20
    }

    override fun getTotalScore(): Int
    {
        val rowCount = model.rowCount
        var dartCount = 0

        for (i in 0 until rowCount)
        {
            for (j in 0..BONUS_COLUMN)
            {
                val drt = model.getValueAt(i, j) as Dart?
                if (drt != null && drt !is DartNotThrown)
                {
                    dartCount++
                }
            }
        }

        return dartCount
    }

    override fun rowIsComplete(rowNumber: Int): Boolean
    {
        return model.getValueAt(rowNumber, BONUS_COLUMN - 1) != null && model.getValueAt(rowNumber, BONUS_COLUMN) != null
    }

    override fun getNumberOfColumns(): Int
    {
        return 4 //3 darts, plus bonus for hitting three consecutive
    }

    override fun getNumberOfColumnsForAddingNewDart(): Int
    {
        return getNumberOfColumns() //They're all for containing darts
    }

    override fun initImpl() {}

    fun incrementCurrentClockTarget()
    {
        currentClockTarget++
    }

    fun disableBrucey()
    {
        val row = model.rowCount - 1
        model.setValueAt(DartNotThrown(), row, BONUS_COLUMN)
    }

    private inner class DartRenderer : AbstractTableRenderer<Dart>()
    {
        override fun setFontsAndAlignment()
        {
            horizontalAlignment = SwingConstants.CENTER
            font = Font("Trebuchet MS", Font.BOLD, 15)
        }

        override fun setCellColours(typedValue: Dart?, isSelected: Boolean)
        {
            if (typedValue == null)
            {
                foreground = null
                background = null
            }
            else if (typedValue is DartNotThrown)
            {
                foreground = Color.BLACK
                background = Color.BLACK
            }
            else
            {
                val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
                val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

                var hue = 0f //Red
                if (typedValue.hitClockTarget(clockType))
                {
                    hue = 0.3.toFloat() //Green
                }

                foreground = Color.getHSBColor(hue, 1f, fgBrightness.toFloat())
                background = Color.getHSBColor(hue, 1f, bgBrightness.toFloat())
            }
        }

        override fun getReplacementValue(value: Dart) =
            if (!value.hitClockTarget(clockType))
            {
                "X"
            } else "$value"

        override fun allowNulls() = true
    }

    companion object
    {
        private const val BONUS_COLUMN = 3
    }
}
