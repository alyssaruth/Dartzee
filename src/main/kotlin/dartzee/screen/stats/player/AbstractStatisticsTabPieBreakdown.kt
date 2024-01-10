package dartzee.screen.stats.player

import dartzee.core.bean.RowSelectionListener
import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.core.util.containsComponent
import dartzee.core.util.getDescription
import dartzee.stats.GameWrapper
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset

abstract class AbstractStatisticsTabPieBreakdown : AbstractStatisticsTab(), RowSelectionListener {
    abstract val ranges: List<IntRange>

    private val tableHoleBreakdown = ScrollTable(testId = "BreakdownMine")
    val tableHoleBreakdownOther = ScrollTable()
    private val tablePanel = JPanel()
    private val pieChartPanel = JPanel()
    private val myPieChartPanel = ChartPanel(null)
    val otherPieChartPanel = ChartPanel(null)

    init {
        layout = GridLayout(1, 3, 0, 0)

        tableHoleBreakdownOther.tableForeground = Color.RED
        tableHoleBreakdown.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tableHoleBreakdownOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tablePanel.border = EmptyBorder(5, 5, 5, 5)
        add(tablePanel)
        tablePanel.layout = GridLayout(2, 1, 0, 0)
        tablePanel.add(tableHoleBreakdown)
        tablePanel.add(tableHoleBreakdownOther)
        add(pieChartPanel)
        pieChartPanel.layout = GridLayout(2, 1, 0, 0)

        pieChartPanel.add(myPieChartPanel)

        pieChartPanel.add(otherPieChartPanel)

        tableHoleBreakdown.addRowSelectionListener(this)
        tableHoleBreakdownOther.addRowSelectionListener(this)
    }

    abstract fun getColorForRange(range: IntRange): Color

    abstract fun getTableRows(filteredGames: List<GameWrapper>): Pair<List<List<Any?>>, List<Any>?>

    open fun applyAdditionalFilters(filteredGames: List<GameWrapper>): List<GameWrapper> =
        filteredGames

    override fun populateStats() {
        setTableVisibility()

        populateHoleBreakdown(tableHoleBreakdown, myPieChartPanel, filteredGames)
        if (includeOtherComparison()) {
            populateHoleBreakdown(tableHoleBreakdownOther, otherPieChartPanel, filteredGamesOther)
        }
    }

    private fun populateHoleBreakdown(
        table: ScrollTable,
        chartPanel: ChartPanel,
        games: List<GameWrapper>
    ) {
        val filteredGames = applyAdditionalFilters(games)
        val model = TableUtil.DefaultModel()
        model.addColumn("Target")

        ranges.forEach { model.addColumn(it.getDescription()) }

        model.addColumn("Avg")
        table.model = model

        val finishedGames = filteredGames.filter { it.isFinished() }
        populateModel(table, finishedGames)

        table.sortBy(0, false)
        table.selectFirstRow()

        updatePieChart(table, chartPanel)
    }

    private fun populateModel(table: ScrollTable, filteredGames: List<GameWrapper>) {
        val allRows = getTableRows(filteredGames)
        val breakdownRows = allRows.first.map { it.toTypedArray() }

        breakdownRows.forEach { table.addRow(it) }

        val totalRow = allRows.second
        if (totalRow != null) {
            table.addFooterRow(totalRow.toTypedArray())
        }
    }

    private fun updatePieChart(table: ScrollTable, panel: ChartPanel) {
        val selectedRow = table.selectedModelRow
        if (selectedRow == -1) {
            // Do nothing
            return
        }

        val selectedHole = table.getValueAt(selectedRow, 0)

        val dataset = DefaultPieDataset<String>()
        val pieChart = ChartFactory.createPieChart("" + selectedHole, dataset, true, true, false)
        val plot = pieChart.plot as PiePlot<*>

        ranges.forEachIndexed { ix, range ->
            dataset.setValue(
                range.getDescription(),
                (table.getNonNullValueAt(selectedRow, ix + 1) as Int)
            )

            val col = getColorForRange(range)
            plot.setSectionPaint(range.getDescription(), col)
        }

        plot.labelGenerator = null
        panel.chart = pieChart
    }

    private fun setTableVisibility() {
        if (!includeOtherComparison()) {
            pieChartPanel.layout = GridLayout(1, 1, 0, 0)
            pieChartPanel.remove(otherPieChartPanel)
            tablePanel.layout = GridLayout(1, 1, 0, 0)
            tablePanel.remove(tableHoleBreakdownOther)
        } else if (!tablePanel.containsComponent(tableHoleBreakdownOther)) {
            pieChartPanel.layout = GridLayout(2, 1, 0, 0)
            pieChartPanel.add(otherPieChartPanel)
            tablePanel.layout = GridLayout(2, 1, 0, 0)
            tablePanel.add(tableHoleBreakdownOther)
        }

        tablePanel.repaint()
    }

    override fun selectionChanged(src: ScrollTable) {
        when (src) {
            tableHoleBreakdown -> {
                updateSelection(tableHoleBreakdown, tableHoleBreakdownOther)
                updatePieChart(tableHoleBreakdown, myPieChartPanel)
            }
            tableHoleBreakdownOther -> {
                updateSelection(tableHoleBreakdownOther, tableHoleBreakdown)
                updatePieChart(tableHoleBreakdownOther, otherPieChartPanel)
            }
        }
    }

    private fun updateSelection(src: ScrollTable, dest: ScrollTable) {
        val row = src.selectedModelRow
        if (row < dest.rowCount) {
            dest.selectRow(row)
        }
    }
}
