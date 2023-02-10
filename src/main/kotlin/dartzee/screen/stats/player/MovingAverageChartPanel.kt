package dartzee.screen.stats.player

import dartzee.core.util.getAllChildComponentsForType
import dartzee.utils.DartsColour.getBrightenedColour
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.MovingAverage
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JPanel

class MovingAverageChartPanel(private val parentTab: AbstractStatisticsTab) : JPanel(), ActionListener
{
    private var graphCollection = XYSeriesCollection()
    private val panelGraph = ChartPanel(null)
    private val panelCheckBoxes = JPanel()

    init
    {
        layout = BorderLayout(0, 0)
        panelGraph.layout = BorderLayout(0, 0)
        add(panelGraph, BorderLayout.CENTER)
        add(panelCheckBoxes, BorderLayout.SOUTH)
    }

    fun reset(title: String, yLabel: String)
    {
        graphCollection = XYSeriesCollection()
        panelGraph.chart = ChartFactory.createXYLineChart(title, "Game", yLabel, graphCollection)
    }

    fun finalise()
    {
        adjustTickboxes()
        setPlotColours(panelGraph.chart)
    }

    private fun adjustTickboxes()
    {
        //Remove checkboxes that are no longer relevant (e.g. just removed a comparison)
        panelCheckBoxes.getAllChildComponentsForType<JCheckBox>().forEach {
            if (getGraphSeriesIndexForCheckBox(it) == -1) panelCheckBoxes.remove(it)
        }

        //Go through what's left and add any others that are required by the graph (e.g. just added a comparison)
        val checkBoxes = panelCheckBoxes.getAllChildComponentsForType<JCheckBox>()
        val allSeries: List<XYSeries> = graphCollection.getXYSeries()
        allSeries.forEach {
            val key = "${it.key}"
            if (checkBoxes.none { cb -> cb.text == key} )
            {
                val checkBox = JCheckBox(key)
                checkBox.isSelected = true
                checkBox.addActionListener(this)
                panelCheckBoxes.add(checkBox)
            }
        }

        //For any checkboxes that are unticked, remove the graph series
        checkBoxes.filter { !it.isSelected }.forEach { graphCollection.removeSeries(getGraphSeriesIndexForCheckBox(it)) }

        panelCheckBoxes.validate()
        panelCheckBoxes.repaint()
    }

    private fun getGraphSeriesIndexForCheckBox(checkBox: JCheckBox): Int
    {
        val allSeries: List<XYSeries> = graphCollection.getXYSeries()
        return allSeries.indexOfFirst { it.key == checkBox.text }
    }

    private fun XYSeriesCollection.getXYSeries() = series.filterIsInstance<XYSeries>()

    private fun setPlotColours(chart: JFreeChart?)
    {
        val plot = chart!!.plot as XYPlot
        val allSeries: List<XYSeries> = graphCollection.getXYSeries()

        allSeries.forEachIndexed { ix, series ->
            val key = "${series.key}"
            var colour = Color.blue
            if (key.contains("Other")) {
                colour = Color.red
            }
            if (key.contains("(moving)")) {
                colour = getBrightenedColour(colour!!)
            }
            plot.renderer.setSeriesPaint(ix, colour)
        }
    }

    fun addSeries(series: XYSeries, avgThreshold: Int)
    {
        val movingAvgSeries = createMovingAverage(series, avgThreshold.toLong())
        graphCollection.addSeries(series)
        graphCollection.addSeries(movingAvgSeries)
    }

    private fun createMovingAverage(original: XYSeries, avgThreshold: Long): XYSeries
    {
        val collection = XYSeriesCollection(original)
        val movingAvgCollection = MovingAverage.createMovingAverage(collection," (moving)", avgThreshold,avgThreshold - 1)
        return (movingAvgCollection as XYSeriesCollection).getSeries(0)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        parentTab.populateStats()
    }
}