package dartzee.screen.game.x01

import dartzee.core.bean.NumberField
import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.game.UniqueParticipantName
import dartzee.game.X01Config
import dartzee.game.state.X01PlayerState
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractGameStatisticsPanel
import dartzee.utils.calculateThreeDartAverage
import dartzee.utils.getScoringDarts
import dartzee.utils.isCheckoutDart
import dartzee.utils.sumScore
import java.awt.BorderLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JLabel
import javax.swing.JPanel

/** Shows running stats for X01 games - three-dart average, checkout % etc. */
open class GameStatisticsPanelX01(gameParams: String) :
    AbstractGameStatisticsPanel<X01PlayerState>(), PropertyChangeListener {
    private val panel = JPanel()
    private val lblSetupThreshold = JLabel("Setup Threshold")
    val nfSetupThreshold = NumberField()

    init {
        add(panel, BorderLayout.NORTH)
        panel.add(lblSetupThreshold)

        panel.add(nfSetupThreshold)
        nfSetupThreshold.columns = 10

        nfSetupThreshold.value = 100
        nfSetupThreshold.addPropertyChangeListener(this)
        nfSetupThreshold.setMinimum(62)
        nfSetupThreshold.setMaximum(X01Config.fromJson(gameParams).target - 1)
    }

    override fun getRankedRowsHighestWins() =
        listOf("Highest Score", "3-dart avg", "Lowest Score", "Treble %", "Checkout %")

    override fun getRankedRowsLowestWins() = listOf("Miss %")

    override fun getHistogramRows() =
        listOf(
            "180",
            "140 - 179",
            "100 - 139",
            "80 - 99",
            "60 - 79",
            "40 - 59",
            "20 - 39",
            "0 - 19"
        )

    override fun getStartOfSectionRows() = listOf("180", "Top Darts", "Checkout %", "Best Game")

    override fun addRowsToTable() {
        addRow(getScoreRow("Highest Score") { it.maxOrZero() })
        addRow(getThreeDartAvgsRow())
        addRow(getScoreRow("Lowest Score") { it.minOrZero() })
        addRow(getMultiplePercent("Miss %", 0))
        addRow(getMultiplePercent("Treble %", 3))

        addRow(getScoresBetween(180, 181, "180"))
        addRow(getScoresBetween(140, 180, "140 - 179"))
        addRow(getScoresBetween(100, 140, "100 - 139"))
        addRow(getScoresBetween(80, 100, "80 - 99"))
        addRow(getScoresBetween(60, 80, "60 - 79"))
        addRow(getScoresBetween(40, 60, "40 - 59"))
        addRow(getScoresBetween(20, 40, "20 - 39"))
        addRow(getScoresBetween(0, 20, "0 - 19"))

        addTopDartsRows()

        addRow(getCheckoutPercentRow())

        table.setColumnWidths("120")
    }

    private fun addTopDartsRows() {
        val topDarts = factoryRow("Top Darts")
        val secondDarts = factoryRow("")
        val thirdDarts = factoryRow("")
        val fourthDarts = factoryRow("")
        val fifthDarts = factoryRow("")
        val remainingDarts = factoryRow("Remainder")

        for (i in uniqueParticipantNamesOrdered.indices) {
            val playerName = uniqueParticipantNamesOrdered[i]
            val darts = getScoringDarts(playerName)

            val hmHitScoreToDarts = darts.groupBy { it.getHitScore() }
            val sortedEntries =
                hmHitScoreToDarts.entries
                    .sortedWith(
                        compareByDescending<Map.Entry<Int, List<Dart>>> { it.value.size }
                            .thenByDescending { it.key }
                    )
                    .toMutableList()

            parseTopDartEntry(sortedEntries, topDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, secondDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, thirdDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, fourthDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, fifthDarts, i, darts.size)

            // Deal with the remainder
            val remainder = sortedEntries.map { it.value.size }.sum().toDouble()
            val percent = MathsUtil.getPercentage(remainder, darts.size, 0).toInt()
            remainingDarts[i + 1] = "$percent%"
        }

        addRow(topDarts)
        addRow(secondDarts)
        addRow(thirdDarts)
        addRow(fourthDarts)
        addRow(fifthDarts)
        addRow(remainingDarts)
    }

    private fun parseTopDartEntry(
        sortedEntries: MutableList<Map.Entry<Int, List<Dart>>>,
        row: Array<Any?>,
        i: Int,
        totalDarts: Int
    ) {
        if (sortedEntries.isEmpty()) {
            row[i + 1] = "N/A [0%]"
        } else {
            val entry = sortedEntries.removeAt(0)
            val percent = MathsUtil.getPercentage(entry.value.size, totalDarts, 0).toInt()

            row[i + 1] = "${entry.key} [$percent%]"
        }
    }

    private fun getThreeDartAvgsRow() =
        prepareRow("3-dart avg") { playerName ->
            val darts = getFlattenedDarts(playerName)

            val avg = calculateThreeDartAverage(darts, nfSetupThreshold.getNumber())
            if (avg < 0) null else MathsUtil.round(avg, 2)
        }

    private fun getCheckoutPercentRow() =
        prepareRow("Checkout %") { playerName ->
            val darts = getFlattenedDarts(playerName)

            val potentialFinishers = darts.filter { d -> isCheckoutDart(d) }
            val actualFinishes =
                potentialFinishers.filter { d -> d.isDouble() && d.getTotal() == d.startingScore }

            if (actualFinishes.isEmpty()) {
                null
            } else {
                MathsUtil.getPercentage(actualFinishes.size, potentialFinishers.size)
            }
        }

    private fun getScoresBetween(min: Int, max: Int, desc: String) =
        prepareRow(desc) { playerName ->
            val rounds = getScoringRounds(playerName)
            val roundsInRange = rounds.filter { r -> sumScore(r) in min until max }
            roundsInRange.size
        }

    private fun getScoreRow(desc: String, f: (i: List<Int>) -> Int) =
        prepareRow(desc) { playerName ->
            val rounds = getScoringRounds(playerName)
            val roundsAsTotal = rounds.map { rnd -> sumScore(rnd) }
            if (roundsAsTotal.isEmpty()) null else f(roundsAsTotal)
        }

    private fun getMultiplePercent(desc: String, multiplier: Int) =
        prepareRow(desc) { playerName ->
            val scoringDarts = getScoringDarts(playerName)
            val hits = scoringDarts.filter { d -> d.multiplier == multiplier }
            MathsUtil.getPercentage(hits.size, scoringDarts.size)
        }

    private fun getScoringRounds(playerName: UniqueParticipantName): List<List<Dart>> {
        val rounds = hmPlayerToDarts[playerName]
        rounds ?: return mutableListOf()

        return rounds.filter { it.last().startingScore > nfSetupThreshold.getNumber() }.toList()
    }

    private fun getScoringDarts(playerName: UniqueParticipantName): List<Dart> {
        val darts = getFlattenedDarts(playerName)
        return getScoringDarts(darts, nfSetupThreshold.getNumber())
    }

    override fun propertyChange(arg0: PropertyChangeEvent) {
        val propertyName = arg0.propertyName
        if (propertyName == "value") {
            buildTableModel()
            repaint()
        }
    }
}
