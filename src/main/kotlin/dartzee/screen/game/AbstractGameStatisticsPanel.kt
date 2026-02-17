package dartzee.screen.game

import dartzee.core.bean.ScrollTable
import dartzee.core.bean.makeTransparentTextPane
import dartzee.core.util.MathsUtil
import dartzee.core.util.TableUtil
import dartzee.core.util.addUnique
import dartzee.core.util.alignCentrally
import dartzee.core.util.append
import dartzee.core.util.setFontSize
import dartzee.game.UniqueParticipantName
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.`object`.Dart
import dartzee.utils.InjectedThings
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

/** Shows statistics for each player in a particular game, based on the PlayerStates */
abstract class AbstractGameStatisticsPanel<PlayerState : AbstractPlayerState<PlayerState>> :
    JPanel() {
    protected val uniqueParticipantNamesOrdered = mutableListOf<UniqueParticipantName>()
    protected var participants: List<IWrappedParticipant> = emptyList()
    protected val hmPlayerToDarts = mutableMapOf<UniqueParticipantName, List<List<Dart>>>()
    protected val hmPlayerToStates = mutableMapOf<UniqueParticipantName, List<PlayerState>>()

    val table = ScrollTable()

    abstract fun getRankedRowsHighestWins(): List<String>

    abstract fun getRankedRowsLowestWins(): List<String>

    abstract fun getHistogramRows(): List<String>

    protected abstract fun getStartOfSectionRows(): List<String>

    protected abstract fun addRowsToTable()

    init {
        layout = BorderLayout(0, 0)
        add(table, BorderLayout.CENTER)

        table.disableSorting()
        table.setTableBorder(EmptyBorder(10, 5, 0, 5))

        val c = UIManager.getColor("Panel.background")
        val c2 = Color(c.red, c.green, c.blue)
        table.setTableBackground(c2)
        table.setShowRowCount(false)

        table.setRowHeight(20)
        table.model = TableUtil.DefaultModel()
    }

    fun showStats(playerStates: List<PlayerState>) {
        val statesToUse = playerStates.filterNot { it.hasResigned() }

        this.participants = statesToUse.map { it.wrappedParticipant }

        uniqueParticipantNamesOrdered.clear()
        hmPlayerToDarts.clear()
        hmPlayerToStates.clear()

        val hm = statesToUse.groupBy { it.wrappedParticipant.getUniqueParticipantName() }
        hmPlayerToStates.putAll(hm)

        hmPlayerToDarts.putAll(hm.mapValues { it.value.flatMap { state -> state.completedRounds } })

        if (isSufficientData()) {
            buildTableModel()
        }

        val activeCount = playerStates.count { it.wrappedParticipant.participant.isActive() }
        if (InjectedThings.partyMode && activeCount <= 1) {
            addGameOverMessage(playerStates)
        }
    }

    private fun addGameOverMessage(playerStates: List<PlayerState>) {
        val winner = playerStates.first { it.wrappedParticipant.participant.finishingPosition == 1 }
        val heading =
            makeTransparentTextPane().apply {
                border = EmptyBorder(10, 0, 20, 0)
                alignCentrally()
                setFontSize(36)
                append("Game Over!")
            }

        heading.preferredSize = Dimension(100, 65)
        add(heading, BorderLayout.NORTH)

        val textPane =
            makeTransparentTextPane().apply {
                alignCentrally()
                setFontSize(18)

                append("Congrats to ")
                append(winner.wrappedParticipant.getParticipantName(), true)
                append(" on the win!")
                append("\n\n")
                append("When you're done looking at the stats, this window can be closed.")
            }

        textPane.preferredSize = Dimension(100, 250)
        add(textPane, BorderLayout.SOUTH)
    }

    private fun isSufficientData(): Boolean {
        val names = hmPlayerToDarts.keys
        return names.all { p -> getFlattenedDarts(p).isNotEmpty() }
    }

    protected fun buildTableModel() {
        table.model = TableUtil.DefaultModel()

        for (pt in participants) {
            val participantName = pt.getUniqueParticipantName()
            uniqueParticipantNamesOrdered.addUnique(participantName)
        }

        val requiredColumns = listOf("") + uniqueParticipantNamesOrdered.map { it.value }
        requiredColumns.forEach(table::addColumn)

        addRowsToTable()

        // Rendering
        for (i in 0 until table.columnCount) {
            table.getColumn(i).cellRenderer = factoryStatsCellRenderer()
            table.getColumn(i).headerRenderer = GameStatisticsHeaderRenderer()
        }
    }

    private fun factoryStatsCellRenderer() =
        GameStatisticsCellRenderer(
            getStartOfSectionRows(),
            getRankedRowsHighestWins(),
            getRankedRowsLowestWins(),
            getHistogramRows(),
        )

    protected fun addRow(row: Array<Any?>) {
        table.model.addRow(row)
    }

    protected fun getFlattenedDarts(uniqueParticipantName: UniqueParticipantName): List<Dart> {
        val rounds = hmPlayerToDarts[uniqueParticipantName]
        return rounds?.flatten().orEmpty()
    }

    protected fun factoryRow(rowName: String): Array<Any?> {
        val row = arrayOfNulls<Any>(table.columnCount)
        row[0] = rowName
        return row
    }

    protected fun getBestGameRow(fn: (s: List<Int>) -> Int) =
        prepareRow("Best Game") { participantName ->
            val playerPts = getFinishedParticipants(participantName)
            val scores = playerPts.map { it.participant.finalScore }
            if (scores.isEmpty()) null else fn(scores)
        }

    protected fun getAverageGameRow() =
        prepareRow("Avg Game") { participantName ->
            val playerPts = getFinishedParticipants(participantName)
            val scores = playerPts.map { it.participant.finalScore }
            if (scores.isEmpty()) null else MathsUtil.round(scores.average(), 2)
        }

    private fun getFinishedParticipants(uniqueParticipantName: UniqueParticipantName) =
        participants.filter {
            it.getUniqueParticipantName() == uniqueParticipantName && it.participant.finalScore > -1
        }

    protected fun prepareRow(
        name: String,
        fn: (uniqueParticipantName: UniqueParticipantName) -> Any?,
    ): Array<Any?> {
        val row = factoryRow(name)

        for (i in uniqueParticipantNamesOrdered.indices) {
            val playerName = uniqueParticipantNamesOrdered[i]
            val result = fn(playerName)

            row[i + 1] = result ?: "N/A"
        }

        return row
    }
}
