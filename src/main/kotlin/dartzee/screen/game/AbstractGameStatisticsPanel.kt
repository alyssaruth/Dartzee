package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.core.bean.ScrollTable
import dartzee.core.util.Debug
import dartzee.core.util.MathsUtil
import dartzee.core.util.addUnique
import dartzee.db.ParticipantEntity
import dartzee.game.state.AbstractPlayerState
import dartzee.utils.DartsColour
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Shows statistics for each player in a particular game.
 * Runs ad-hoc SQL to get the stats, because the full detail isn't readily available in memory (and would be messy to maintain)
 */
abstract class AbstractGameStatisticsPanel<PlayerState: AbstractPlayerState<*>>(protected val gameParams: String) : JPanel()
{
    protected var playerNamesOrdered = mutableListOf<String>()
    protected var participants: List<ParticipantEntity>? = null
    protected val hmPlayerToDarts = mutableMapOf<String, List<List<Dart>>>()
    protected val hmPlayerToStates = mutableMapOf<String, List<PlayerState>>()

    private var tm = DefaultTableModel()

    protected val table = ScrollTable()

    protected abstract fun getRankedRowsHighestWins(): List<String>
    protected abstract fun getRankedRowsLowestWins(): List<String>
    protected abstract fun getHistogramRows(): List<String>
    protected abstract fun getStartOfSectionRows(): List<String>

    private fun getHistogramRowNumbers(): MutableList<Int>
    {
        val ret = mutableListOf<Int>()
        for (i in 0 until table.rowCount)
        {
            val columnName = table.getValueAt(i, 0)
            if (getHistogramRows().contains(columnName))
            {
                ret.add(i)
            }
        }

        return ret
    }


    init
    {
        layout = BorderLayout(0, 0)
        add(table, BorderLayout.CENTER)

        table.disableSorting()
        table.setTableBorder(EmptyBorder(10, 5, 0, 5))

        val c = UIManager.getColor("Panel.background")
        val c2 = Color(c.red, c.green, c.blue)
        table.setTableBackground(c2)

        table.setShowRowCount(false)
    }

    fun showStats(playerStates: List<PlayerState>)
    {
        this.participants = playerStates.map { it.pt }

        hmPlayerToDarts.clear()
        hmPlayerToStates.clear()

        val hm = playerStates.groupBy { it.pt.getPlayerName() }
        hmPlayerToStates.putAll(hm)

        hmPlayerToDarts.putAll(hm.mapValues { it.value.flatMap { state -> state.darts }})

        if (isSufficientData())
        {
            buildTableModel()
        }
    }

    private fun isSufficientData(): Boolean
    {
        val playerNames = hmPlayerToDarts.keys
        return playerNames.all { p -> getFlattenedDarts(p).isNotEmpty() }
    }

    protected fun getRowWidth() = playerNamesOrdered.size + 1

