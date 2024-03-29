package dartzee.screen.stats.player.x01

import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.NumberField
import dartzee.core.util.MathsUtil
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.screen.stats.player.AbstractStatisticsTab
import dartzee.screen.stats.player.MovingAverageChartPanel
import dartzee.stats.GameWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.TitledBorder
import net.miginfocom.swing.MigLayout
import org.jfree.data.xy.XYSeries

class StatisticsTabX01ThreeDartAverage : AbstractStatisticsTab() {
    private val nfScoringThreshold = NumberField(62, 501, testId = "setupThreshold")
    private val lblMovingAverageInterval = JLabel("Moving Average Interval")
    private val nfAverageThreshold = NumberField(1, 200)
    private val lblDartAverage = JLabel("3 Dart Average")
    private val nfThreeDartAverage = NumberField(testId = "overallAverage")
    private val panelTable = JPanel()
    private val tableBestAverages = ScrollTableDartsGame()
    private val nfOtherThreeDartAvg = NumberField(testId = "overallAverageOther")
    private val panelCenter = JPanel()
    private val chartPanel = MovingAverageChartPanel(this)
    private val lblMiss = JLabel("Miss %")
    private val nfMissPercent = NumberField(testId = "missPercent")
    private val nfOtherMissPercent = NumberField(testId = "missPercentOther")

    init {
        nfAverageThreshold.preferredSize = Dimension(40, 20)
        layout = BorderLayout(0, 0)
        val panel = JPanel()
        add(panel, BorderLayout.EAST)
        panel.layout = MigLayout("", "[][grow]", "[][][][][][][]")
        val lblSetupThreshold = JLabel("Setup Threshold")
        panel.add(lblSetupThreshold, "cell 0 0,alignx leading")
        nfScoringThreshold.preferredSize = Dimension(40, 20)
        nfThreeDartAverage.preferredSize = Dimension(40, 20)
        panel.add(nfScoringThreshold, "cell 1 0")
        nfScoringThreshold.value = 140
        nfAverageThreshold.value = 5
        panel.add(lblMovingAverageInterval, "cell 0 1,alignx leading")
        panel.add(nfAverageThreshold, "cell 1 1")
        panel.add(lblDartAverage, "cell 0 3,alignx leading")
        nfThreeDartAverage.isEditable = false
        panel.add(nfThreeDartAverage, "flowx,cell 1 3")
        panel.add(lblMiss, "cell 0 4,alignx trailing")
        nfMissPercent.preferredSize = Dimension(40, 20)
        nfMissPercent.isEditable = false
        panel.add(nfMissPercent, "flowx,cell 1 4")
        panelTable.border =
            TitledBorder(null, "Raw Data", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        panel.add(panelTable, "cell 0 6 2 1")
        panelTable.layout = BorderLayout(0, 0)
        panelTable.add(tableBestAverages)
        tableBestAverages.setPreferredScrollableViewportSize(Dimension(300, 800))
        nfOtherThreeDartAvg.preferredSize = Dimension(40, 20)
        nfOtherThreeDartAvg.isEditable = false
        nfOtherThreeDartAvg.foreground = Color.RED
        panel.add(nfOtherThreeDartAvg, "cell 1 3")
        nfOtherMissPercent.preferredSize = Dimension(40, 20)
        nfOtherMissPercent.foreground = Color.RED
        nfOtherMissPercent.isEditable = false
        panel.add(nfOtherMissPercent, "cell 1 4")
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(chartPanel, BorderLayout.CENTER)

        nfScoringThreshold.addPropertyChangeListener(this)
        nfAverageThreshold.addPropertyChangeListener(this)
    }

    override fun populateStats() {
        chartPanel.reset("3 Dart Average", "Average")

        nfOtherThreeDartAvg.isVisible = includeOtherComparison()
        nfOtherMissPercent.isVisible = includeOtherComparison()

        // Construct the table model
        val model = DefaultModel()
        model.addColumn("Ordinal")
        model.addColumn("Average")
        model.addColumn("Start Value")
        model.addColumn("Game")

        populateStats(filteredGames, model, nfThreeDartAverage, nfMissPercent, "")
        if (includeOtherComparison()) {
            populateStats(
                filteredGamesOther,
                null,
                nfOtherThreeDartAvg,
                nfOtherMissPercent,
                " (Other)"
            )
        }

        chartPanel.finalise()

        // Finish off the table model
        tableBestAverages.model = model
        tableBestAverages.sortBy(0, false)
    }

    private fun populateStats(
        filteredGames: List<GameWrapper>,
        model: DefaultModel?,
        nfThreeDartAverage: JTextField,
        nfMissPercent: JTextField,
        graphSuffix: String
    ) {
        // Filter out unfinished games, then sort by start date
        val sortedGames = filteredGames.sortedBy { it.dtStart }
        val scoreThreshold = nfScoringThreshold.getNumber()

        val allScoringDarts = sortedGames.flatMap { it.getScoringDarts(scoreThreshold) }

        val totalScoringDarts = allScoringDarts.size.toDouble()
        val misses = allScoringDarts.count { it.multiplier == 0 }.toDouble()
        val overallThreeDartAvg =
            3 * allScoringDarts.sumOf { it.getTotal() } / allScoringDarts.size.toDouble()

        val rawAverages = XYSeries("Avg$graphSuffix")
        sortedGames.forEachIndexed { i, game ->
            val ordinal = i + 1
            val avg = game.getThreeDartAverage(scoreThreshold)
            val startValue = game.getGameStartValueX01()
            val gameId = game.localId

            // Table row - only show the raw data for the actual player, not the comparison
            if (model != null) {
                val row = arrayOf(ordinal, avg, startValue, gameId)
                model.addRow(row)
            }

            // Graph point
            rawAverages.add(ordinal, avg)
        }

        chartPanel.addSeries(rawAverages, nfAverageThreshold.getNumber())

        // Overall avg, to 1 d.p
        nfThreeDartAverage.text = "" + MathsUtil.round(overallThreeDartAvg, 1)

        // Miss percent, to 1 d.p
        nfMissPercent.text = "" + MathsUtil.round(100 * misses / totalScoringDarts, 1)
    }
}
