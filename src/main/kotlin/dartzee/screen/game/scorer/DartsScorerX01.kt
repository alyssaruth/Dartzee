package dartzee.screen.game.scorer

import dartzee.`object`.CheckoutSuggester
import dartzee.`object`.Dart
import dartzee.`object`.DartHint
import dartzee.game.state.X01PlayerState
import dartzee.screen.game.GamePanelPausable
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class DartsScorerX01(parent: GamePanelPausable<*, *>, gameParams: String) : DartsScorerPausable<X01PlayerState>(parent)
{
    private val lblStartingScore = JLabel(gameParams)

    init
    {
        lblStartingScore.horizontalAlignment = SwingConstants.CENTER
        lblStartingScore.font = Font("Trebuchet MS", Font.PLAIN, 16)
        panelNorth.add(lblStartingScore, BorderLayout.SOUTH)
    }

    override fun stateChangedImpl(state: X01PlayerState)
    {
        state.completedRounds.forEachIndexed { ix, round ->
            addDartRound(round)

            val roundNumber = ix + 1
            val scoreRemaining = state.getRemainingScoreForRound(roundNumber)

            model.setValueAt(scoreRemaining, ix, SCORE_COLUMN)
        }

        if (state.currentRound.isNotEmpty())
        {
            addDartRound(state.currentRound)
        }

        addCheckoutSuggestion(state)
    }

    private fun addCheckoutSuggestion(state: X01PlayerState)
    {
        val dartsRemaining = 3 - state.currentRound.size
        val currentScore = state.getRemainingScore()
        val checkout = CheckoutSuggester.suggestCheckout(currentScore, dartsRemaining) ?: return

        if (state.currentRound.isEmpty())
        {
            addRow(makeEmptyRow())
        }

        checkout.forEach(::addDart)
    }

    override fun initImpl()
    {
        tableScores.getColumn(SCORE_COLUMN).cellRenderer = ScorerRenderer()
        for (i in 0 until SCORE_COLUMN)
        {
            tableScores.getColumn(i).cellRenderer = DartRenderer()
        }
    }

    fun getDartsForRow(row: Int): List<Dart>
    {
        val ret = mutableListOf<Dart>()
        for (i in 0 until SCORE_COLUMN)
        {
            val drt = model.getValueAt(row, i) as Dart?
            if (drt != null && drt !is DartHint)
            {
                ret.add(drt)
            }
        }

        return ret
    }

    override fun rowIsComplete(rowNumber: Int) = model.getValueAt(rowNumber, SCORE_COLUMN) != null

    override fun getNumberOfColumns() = SCORE_COLUMN + 1

    private inner class ScorerRenderer : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            horizontalAlignment = SwingConstants.CENTER
            font = Font("Trebuchet MS", Font.BOLD, 15)
            val modelRow = table!!.convertRowIndexToModel(row)

            setColours(table, modelRow)
            return this
        }

        private fun setColours(table: JTable, modelRow: Int)
        {
            if (getDartsForRow(modelRow).isEmpty())
            {
                foreground = null
                background = null
                return
            }

            val tm = table.model
            val totalScore = (getScoreAt(tm, modelRow, 0)
                    + getScoreAt(tm, modelRow, 1)
                    + getScoreAt(tm, modelRow, 2))

            val fg = DartsColour.getScorerForegroundColour(totalScore.toDouble())
            val bg = DartsColour.getScorerBackgroundColour(totalScore.toDouble())

            foreground = fg
            background = bg
        }

        private fun getScoreAt(tm: TableModel, row: Int, col: Int): Int
        {
            val value = tm.getValueAt(row, col) as Dart? ?: return 0
            if (value is DartHint)
            {
                return 0
            }

            return value.getTotal()
        }
    }

    companion object
    {
        private const val SCORE_COLUMN = 3
    }
}
