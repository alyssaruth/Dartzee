package burlton.dartzee.code.screen.stats.player.rtc

import burlton.core.code.obj.HashMapCount
import burlton.core.code.util.getDescription
import burlton.dartzee.code.screen.stats.player.AbstractStatisticsTab
import burlton.dartzee.code.stats.GameWrapper
import burlton.desktopcore.code.bean.RowSelectionListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil
import burlton.desktopcore.code.util.containsComponent
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.UIManager
import javax.swing.border.TitledBorder

class StatisticsTabRoundTheClockHitRate : AbstractStatisticsTab(), RowSelectionListener
{
    private val ranges = mutableListOf(1..1, 2..2, 3..3, 4..6, 7..10, 11..15, 16.until(Int.MAX_VALUE))

    private val tableHoleBreakdown = ScrollTable()
    val tableHoleBreakdownOther = ScrollTable()
    private val tablePanel = JPanel()
    private val pieChartPanel = JPanel()
    private val myPieChartPanel = ChartPanel(null)
    val otherPieChartPanel = ChartPanel(null)

    init
    {
        layout = GridLayout(1, 3, 0, 0)

        tableHoleBreakdownOther.tableForeground = Color.RED
        tableHoleBreakdown.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tableHoleBreakdownOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        tablePanel.border = TitledBorder(UIManager.getBorder("TitledBorder.border"), "Double Finishes", TitledBorder.LEADING, TitledBorder.TOP, null, Color(0, 0, 0))
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

    override fun populateStats()
    {
        setTableVisibility()

        populateHoleBreakdown(tableHoleBreakdown, myPieChartPanel, filteredGames)
        if (includeOtherComparison())
        {
            populateHoleBreakdown(tableHoleBreakdownOther, otherPieChartPanel, filteredGamesOther)
        }
    }
    private fun populateHoleBreakdown(table: ScrollTable, chartPanel: ChartPanel, filteredGames: List<GameWrapper>)
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("Target")

        ranges.forEach{
            model.addColumn(it.getDescription())
        }

        model.addColumn("Avg")
        table.model = model

        val finishedGames = filteredGames.filter{ it.isFinished() }
        populateModel(table, finishedGames)

        table.sortBy(0, false)
        table.disableSorting()
        table.selectFirstRow()

        updatePieChart(table, chartPanel)
    }

    private fun populateModel(table: ScrollTable, filteredGames: List<GameWrapper>)
    {
        val hmTargetToAverageDarts = getAverageThrowsPerTarget(filteredGames)
        val hmTargetToRangeBreakdown = getRangeBreakdownPerTarget(filteredGames)

        hmTargetToRangeBreakdown.forEach{ target, rangeToCount ->
            val avg = hmTargetToAverageDarts[target]

            val row = listOf(target) + ranges.map { rangeToCount.getCount(it) as Any? } + listOf(avg)

            table.addRow(row.toTypedArray())
        }

        //Overall
        val overallAverage = hmTargetToAverageDarts.values.sum() / 20
        val totalRow = arrayOf<Any?>("", -1, -1, -1, -1, -1, -1, -1, overallAverage)
        table.addFooterRow(totalRow)
    }

    private fun updatePieChart(table: ScrollTable, panel: ChartPanel)
    {
        val selectedRow = table.selectedModelRow
        if (selectedRow == -1)
        {
            //Do nothing
            return
        }

        val selectedHole = table.getValueAt(selectedRow, 0)

        val dataset = DefaultPieDataset()
        val pieChart = ChartFactory.createPieChart("" + selectedHole, dataset, true, true, false)
        val plot = pieChart.plot as PiePlot

        ranges.forEachIndexed { ix, range ->
            dataset.setValue(range.getDescription(), (table.getValueAt(selectedRow, ix + 1) as Int))
        }

        plot.labelGenerator = null
        panel.chart = pieChart
    }

    fun getAverageThrowsPerTarget(games: List<GameWrapper>): Map<Int, Double>
    {
        return games.flatMap{ it.getAllDarts() }
                    .groupBy{ it.startingScore }
                    .mapValues{ it.value.size.toDouble() / games.size }
    }

    fun getRangeBreakdownPerTarget(games: List<GameWrapper>): Map<Int, HashMapCount<IntRange>>
    {
        val hmRangeBreakdown = mutableMapOf<Int, HashMapCount<IntRange>>()

        val hmCountConstructor = { HashMapCount<IntRange>() }
        val individualGameRanges = games.map{ it.getRangeByTarget(ranges) }
        individualGameRanges.forEach{
            it.forEach{ target, range ->
                val hmCount = hmRangeBreakdown.getOrPut(target, hmCountConstructor)
                hmCount.incrementCount(range)
            }
        }

        return hmRangeBreakdown
    }

    private fun setTableVisibility()
    {
        if (!includeOtherComparison())
        {
            pieChartPanel.layout = GridLayout(1, 1, 0, 0)
            pieChartPanel.remove(otherPieChartPanel)
            tablePanel.layout = GridLayout(1, 1, 0, 0)
            tablePanel.remove(tableHoleBreakdownOther)
        }
        else if (!containsComponent(tablePanel, tableHoleBreakdownOther))
        {
            pieChartPanel.layout = GridLayout(2, 1, 0, 0)
            pieChartPanel.add(otherPieChartPanel)
            tablePanel.layout = GridLayout(2, 1, 0, 0)
            tablePanel.add(tableHoleBreakdownOther)
        }

        tablePanel.repaint()
    }

    override fun selectionChanged(src: ScrollTable)
    {
        when (src)
        {
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
    private fun updateSelection(src: ScrollTable, dest: ScrollTable)
    {
        val row = src.selectedModelRow
        if (row < dest.rowCount)
        {
            dest.selectRow(row)
        }
    }
}