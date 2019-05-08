package burlton.dartzee.code.screen.game

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.utils.PREFERENCES_DOUBLE_BG_BRIGHTNESS
import burlton.dartzee.code.utils.PREFERENCES_DOUBLE_FG_BRIGHTNESS
import burlton.dartzee.code.utils.PreferenceUtil
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.border.Border
import javax.swing.border.MatteBorder
import javax.swing.table.DefaultTableCellRenderer

class DartsScorerGolf : DartsScorer()
{
    private var currentScore = 0
    var fudgeFactor = 0 //For when we're displaying only a back 9, we need to shift everything up
    var showGameId = false

    override fun clearRound(roundNumber: Int)
    {
        when (roundNumber)
        {
            in 1..ROUNDS_HALFWAY -> super.clearRound(roundNumber)
            else -> super.clearRound(roundNumber + 1)
        }
    }

    override fun getNumberOfColumns(): Int
    {
        return 5 + if (showGameId) 1 else 0
    }

    override fun initImpl(gameParams: String)
    {
        for (i in 0..SCORE_COLUMN)
        {
            tableScores.setRenderer(i, DartRenderer(showGameId))
        }

        if (showGameId)
        {
            tableScores.setLinkColumnIndex(tableScores.columnCount - 1)
        }
    }

    override fun getEmptyRow(): Array<Any>
    {
        val emptyRow = super.getEmptyRow()

        //Set the first column to be the round number
        val rowCount = model.rowCount
        emptyRow[0] = getTargetForRowNumber(rowCount)

        return emptyRow
    }

    override fun rowIsComplete(rowNumber: Int): Boolean
    {
        return model.getValueAt(rowNumber, SCORE_COLUMN) != null
    }

    fun setTableForeground(color: Color)
    {
        tableScores.tableForeground = color
        lblResult.foreground = color
    }

    /**
     * Helper to add a full round at a time, for when we're viewing stats or loading a game
     */
    @JvmOverloads
    fun addDarts(darts: Collection<Dart>, gameId: Long = -1)
    {
        for (dart in darts)
        {
            addDart(dart)
        }

        if (gameId > -1)
        {
            val row = tableScores.rowCount - 1
            val column = tableScores.columnCount - 1
            model.setValueAt(gameId, row, column)
        }

        finaliseRoundScore()
    }

    fun finaliseRoundScore()
    {
        val rowNumber = model.rowCount - 1
        val target = getTargetForRowNumber(rowNumber)
        val drt = getLastDartThrown(rowNumber)

        val score = drt!!.getGolfScore(target)

        model.setValueAt(score, rowNumber, SCORE_COLUMN)

        currentScore += score

        if (target == ROUNDS_HALFWAY || target == ROUNDS_FULL)
        {
            val totalRow = arrayOf<Any?>(null, null, null, null, Integer.valueOf(currentScore))
            addRow(totalRow)
        }

        lblResult.text = "" + currentScore
        lblResult.isVisible = true
    }

    private fun getLastDartThrown(rowNumber: Int): Dart?
    {
        var ret: Dart? = null
        for (i in 1 until SCORE_COLUMN)
        {
            val drt = model.getValueAt(rowNumber, i)
            if (drt != null)
            {
                ret = drt as Dart
            }
        }

        return ret
    }

    override fun getTotalScore(): Int
    {
        return currentScore
    }

    /**
     * Static methods
     */
    private fun getTargetForRowNumber(row: Int): Int
    {
        if (row < ROUNDS_HALFWAY)
        {
            //Row 0 is 1, etc.
            return row + fudgeFactor + 1
        }

        if (row > ROUNDS_HALFWAY)
        {
            //We have an extra subtotal row to consider
            return row + fudgeFactor
        }

        Debug.stackTrace("Trying to get round target for the subtotal row")
        return -1
    }

    /**
     * Inner Classes
     */
    private class DartRenderer(private val showGameId: Boolean) : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            val newValue = getReplacementValue(table, value, row)
            val cell = super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column) as JComponent

            horizontalAlignment = SwingConstants.CENTER
            font = Font("Trebuchet MS", Font.BOLD, 15)

            val border = getBorderForCell(row, column)
            cell.border = border

            if (column == 0
                    || newValue == null
                    || isScoreRow(row))
            {
                foreground = null
                background = null
            } else
            {
                val score = newValue as Int

                val bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS)
                val fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS)

                foreground = getScorerColour(score, fgBrightness)
                background = getScorerColour(score, bgBrightness)
            }

            return this
        }

        fun getReplacementValue(table: JTable?, obj: Any?, row: Int): Any?
        {
            if (obj == null)
            {
                return null
            }

            if (obj !is Dart)
            {
                return obj
            }

            val drt = obj as Dart?
            val target = table!!.getValueAt(row, 0) as Int

            return drt!!.getGolfScore(target)
        }

        private fun getBorderForCell(row: Int, col: Int): Border
        {
            var top = 0
            var bottom = 0
            var left = 0
            var right = 0

            if (isScoreRow(row))
            {
                top = 2
                bottom = 2
            }

            if (col == 1)
            {
                left = 2
            }

            if (col == 3)
            {
                right = 2
            }

            if (showGameId && col == 4)
            {
                right = 2
            }

            return MatteBorder(top, left, bottom, right, Color.BLACK)
        }
    }

    companion object
    {
        private const val ROUNDS_HALFWAY = 9
        private const val ROUNDS_FULL = 18
        private const val SCORE_COLUMN = 4

        fun isScoreRow(row: Int): Boolean
        {
            return row == ROUNDS_HALFWAY || row == ROUNDS_FULL + 1
        }

        fun getScorerColour(score: Int, brightness: Double): Color
        {
            val hue = when(score)
            {
                4 -> 0.1f
                3 -> 0.2f
                2 -> 0.3f
                1 -> 0.5f
                else -> 0f
            }

            return Color.getHSBColor(hue, 1f, brightness.toFloat())
        }
    }

}