    protected fun getAverageGameRow(): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "Avg Game"

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]

            val playerPts = getFinishedParticipants(playerName)
            if (playerPts.isEmpty())
            {
                row[i + 1] = "N/A"
            } else
            {
                val avg = playerPts.map { pt -> pt.finalScore }.average()
                row[i + 1] = MathsUtil.round(avg, 2)
            }
        }

        return row
    }

    protected fun buildTableModel()
    {
        tm = DefaultTableModel()
        tm.addColumn("")

        for (pt in participants!!)
        {
            val playerName = pt.getPlayerName()
            playerNamesOrdered.addUnique(playerName)
        }

        for (playerName in playerNamesOrdered)
        {
            tm.addColumn(playerName)
        }

        table.setRowHeight(20)
        table.model = tm

        addRowsToTable()

        //Rendering
        for (i in 0 until getRowWidth())
        {
            table.getColumn(i).cellRenderer = ScorerRenderer()
            table.getColumn(i).headerRenderer = HeaderRenderer()
        }
    }

    protected fun addRow(row: Array<Any?>)
    {
        tm.addRow(row)
    }

    protected fun getFlattenedDarts(playerName: String): List<Dart>
    {
        val rounds = hmPlayerToDarts[playerName]

        rounds ?: return mutableListOf()

        return rounds.flatten()
    }

    protected fun factoryRow(rowName: String): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = rowName
        return row
    }

    protected fun getBestGameRow(fn: (s: List<Int>) -> Int): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "Best Game"

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val playerPts = getFinishedParticipants(playerName)

            if (playerPts.isEmpty())
            {
                row[i + 1] = "N/A"
            }
            else
            {
                val scores = playerPts.map { pt -> pt.finalScore }
                row[i + 1] = fn.invoke(scores)
            }

        }

        return row
    }

    private fun getFinishedParticipants(playerName: String): MutableList<ParticipantEntity>
    {
        return participants!!.filter { pt -> pt.getPlayerName() == playerName && pt.finalScore > -1 }.toMutableList()
    }

    protected fun prepareRow(name: String, fn: (playerName: String) -> Any?): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = name

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val result = fn(playerName)

            row[i + 1] = result ?: "N/A"
        }

        return row
    }

    protected abstract fun addRowsToTable()

    private inner class HeaderRenderer : JTextPane(), TableCellRenderer
    {
        init
        {
            val doc = this.styledDocument
            val center = SimpleAttributeSet()
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER)
            doc.setParagraphAttributes(0, doc.length, center, false)
        }

        override fun getTableCellRendererComponent(table: JTable,
                                                   value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                                   column: Int): Component
        {
            text = value as String
            font = Font("Trebuchet MS", Font.BOLD, 15)
            border = getBorder(column)

            setSize(table.columnModel.getColumn(column).width, preferredSize.height)

            if (column == 0)
            {
                background = Color(0, 0, 0, 0)
                isOpaque = false
            }

            return this
        }

        private fun getBorder(column: Int): MatteBorder
        {
            val top = if (column == 0) 0 else 2
            val left = if (column == 0) 0 else 1
            val right = if (column == getRowWidth() - 1) 2 else 1

            return MatteBorder(top, left, 2, right, Color.BLACK)
        }
    }

    private inner class ScorerRenderer : DefaultTableCellRenderer()
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            horizontalAlignment = SwingConstants.CENTER

            if (column == 0)
            {
                font = Font("Trebuchet MS", Font.BOLD, 15)
            }
            else
            {
                font = Font("Trebuchet MS", Font.PLAIN, 15)
            }

            setColours(table, row, column)
            border = getBorder(table!!, row, column)

            return this
        }

        private fun getBorder(table: JTable, row: Int, column: Int): MatteBorder
        {
            val left = if (column == 0) 2 else 1
            val right = if (column == getRowWidth() - 1) 2 else 1


            val bottom = if (row == table.rowCount - 1) 2 else 0

            val startOfSectionRow = getStartOfSectionRows().contains(table.getValueAt(row, 0))
            val top = if (startOfSectionRow) 2 else 0

            return MatteBorder(top, left, bottom, right, Color.BLACK)
        }

        private fun setColours(table: JTable?, row: Int, column: Int)
        {
            if (column == 0)
            {
                //Do nothing
                foreground = null
                background = Color.WHITE
                return
            }

            val tm = table!!.model

            val rowName = table.getValueAt(row, 0)
            if (getRankedRowsHighestWins().contains(rowName))
            {
                val pos = getPositionForColour(tm, row, column, true)
                DartsColour.setFgAndBgColoursForPosition(this, pos, Color.WHITE)
            }
            else if (getRankedRowsLowestWins().contains(rowName))
            {
                val pos = getPositionForColour(tm, row, column, false)
                DartsColour.setFgAndBgColoursForPosition(this, pos, Color.WHITE)
            }
            else if (getHistogramRows().contains(rowName))
            {
                val sum = getHistogramSum(tm, column)

                val thisValue = getDoubleAt(tm, row, column)
                val percent = if (sum == 0L) 0f else thisValue.toFloat() / sum

                val bg = Color.getHSBColor(0.5.toFloat(), percent, 1f)

                foreground = null
                background = bg
            }
            else
            {
                foreground = null
                background = Color.WHITE
            }
        }

        private fun getDoubleAt(tm: TableModel, row: Int, col: Int): Double
        {
            val thisValue = tm.getValueAt(row, col)

            if (thisValue == null)
            {
                Debug.append("ROW: $row, COL: $col")
                return -1.0
            }

            return (thisValue as Number).toDouble()
        }

        private fun getPositionForColour(tm: TableModel, row: Int, col: Int, highestWins: Boolean): Int
        {
            if (tm.getValueAt(row, col) is String || playerNamesOrdered.size == 1)
            {
                return -1
            }

            val myScore = getDoubleAt(tm, row, col)

            var myPosition = 1
            for (i in 1 until tm.columnCount)
            {
                if (i == col || tm.getValueAt(row, i) is String)
                {
                    continue
                }

                val theirScore = getDoubleAt(tm, row, i)

                //Compare positivity to the boolean
                val result = java.lang.Double.compare(theirScore, myScore)
                if (result > 0 == highestWins && result != 0)
                {
                    myPosition++
                }
            }

            return myPosition
        }

        private fun getHistogramSum(tm: TableModel, col: Int): Long
        {
            return getHistogramRowNumbers().map { row -> (tm.getValueAt(row, col) as Number).toLong() }
                    .sum()
        }
    }
}