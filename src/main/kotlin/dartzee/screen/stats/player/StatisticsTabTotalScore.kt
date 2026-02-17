package dartzee.screen.stats.player

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.NumberField
import dartzee.core.bean.selectedItemTyped
import dartzee.core.obj.HashMapCount
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.game.GAME_PARAMS_NOT_APPLICABLE
import dartzee.screen.stats.median
import dartzee.stats.GameWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.beans.PropertyChangeEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.ImageIcon
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.border.TitledBorder
import javax.swing.table.DefaultTableCellRenderer
import net.miginfocom.swing.MigLayout
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.xy.XYSeries

/**
 * A tab to show bar a line representations for the 'FinalScore' column on Participant. Configurable
 * title so it can show "Total Darts" for X01, etc. Also displays mean and median values for
 * FinalScore.
 */
class StatisticsTabTotalScore(private val graphTitle: String, outlierMax: Int) :
    AbstractStatisticsTab(), ActionListener {
    private var graphMin = Integer.MAX_VALUE
    private var graphMax = -1

    private var dataset: DefaultCategoryDataset? = null
    private var boxDataset: DefaultBoxAndWhiskerCategoryDataset? = null

    private val panel = ChartPanel(null)
    private val nfOutlier = NumberField(10, 200)
    private val lblGrouping = JLabel("Grouping")
    private val nfGroups = NumberField(1, 20)
    private val panelBar = JPanel()
    private val panelBarConfiguration = JPanel()
    private val lblMean = JLabel("Mean")
    private val lblMedian = JLabel("Median")
    private val nfMean = NumberField(testId = "Mean")
    private val nfMedian = NumberField(testId = "Median")
    private val lblGameType = JLabel("Game Type")
    private val panelRawDataTable = JPanel()
    private val table = ScrollTableDartsGame()
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val comboBox = JComboBox<ComboBoxItem<String>>()
    private val nfMeanOther = NumberField()
    private val nfMedianOther = NumberField()
    private val panelLine = JPanel()
    private val lineChartPanel = MovingAverageChartPanel(this)
    private val panelLineConfiguration = JPanel()
    private val lblMovingAvg = JLabel("Moving avg")
    private val nfAverageThreshold = NumberField(1, 200)
    private val panelBoxAndWhisker = JPanel()
    private val boxAndWhiskerChartPanel = ChartPanel(null)
    private val chckbxIncludeUnfinishedGames = JCheckBox("Include unfinished")

    init {
        nfMedianOther.preferredSize = Dimension(50, 20)
        nfMeanOther.preferredSize = Dimension(50, 20)
        layout = BorderLayout(0, 0)
        add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab(
            "",
            ImageIcon(javaClass.getResource("/icons/bar chart.png")),
            panelBar,
            null,
        )
        panelBar.layout = BorderLayout(0, 0)
        panelBar.add(panel, BorderLayout.CENTER)
        panelBar.add(panelBarConfiguration, BorderLayout.EAST)
        panelBarConfiguration.layout = MigLayout("", "[][grow]", "[][][]")
        val lblOutlierCutoff = JLabel("Outlier cutoff")
        panelBarConfiguration.add(lblOutlierCutoff, "cell 0 0")
        panelBarConfiguration.add(nfOutlier, "cell 1 0")
        nfOutlier.value = 75
        nfOutlier.preferredSize = Dimension(50, 20)
        nfOutlier.addPropertyChangeListener(this)
        panelBarConfiguration.add(lblGrouping, "cell 0 1")
        panelBarConfiguration.add(nfGroups, "cell 1 1")
        nfGroups.value = 3
        nfGroups.preferredSize = Dimension(50, 20)
        tabbedPane.addTab(
            "",
            ImageIcon(javaClass.getResource("/icons/line chart.png")),
            panelLine,
            null,
        )
        panelLine.layout = BorderLayout(0, 0)
        panelLine.add(lineChartPanel, BorderLayout.CENTER)
        panelLine.add(panelLineConfiguration, BorderLayout.EAST)
        panelLineConfiguration.layout = MigLayout("", "[][]", "[]")
        panelLineConfiguration.add(lblMovingAvg, "flowx,cell 0 0")
        nfAverageThreshold.value = 5
        nfAverageThreshold.preferredSize = Dimension(40, 20)
        panelLineConfiguration.add(nfAverageThreshold, "cell 1 0")
        tabbedPane.addTab(
            "",
            ImageIcon(javaClass.getResource("/icons/boxAndWhisker.png")),
            panelBoxAndWhisker,
            null,
        )
        panelBoxAndWhisker.layout = BorderLayout(0, 0)
        panelBoxAndWhisker.add(boxAndWhiskerChartPanel, BorderLayout.CENTER)
        nfGroups.addPropertyChangeListener(this)
        val panelRawData = JPanel()
        add(panelRawData, BorderLayout.WEST)
        panelRawData.layout = MigLayout("", "[][]", "[][][][][][grow][][]")
        panelRawData.add(lblGameType, "cell 0 0")
        panelRawData.add(comboBox, "flowx,cell 1 0")
        panelRawData.add(lblMean, "cell 0 2,alignx leading")
        nfMean.preferredSize = Dimension(50, 20)
        panelRawData.add(nfMean, "cell 1 2")
        nfMean.isEditable = false
        nfMeanOther.isEditable = false
        nfMedianOther.isEditable = false
        nfMeanOther.foreground = Color.RED
        nfMedianOther.foreground = Color.RED
        panelRawData.add(lblMedian, "cell 0 3,alignx leading")
        nfMedian.preferredSize = Dimension(50, 20)
        panelRawData.add(nfMedian, "flowx,cell 1 3")
        nfMedian.isEditable = false
        panelRawDataTable.border =
            TitledBorder(null, "Raw Data", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panelRawData.add(panelRawDataTable, "cell 0 5 2 3,grow")
        panelRawDataTable.layout = BorderLayout(0, 0)
        panelRawDataTable.add(table)
        table.setPreferredScrollableViewportSize(Dimension(300, 800))
        nfAverageThreshold.addPropertyChangeListener(this)
        panelRawData.add(nfMeanOther, "cell 1 2")
        panelRawData.add(nfMedianOther, "cell 1 3")
        panelRawData.add(chckbxIncludeUnfinishedGames, "cell 1 0 2 1")
        nfOutlier.setMaximum(outlierMax)

        chckbxIncludeUnfinishedGames.addActionListener(this)
        comboBox.addActionListener(this)
    }

    override fun populateStats() {
        val gameParams = initialiseFields()

        populateStatsWithoutChangingFields(gameParams)
    }

    private fun populateStatsWithoutChangingFields(selectedParams: ComboBoxItem<String>) {
        val paramDesc = "$selectedParams"
        lineChartPanel.reset("$graphTitle ($paramDesc)", graphTitle)

        // Filter out unfinished games and games with the wrong params
        val filter = { g: GameWrapper ->
            g.gameParams == selectedParams.hiddenData &&
                (g.isFinished() || chckbxIncludeUnfinishedGames.isSelected)
        }
        val gamesToGraph = filteredGames.filter(filter).sortedBy { it.dtStart }
        val otherGamesToGraph = filteredGamesOther.filter(filter).sortedBy { it.dtStart }
        val includeOther = !otherGamesToGraph.isEmpty()

        // Sort out what the min and max displayed on the graph will be
        adjustGraphMinAndMax(gamesToGraph, otherGamesToGraph)

        // Sort the games and populate the raw data table
        populateTable(gamesToGraph)

        dataset = DefaultCategoryDataset()
        boxDataset = DefaultBoxAndWhiskerCategoryDataset()

        addValuesToDataset(gamesToGraph, "Me", nfMedian, nfMean)
        if (includeOther) {
            addValuesToDataset(otherGamesToGraph, "Other", nfMedianOther, nfMeanOther)
        }

        finaliseBarChart(paramDesc)
        finaliseBoxPlot(paramDesc)

        nfMeanOther.isVisible = includeOther
        nfMedianOther.isVisible = includeOther

        lineChartPanel.finalise()
    }

    private fun initialiseFields(): ComboBoxItem<String> {
        val gameParams = initialiseGameTypeComboBox()

        initialiseOutlierCutOffAndGrouping(gameParams.hiddenData)

        return gameParams
    }

    private fun initialiseOutlierCutOffAndGrouping(gameParams: String) {
        val filter = { g: GameWrapper -> g.gameParams == gameParams && g.isFinished() }
        val gamesToGraph = filteredGames.filter(filter)
        if (gamesToGraph.isEmpty()) {
            return
        }

        val scores = gamesToGraph.map { it.finalScore }.sorted()

        val lqIndex = scores.size / 4
        val uqIndex = 3 * lqIndex
        val uq = scores[uqIndex]
        val iqr = uq - scores[lqIndex]

        val outlierThreshold = uq + 3 * iqr / 2
        nfOutlier.value = outlierThreshold

        val min = scores.first()
        val max = scores.last()

        // Go for 10 bars, whatever that works out to be
        var grouping = (max - min) / 10
        grouping = Math.max(1, grouping)
        nfGroups.value = grouping
    }

    private fun finaliseBarChart(gameParams: String) {
        val barChart =
            ChartFactory.createBarChart(
                "$graphTitle ($gameParams)",
                graphTitle,
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false,
            )

        val plot = barChart.categoryPlot
        plot.renderer.setSeriesPaint(0, Color.BLUE)
        if (includeOtherComparison()) {
            plot.renderer.setSeriesPaint(1, Color.RED)
        }

        panel.chart = barChart
    }

    private fun finaliseBoxPlot(gameParams: String) {
        val boxChart =
            ChartFactory.createBoxAndWhiskerChart(
                "$graphTitle ($gameParams)",
                "",
                "",
                boxDataset,
                true,
            )

        val renderer = BoxAndWhiskerRenderer()
        renderer.isMeanVisible = false
        renderer.setSeriesPaint(0, Color.BLUE)
        if (includeOtherComparison()) {
            renderer.setSeriesPaint(1, Color.RED)
        }

        val plot = boxChart.categoryPlot
        plot.orientation = PlotOrientation.HORIZONTAL

        plot.renderer = renderer

        boxAndWhiskerChartPanel.chart = boxChart
    }

    private fun populateTable(gamesToGraph: List<GameWrapper>) {
        val model = DefaultModel()
        model.addColumn("Ordinal")
        model.addColumn("Score")
        model.addColumn("Game")
        model.addColumn("!Unfinished")

        for (i in gamesToGraph.indices) {
            val g = gamesToGraph[i]

            var finalScore = g.finalScore
            if (finalScore == -1) {
                finalScore = g.getAllDarts().size
            }

            val row = arrayOf(i + 1, finalScore, g.localId, !g.isFinished())
            model.addRow(row)
        }

        table.model = model
        table.setRenderer(1, TotalScoreRenderer())
        table.removeColumn(3)
    }

    /**
     * No more mentalness with radio buttons (though it was fun...) Now just have a combo box that
     * we populate. Still try to preserve the previous selection if we can.
     */
    private fun initialiseGameTypeComboBox(): ComboBoxItem<String> {
        // Remember what was selected previously.
        val selectedItem = comboBox.selectedItem

        // Now get what scores should now show
        val startingScores = getDistinctGameParams().sorted().toMutableList()

        // Handle 0 games
        if (startingScores.isEmpty()) {
            startingScores.add(GAME_PARAMS_NOT_APPLICABLE)
        }

        val comboOptions =
            startingScores
                .map { ComboBoxItem(it, getGameType().getParamsDescription(it)) }
                .toTypedArray()
        comboBox.model = DefaultComboBoxModel(comboOptions)
        comboBox.selectedItem = selectedItem

        var ix = comboBox.selectedIndex
        if (ix == -1) {
            ix = 0
            comboBox.selectedIndex = 0
        }

        return comboBox.getItemAt(ix)
    }

    /** Get the minimum and maximum number of darts for the graph */
    private fun adjustGraphMinAndMax(
        gamesToGraph: List<GameWrapper>,
        gamesToGraphOther: List<GameWrapper>,
    ) {
        val combined = (gamesToGraph + gamesToGraphOther).filter { it.isFinished() }

        graphMax = combined.map { it.finalScore }.maxOrNull() ?: Integer.MAX_VALUE
        graphMin = combined.map { it.finalScore }.minOrNull() ?: 0
    }

    /** Deal with populating the dataset used by the bar chart */
    private fun addValuesToDataset(
        gamesToGraph: List<GameWrapper>,
        legendKey: String,
        nfMedian: NumberField,
        nfMean: NumberField,
    ) {
        // Build up counts for each game finish value
        val suffix = " ($legendKey)"
        val series = XYSeries(graphTitle + suffix)
        val hmNoDartsToCount = HashMapCount<Int>()
        for (i in gamesToGraph.indices) {
            val game = gamesToGraph[i]
            val score = game.finalScore
            if (score > -1) {
                series.add((i + 1).toDouble(), score.toDouble())
                hmNoDartsToCount.incrementCount(score)
            }
        }

        lineChartPanel.addSeries(series, nfAverageThreshold.getNumber())

        appendToDataset(legendKey, hmNoDartsToCount)

        val scores = gamesToGraph.map { it.finalScore }
        val avg = scores.average()
        val median = scores.median()
        nfMedian.value = median
        nfMean.value = avg
    }

    private fun appendToDataset(legendKey: String, hmNoDartsToCount: HashMapCount<Int>) {
        val outlierLimit = nfOutlier.getNumber()
        val groups = nfGroups.getNumber()

        var groupCount = 0
        var rangeStart = graphMin
        for (i in graphMin..Math.min(outlierLimit, graphMax)) {
            groupCount += hmNoDartsToCount.getCount(i)

            // If we're a multiple of the group #...
            if (i % groups == 0 || i == Math.min(outlierLimit, graphMax)) {
                val rangeDesc = getRangeDesc(rangeStart, i)
                dataset!!.addValue(groupCount.toDouble(), legendKey, rangeDesc)

                // Set up for the next block
                groupCount = 0
                rangeStart = i + 1
            }
        }

        val hmOutliers = hmNoDartsToCount.filterKeys { it > outlierLimit }
        val outlierCount = hmOutliers.map { it.value }.sum()

        dataset!!.addValue(outlierCount.toDouble(), legendKey, (outlierLimit + 1).toString() + "+")

        // Also add to the Box and Whisker dataset
        val allValues = hmNoDartsToCount.getFlattenedOrderedList(null)
        boxDataset!!.add(allValues, legendKey, "")
    }

    private fun getRangeDesc(start: Int, finish: Int) =
        if (start == finish) "$start" else "$start - $finish"

    /** GameParams combo box */
    override fun actionPerformed(arg0: ActionEvent) {
        populateStats()
    }

    /** The number fields */
    override fun propertyChange(arg0: PropertyChangeEvent) {
        val propertyName = arg0.propertyName
        if (propertyName == "value") {
            val selectedGameParams = comboBox.selectedItemTyped()
            populateStatsWithoutChangingFields(selectedGameParams)
        }
    }

    private class TotalScoreRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            val modelRow = table!!.rowSorter.convertRowIndexToModel(row)

            val model = table.model as DefaultModel
            val unfinished = model.getValueAt(modelRow, 3) as Boolean
            foreground =
                if (unfinished) {
                    if (isSelected) Color.CYAN else Color.RED
                } else {
                    if (isSelected) Color.WHITE else Color.BLACK
                }

            return this
        }
    }
}
