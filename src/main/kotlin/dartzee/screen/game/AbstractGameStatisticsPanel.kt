package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.core.bean.ScrollTable
import dartzee.core.util.MathsUtil
import dartzee.core.util.addUnique
import dartzee.game.UniqueParticipantName
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel

/**
 * Shows statistics for each player in a particular game, based on the PlayerStates
 */
abstract class AbstractGameStatisticsPanel<PlayerState: AbstractPlayerState<PlayerState>>: JPanel()
{
    protected val uniqueParticipantNamesOrdered = mutableListOf<UniqueParticipantName>()
    protected var participants: List<IWrappedParticipant> = emptyList()
    protected val hmPlayerToDarts = mutableMapOf<UniqueParticipantName, List<List<Dart>>>()
    protected val hmPlayerToStates = mutableMapOf<UniqueParticipantName, List<PlayerState>>()

    var tm = DefaultTableModel()

    val table = ScrollTable()

    abstract fun getRankedRowsHighestWins(): List<String>
    abstract fun getRankedRowsLowestWins(): List<String>
    abstract fun getHistogramRows(): List<String>
    protected abstract fun getStartOfSectionRows(): List<String>
    protected abstract fun addRowsToTable()

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
        this.participants = playerStates.map { it.wrappedParticipant }

        hmPlayerToDarts.clear()
        hmPlayerToStates.clear()

        val hm = playerStates.groupBy { it.wrappedParticipant.getUniqueParticipantName() }
        hmPlayerToStates.putAll(hm)

        hmPlayerToDarts.putAll(hm.mapValues { it.value.flatMap { state -> state.completedRounds }})

        if (isSufficientData())
        {
            buildTableModel()
        }
    }

    private fun isSufficientData(): Boolean
    {
        val names = hmPlayerToDarts.keys
        return names.all { p -> getFlattenedDarts(p).isNotEmpty() }
    }

    protected fun buildTableModel()
    {
        tm = DefaultTableModel()
        tm.addColumn("")

        for (pt in participants)
        {
            val participantName = pt.getUniqueParticipantName()
            uniqueParticipantNamesOrdered.addUnique(participantName)
        }

        for (participantName in uniqueParticipantNamesOrdered)
        {
            tm.addColumn(participantName)
        }

        table.setRowHeight(20)
        table.model = tm

        addRowsToTable()

        //Rendering
        for (i in 0 until tm.columnCount)
        {
            table.getColumn(i).cellRenderer = factoryStatsCellRenderer()
            table.getColumn(i).headerRenderer = GameStatisticsHeaderRenderer()
        }
    }

    private fun factoryStatsCellRenderer() =
        GameStatisticsCellRenderer(getStartOfSectionRows(), getRankedRowsHighestWins(), getRankedRowsLowestWins(), getHistogramRows())

    protected fun addRow(row: Array<Any?>)
    {
        tm.addRow(row)
    }

    protected fun getFlattenedDarts(uniqueParticipantName: UniqueParticipantName): List<Dart>
    {
        val rounds = hmPlayerToDarts[uniqueParticipantName]
        return rounds?.flatten() ?: listOf()
    }

    protected fun factoryRow(rowName: String): Array<Any?>
    {
        val row = arrayOfNulls<Any>(tm.columnCount)
        row[0] = rowName
        return row
    }

    protected fun getBestGameRow(fn: (s: List<Int>) -> Int) = prepareRow("Best Game") { participantName ->
        val playerPts = getFinishedParticipants(participantName)
        val scores = playerPts.map { it.participant.finalScore }
        if (scores.isEmpty()) null else fn(scores)
    }
    protected fun getAverageGameRow() = prepareRow("Avg Game") { participantName ->
        val playerPts = getFinishedParticipants(participantName)
        val scores = playerPts.map { it.participant.finalScore }
        if (scores.isEmpty()) null else MathsUtil.round(scores.average(), 2)
    }

    private fun getFinishedParticipants(uniqueParticipantName: UniqueParticipantName) = participants.filter { it.getUniqueParticipantName() == uniqueParticipantName && it.participant.finalScore > -1 }

    protected fun prepareRow(name: String, fn: (uniqueParticipantName: UniqueParticipantName) -> Any?): Array<Any?>
    {
        val row = factoryRow(name)

        for (i in uniqueParticipantNamesOrdered.indices)
        {
            val playerName = uniqueParticipantNamesOrdered[i]
            val result = fn(playerName)

            row[i + 1] = result ?: "N/A"
        }

        return row
    }
}
