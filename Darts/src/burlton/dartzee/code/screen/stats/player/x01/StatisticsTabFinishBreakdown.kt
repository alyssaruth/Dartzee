package burlton.dartzee.code.screen.stats.player.x01

import burlton.core.code.util.MathsUtil.Companion.getPercentage
import burlton.dartzee.code.screen.stats.player.AbstractStatisticsTab
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.code.utils.getCheckoutSingles
import burlton.desktopcore.code.bean.RowSelectionListener
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil.DefaultModel
import burlton.desktopcore.code.util.containsComponent
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.TitledBorder


class StatisticsTabFinishBreakdown: AbstractStatisticsTab(), RowSelectionListener
{
    private var selectedScore: Int? = null

    private val tableFavouriteDoubles = ScrollTable()
    private val tableFavouriteDoublesOther = ScrollTable()
    private val tablePanel = JPanel()
    private val pieChartPanel = ChartPanel(null)

    init
	{
		layout = GridLayout(1, 3, 0, 0)

		tableFavouriteDoublesOther.tableForeground = Color.RED
		tableFavouriteDoubles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		tableFavouriteDoublesOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		tablePanel.border = TitledBorder(null, "Double Finishes")

		add(tablePanel)
		tablePanel.layout = GridLayout(2, 1, 0, 0)
		tablePanel.add(tableFavouriteDoubles)
		tablePanel.add(tableFavouriteDoublesOther)
		add(pieChartPanel)

        tableFavouriteDoubles.addRowSelectionListener(this)
	}

    override fun populateStats()
    {
        setTableVisibility()

        val dataset = buildFavouriteDoublesData(tableFavouriteDoubles, filteredGames)
        if (includeOtherComparison())
        {
            buildFavouriteDoublesData(tableFavouriteDoublesOther, filteredGamesOther)
        }

        val pieChart = ChartFactory.createPieChart("Finishes", dataset, true, true, false)
        val plot = pieChart.plot as PiePlot
        plot.labelGenerator = null
        pieChartPanel.chart = pieChart
    }

    private fun buildFavouriteDoublesData(table: ScrollTable, filteredGames: List<GameWrapper>): DefaultPieDataset
    {
        val model = DefaultModel()
        model.addColumn("Double")
        model.addColumn("Finishes")
        model.addColumn("%")
        val dataset = populateFavouriteDoubles(model, filteredGames)
        table.model = model
        table.sortBy(2, true)
        return dataset
    }

    private fun populateFavouriteDoubles(model: DefaultModel, filteredGames: List<GameWrapper>): DefaultPieDataset
    {
        val scores = filteredGames.filter { it.isFinished() }.map { it.getDartsForFinalRound().last().score }

        val rows = scores.distinct().map {
            val count = scores.count { score -> score == it }
            val percent = getPercentage(count, scores.size.toDouble())
            arrayOf(it, count, percent)
        }

        model.addRows(rows)

        //Build up the pie set. Unlike the table, we need ALL values
        val dataset = DefaultPieDataset()
        getCheckoutSingles().sorted().forEach {
            val count = scores.count { score -> score == it }
            dataset.setValue(it.toString(), count)
        }

        return dataset
    }

    private fun setTableVisibility()
    {
        if (!includeOtherComparison())
        {
            tablePanel.layout = GridLayout(1, 1, 0, 0)
            tablePanel.remove(tableFavouriteDoublesOther)
        }
        else if (!containsComponent(tablePanel, tableFavouriteDoublesOther))
        {
            tablePanel.layout = GridLayout(2, 1, 0, 0)
            tablePanel.add(tableFavouriteDoublesOther)
        }

        tablePanel.repaint()
    }

    override fun selectionChanged(src: ScrollTable)
    {
        val pieChart = pieChartPanel.chart
        val plot = pieChart.plot as PiePlot

        //Unset the old value
        selectedScore?.let { plot.setExplodePercent(it.toString(), 0.0) }

        val selectedRow = src.selectedModelRow
        if (selectedRow > -1)
        {
            val newSelection = src.getValueAt(selectedRow, 0) as Int
            plot.setExplodePercent(newSelection.toString(), 0.2)
            selectedScore = newSelection
        }
    }
}